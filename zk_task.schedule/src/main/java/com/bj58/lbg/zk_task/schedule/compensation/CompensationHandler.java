package com.bj58.lbg.zk_task.schedule.compensation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;

/**
 * 数据补偿类
 * 此类主要用于补偿任务节点和调度节点的数据不一致的问题
 * 主要有三种情况：
 * 1. 因为节点服务down机的影响，导致只更新了newDataList的数据，没有来得及更新taskDataList的数据，
 * 2. 因为调度服务down机的影响，导致调度节点抢占了数据分配的权利（成功更新了状态），但是没能成功分配数据
 * 3. 检查两个数据对象有没有存在dataIds是空的情况，如果有空的就直接从列表中删掉这个对象
 * 其他类型的down机补偿会由分布式服务自身来解决
 * @author 常博
 *
 */
public class CompensationHandler implements Runnable {
	
	private ZooKeeper zk;
	private Watcher watcher;
	
	public CompensationHandler(ZooKeeper zk, Watcher watcher) {
		this.zk = zk;
		this.watcher = watcher;
	}
	
	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
	private Stat statNewData;
	private List<NewData> newDataList;
	private Stat statTaskData;
	private List<TaskData> taskDataList;
	/**
	 * 数据补偿
	 */
	public void dataCompensation() {
		executor.scheduleWithFixedDelay(this, 30, 30, TimeUnit.MINUTES);
	}

