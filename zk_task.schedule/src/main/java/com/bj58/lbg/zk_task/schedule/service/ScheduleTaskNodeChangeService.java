package com.bj58.lbg.zk_task.schedule.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.MemcacheComp;
import com.bj58.lbg.zk_task.core.util.NumberGroupUtil;
import com.bj58.lbg.zk_task.core.util.NumberSubstringUtil;

public class ScheduleTaskNodeChangeService extends ScheduleService{

	private MemcacheComp memcacheComp = MemcacheComp.getMemcacheComp();
	
	public ScheduleTaskNodeChangeService(ZooKeeper zk, Watcher watcher, String schedulePath, String taskPath) {
		super(zk, watcher, schedulePath, taskPath);
	}
	
	@Override
	public void run() {
		processTaskNodeChildrenChanged();
	}

	/**
	 * 处理任务的子节点发生变化
	 */
	public void processTaskNodeChildrenChanged() {
		try {
			//1. 继续保持监控
			List<String> children = zk.getChildren(taskPath, watcher); 
			//2. 遍历newdata里所有分配了的对象，看看哪些对象的哪些数据节点不在当前存活的节点（当然也可能出现的情况是新增了task节点）
			Stat newDataStat = zk.exists(schedulePath, watcher);
			List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData(schedulePath, watcher, null));
			Stat taskDataStat = zk.exists(taskPath, watcher);
			List<TaskData> taskDataList = (List<TaskData>) ByteUtil.byteToObject(zk.getData(taskPath, watcher, null));
			//这里要已newData的数据为准，（因为更新时，先更新newData，后更新taskData, 可能出现newDataList不存在但是taskDataList存在的情况，这个情况会由其他线程来扫描处理），
			//所以要newData依赖taskData里的分配任务来对比
			List<TaskData> pendingTaskDataList = findErrorNodeDataForNewData(children, newDataList, taskDataList);
			//3. 把不存在的节点数据进行一次处理，看看处理了哪些，把还没处理的（没处理的已经包含了处理错误的了）数据再进行一次重新分配
			if(pendingTaskDataList != null && pendingTaskDataList.size() > 0) {
				System.out.println("任务节点变化导致待处理的taskData：" + pendingTaskDataList);
				processPendingTaskDataList(pendingTaskDataList, newDataList, taskDataList, newDataStat, taskDataStat);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理包含不存在节点的数据
	 * 1. 更新newData列表里的数据（从这些节点中，找出已成功完成了的数据, 把成功了的数据从列表里去掉）
	 * 2. 更新taskData列表的数据（把错误节点对象去掉，把剩余的节点数据分配到列表里）
	 * 1要在2之前来执行
	 * 这里面有很重要的一点，如果1执行成功了，2失败了，这里会有其他的schedule节点来继续执行
	 * 因为多个schedule会同时收到节点变化的通知，他们同时去处理这个业务，只有一个节点能处理成功，
	 * 当其他节点在第二次尝试更新的时候，如果1成功了，再次执行也无影响；2如果之前失败，则成功执行2；2如果之前成功，则已经找不到失败的节点，方法自然完成
	 * @param pendingTaskDataList 包含错误节点的任务列表
	 * @throws Exception 
	 */
	private void processPendingTaskDataList(List<TaskData> pendingTaskDataList, List<NewData> newDataList, List<TaskData> taskDataList, Stat newDataStat, Stat taskDataStat) throws Exception {
		//从newData列表中去掉所有已经成功了的数据
		processNewDataList(pendingTaskDataList, newDataList, newDataStat);
		//从taskData列表中去掉所有pending节点数据, 并把剩余的dataIds重新分配
		processTaskDataList(pendingTaskDataList, taskDataList, taskDataStat);
	}

	/**
	 * 从taskData列表中去掉所有pending节点数据, 并把剩余的dataIds重新分配
	 * @param pendingTaskDataList
	 * @param taskDataList
	 * @param taskDataStat
	 * @throws Exception
	 */
	private void processTaskDataList(List<TaskData> pendingTaskDataList, List<TaskData> taskDataList, Stat taskDataStat) throws Exception {
		try {
			for (TaskData pendingTaskData : pendingTaskDataList) {
				//这里要一个个的更新，因为不同的pendingTaskData可能属于不同taskId，不能合并
				long taskId = pendingTaskData.getTaskId();
				String successIds = memcacheComp.get(pendingTaskData.getId()+"_"+taskId+"_success");
				//把taskDataList里的pending对象移除(这里做整体移除，剩余的id会新建任务对象放进去)
				String surplusIds = NumberSubstringUtil.substringNumber(pendingTaskData.getDataIds(), successIds);
				boolean removeResult = taskDataList.remove(pendingTaskData);
				System.out.println("removeTaskNodeData: " + removeResult);
				List<TaskData> surplusTaskDataList = publishSurplusIdsAgain(surplusIds, pendingTaskData); 
				taskDataList.addAll(surplusTaskDataList);
			}
			zk.setData(taskPath, ByteUtil.objectToByte(taskDataList), taskDataStat.getVersion());
			System.out.println("处理错误节点之后的taskDataList： " + taskDataList);
		} catch (KeeperException.BadVersionException e) {
			//这里要注意： 当发生版本更新异常时，要对整个业务做重新获取和比较，因为这里面涉及到的所有任务有可能都产生了变化
			//当执行N次之后，节点变化的问题有可能让本schedule处理，也有可能让别的schedule节点处理，会自然结束。
			processTaskNodeChildrenChanged();
		}
	}
	
	/**
	 * 遍历待处理的任务数据，从newData列表中去掉所有已经成功了的数据
	 * @param pendingTaskDataList
	 * @param newDataList
	 * @param newDataStat
	 * @throws Exception
	 */
	private void processNewDataList(List<TaskData> pendingTaskDataList, List<NewData> newDataList, Stat newDataStat) throws Exception {
		try {
			for (TaskData pendingTaskData : pendingTaskDataList) {
				long taskId = pendingTaskData.getTaskId();
				String successIds = memcacheComp.get(pendingTaskData.getId()+"_"+taskId+"_success");
				List<NewData> subNewDataList = getNewDataListByTaskId(taskId, newDataList);
				for (NewData subNewData : subNewDataList) {
					//把newData里的已经成功完成了的数据，从总数据中移除(这里只删除成功的，未执行的不动)
					String unfinishNewDataIds = NumberSubstringUtil.substringNumber(subNewData.getDataIds(), successIds);
					subNewData.setDataIds(unfinishNewDataIds);
				}
			}
			zk.setData(schedulePath, ByteUtil.objectToByte(newDataList), newDataStat.getVersion());
			System.out.println("处理错误节点数据之后的newDataList: " + newDataList);
		} catch (KeeperException.BadVersionException e) {
			//这里要注意： 当发生版本更新异常时，要对整个业务做重新获取和比较，因为这里面涉及到的所有任务有可能都产生了变化
			//当执行N次之后，节点变化的问题有可能让本schedule处理，也有可能让别的schedule节点处理，会自然结束。
			processTaskNodeChildrenChanged();
		}
	}
	
	/**
	 * 根据taskId找到对应的newDataList
	 * 因为一个taskId，有可能已经把之前错误的添加了进去，是可能存在多份的
	 * @param taskId
	 * @param newDataList
	 * @return
	 */
	private List<NewData> getNewDataListByTaskId(long taskId, List<NewData> newDataList) {
		List<NewData> subNewDataList = new ArrayList<NewData>();
		for (NewData newData : newDataList) {
			if(newData.getTaskId() == taskId) {
				subNewDataList.add(newData);
			}
		}
		return subNewDataList;
	}
	
	/**
	 * 对剩余的id进行重新分配
	 * @param surplusIds
	 * @return
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	private List<TaskData> publishSurplusIdsAgain(String surplusIds, TaskData pendingTaskData) throws KeeperException, InterruptedException {
		List<TaskData> _taskDatas = new ArrayList<TaskData>();   //存放分配好的任务数据
		List<String> nodes = zk.getChildren(taskPath, watcher);
		int nodeSize = nodes.size();
		List<String> list = NumberGroupUtil.groupNumber(surplusIds, nodeSize);
		if(list != null && list.size() > 0) {
			for (int i=0;i<list.size();i++) {
				TaskData taskData = new TaskData();
				taskData.setId(nodes.get(i)+"_"+System.currentTimeMillis());
				taskData.setTaskId(pendingTaskData.getTaskId());
				taskData.setNodeName(nodes.get(i));
				taskData.setDataIds(list.get(i));
				taskData.setStatus(Constant.STATUS_TASKDATA_NOSTART);
				taskData.setCreatetime(new Date());
				taskData.setVersion(pendingTaskData.getVersion());
				_taskDatas.add(taskData);
			}
		}
		return _taskDatas;
	}
	
	/**
	 * 获取所有错误节点的任务
	 * @param children 当前存在的节点nodename
	 * @param newDataList
	 * @param taskDataList
	 * @return
	 */
	private List<TaskData> findErrorNodeDataForNewData(List<String> children, List<NewData> newDataList, List<TaskData> taskDataList) {
		List<TaskData> inexistTastDataList = new ArrayList<TaskData>();
		for (NewData newData : newDataList) {
			//先根据newData找对应的taskData列表
			List<TaskData> subTaskDatas = getTaskDatasForNewData(newData, taskDataList);
			//在根据subTaskDatas找出不存在的节点数据
			List<TaskData> _inexist = compareNodesForErrorTaskData(subTaskDatas, children);
			if(_inexist != null && _inexist.size() > 0) {
				inexistTastDataList.addAll(_inexist);
			}
		}
		return inexistTastDataList;
	}
	
	/**
	 * 获取newData分配的所有子任务
	 * @param newData
	 * @param taskDataList
	 * @return
	 */
	private List<TaskData> getTaskDatasForNewData(NewData newData, List<TaskData> taskDataList) {
		List<TaskData> subTaskData = new ArrayList<TaskData>();
		for (TaskData taskData : taskDataList) {
			if(taskData.getTaskId() == newData.getTaskId()) {
				subTaskData.add(taskData);
			}
		}
		return subTaskData;
	}

	/**
	 * 比较两个节点列表，返回taskData里已经不存在的节点数据
	 * @param subTaskDatas 分配的节点任务
	 * @param children	当前存在的节点
	 */
	private List<TaskData> compareNodesForErrorTaskData(List<TaskData> subTaskDatas, List<String> children) {
		List<TaskData> inexistTastData = new ArrayList<TaskData>();
		for (TaskData subTaskData : subTaskDatas) {
			if(!children.contains(subTaskData.getNodeName())) {
				inexistTastData.add(subTaskData);
			}
		}
		return inexistTastData;
	}
}