	public void run() {
		try {
			refreshNewData();
			refreshTaskData();
			//判断对象有没有存在dataIds是空的情况，如果有空的就直接从列表中删掉这个对象
			judgeEmptyForNewDataIds();
			judgeEmptyForTaskDataIds();
			//比较两份数据的不同
			compareDataIds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 比较两份数据的不同
	 * 从taskid的角度看，两组数据经过多次的处理，一定会出现一组不存在这个taskid的情况
	 * 如果是newData下存在taskid，而taskData下不存在，说明newData在更新状态之后，没能成功分配到task下，需要再分配一次
	 * 如果是taskData下存在taskid，而newData下不存在，说明有taskData部分数据没能更新成功，直接把它删除就好了
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	private void compareDataIds() throws KeeperException, InterruptedException {
		List<Long> newDataTaskIds = getNewDataTaskIds();
		List<Long> taskDataTaskIds = getTaskDataTaskIds();
		//找出newData多出来的taskIds
		List<Long> surplusNewDataTaskIds = getSurplusForNewData(newDataTaskIds, taskDataTaskIds);
		//找出taskData多出来的taskIds
		List<Long> surplusTaskDataTaskIds = getSurplusForTaskData(newDataTaskIds, taskDataTaskIds);
		//处理newData多余的taskIds
		handleSurplusNewDataTaskIds(surplusNewDataTaskIds);
		//处理taskData多余的taskIds
		handleSurplusTaskDataTaskIds(surplusTaskDataTaskIds);
	}

	/**
	 * taskData的直接删除
	 * @param surplusTaskDataTaskIds
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	private void handleSurplusTaskDataTaskIds(List<Long> surplusTaskDataTaskIds) throws KeeperException, InterruptedException {
		boolean flag = false;
		for (Long surplustaskId : surplusTaskDataTaskIds) {
			Iterator<TaskData> it = taskDataList.iterator();
			while(it.hasNext()) {
				TaskData taskData = it.next();
				if(taskData.getTaskId() == surplustaskId) {
					flag = true;
					it.remove();
				}
			}
		}
		if(flag) {
			try {
				zk.setData("/root/task", ByteUtil.objectToByte(taskDataList), statTaskData.getVersion());
			} catch (KeeperException.BadVersionException e) {
				//需要出现版本错误，就要重新取数据进行比较
				refreshNewData();
				refreshTaskData();
				compareDataIds();
			}
		}
	}

	/**
	 * newData的数据要重新分配
	 * @param surplusNewDataTaskIds
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	private void handleSurplusNewDataTaskIds(List<Long> surplusNewDataTaskIds) throws KeeperException, InterruptedException {
		boolean flag = false;
		for (Long surplusTaskId : surplusNewDataTaskIds) {
			for (NewData newData : newDataList) {
				if(newData.getTaskId() == surplusTaskId) {
					flag = true;
					newData.setStatus(Constant.STATUS_NEWDATA_NOT_PUBLISH);
				}
			}
		}
		if(flag) {
			try {
				zk.setData("/root/newdata", ByteUtil.objectToByte(newDataList), statNewData.getVersion());
			} catch (KeeperException.BadVersionException e) {
				//需要出现版本错误，就要重新取数据进行比较
				refreshNewData();
				refreshTaskData();
				compareDataIds();
			}
		}
	}

	private List<Long> getTaskDataTaskIds() {
		List<Long> taskDataTaskIds = new ArrayList<Long>();
		for (TaskData taskData : taskDataList) {
			if(!taskDataTaskIds.contains(taskData.getTaskId())) {
				taskDataTaskIds.add(taskData.getTaskId());
			}
		}
		return taskDataTaskIds;
	}

	private List<Long> getNewDataTaskIds() {
		List<Long> newDataTaskIds = new ArrayList<Long>();
		for (NewData newData : newDataList) {
			if(!newDataTaskIds.contains(newData.getTaskId())) {
				newDataTaskIds.add(newData.getTaskId());
			}
		}
		return newDataTaskIds;
	}

	private List<Long> getSurplusForTaskData(List<Long> newDataTaskIds, List<Long> taskDataTaskIds) {
		List<Long> surplusNewDataTaskIds = new ArrayList<Long>();
		for (Long newDataTaskId : newDataTaskIds) {
			if(!taskDataTaskIds.contains(newDataTaskId)) {
				surplusNewDataTaskIds.add(newDataTaskId);
			}
		}
		return surplusNewDataTaskIds;
	}

	private List<Long> getSurplusForNewData(List<Long> newDataTaskIds, List<Long> taskDataTaskIds) {
		List<Long> surplusTaskDataTaskIds = new ArrayList<Long>();
		for (Long taskDataTaskId : taskDataTaskIds) {
			if(!newDataTaskIds.contains(taskDataTaskId)) {
				surplusTaskDataTaskIds.add(taskDataTaskId);
			}
		}
		return surplusTaskDataTaskIds;
	}

	/**
	 * 重新获取taskData
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private void refreshTaskData() throws KeeperException, InterruptedException {
		statTaskData = zk.exists("/root/task", watcher);
		taskDataList = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/task", watcher, null));
	}

	/**
	 * 重新获取newData
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private void refreshNewData() throws KeeperException, InterruptedException {
		statNewData = zk.exists("/root/newdata", watcher);
		newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", watcher, null));
	}

	private void judgeEmptyForNewDataIds() throws KeeperException, InterruptedException {
		boolean flag = false;
		if(newDataList != null) {
			Iterator<NewData> it = newDataList.iterator();
			while(it.hasNext()) {
				NewData newData = it.next();
				if(newData == null || StringUtils.isBlank(newData.getDataIds())) {
					flag = true;
					it.remove();
				}
			}
		}
		if(flag) {
			try {
				zk.setData("/root/newData", ByteUtil.objectToByte(newDataList), statNewData.getVersion());
			} catch (KeeperException.BadVersionException e) {
				refreshNewData();
				judgeEmptyForNewDataIds();
			}
		}
	}
	
	private void judgeEmptyForTaskDataIds() throws KeeperException, InterruptedException {
		boolean flag = false;
		if(taskDataList != null) {
			Iterator<TaskData> it = taskDataList.iterator();
			while(it.hasNext()) {
				TaskData taskData = it.next();
				if(taskData == null || StringUtils.isBlank(taskData.getDataIds())) {
					flag = true;
					it.remove();
				}
			}
		}
		if(flag) {
			try {
				zk.setData("/root/task", ByteUtil.objectToByte(taskDataList), statTaskData.getVersion());
			} catch (KeeperException.BadVersionException e) {
				refreshTaskData();
				judgeEmptyForTaskDataIds();
			}
		}
	}
}
