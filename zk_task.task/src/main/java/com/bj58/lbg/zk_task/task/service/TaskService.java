package com.bj58.lbg.zk_task.task.service;

import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.exception.TaskHandleException;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.MemcacheComp;
import com.bj58.lbg.zk_task.core.util.NumberConcatUtil;
import com.bj58.lbg.zk_task.core.util.NumberSubstringUtil;
import com.bj58.lbg.zk_task.task.dto.TaskProgress;

public abstract class TaskService implements Runnable {
	private ZooKeeper zk;
	private Watcher watcher;
	private String nodeName;
	private MemcacheComp memcacheComp = MemcacheComp.getMemcacheComp();

	public TaskService(ZooKeeper zk, Watcher watcher, String nodeName) {
		this.zk = zk;
		this.watcher = watcher;
		this.nodeName = nodeName;
	}
	
	public TaskService() {
	
	}
	
	/**
	 * 因为zk等属性要在service创建之后才能被创建，所以该方法需要在zk创建好之后调用，不能写到构造方法里
	 * @param zk
	 * @param watcher
	 * @param nodeName
	 */
	public void initProperties(ZooKeeper zk, Watcher watcher, String nodeName) {
		this.zk = zk;
		this.watcher = watcher;
		this.nodeName = nodeName;
	}


	public void run() {
		findTaskDataToProcess();
	}

	/**
	 * 1. 更新这个taskData的状态
	 * 2. 执行每一个数据id，并记录每一条id的成功或失败结果
	 * 3. 处理完成之后，把这个taskData的数据从任务节点和调度节点的数据列表中删除
	 */
	public void findTaskDataToProcess() {
		try {
			// 先取版本，后取数据
			Stat stat = zk.exists("/root/task", watcher);
			List<TaskData> taskDatas = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/task", watcher, null));
			if (taskDatas != null && taskDatas.size() > 0) {
				for (TaskData taskData : taskDatas) {
					// 如果需要处理的节点是本节点且这段数据还处于未处理的状态
					if (nodeName.equals(taskData.getNodeName()) && taskData.getStatus() == Constant.STATUS_TASKDATA_NOSTART) {
						// 更新这个taskData的状态
						changeDoingStatusForTaskData(taskDatas, taskData, stat);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 注意：这里更新时候可能会出现版本不一致的情况
	private void changeDoingStatusForTaskData(List<TaskData> taskDatas, TaskData taskData, Stat stat) throws Exception {
		try {
			taskData.setStatus(Constant.STATUS_TASKDATA_DOING);
			stat = zk.setData("/root/task", ByteUtil.objectToByte(taskDatas), stat.getVersion());
			// 哪个taskData被更新成功，就意味着这个任务处理者具备了该taskData的处理权，可以对其进行业务处理
			if (stat != null) {
				// stat!=null 表示这个taskData更新成功，那么就可以继续对这个任务进行处理
				// 这里要注意：
				// 随着每一次获取新的版本，很有可能本次处理的taskData和上次的taskData不是同一个对象，所以taskData的业务处理代码一定要写在stat更新成功的代码里
				handleTaskData(taskData);
			}
		} catch (KeeperException.BadVersionException e) {
			// 如果发生版本错误的异常
			System.out.println("出现版本异常" + e.getMessage());
			getNewDataForUpdateForDoingStatus(taskData.getTaskId());
		}
	}

	/**
	 * 任务处理
	 * 
	 * @param taskData
	 * @throws Exception
	 */
	private void handleTaskData(TaskData taskData) throws Exception {
		System.out.println("本次处理的taskData： " + taskData);
		//获取任务执行结果
		TaskProgress progress = getTaskProcess(taskData);
		//处理完成之后，更新这个taskData从root的列表中删除
		finishTaskData(progress, taskData);
	}

	/**
	 * 获取任务的执行结果
	 * 
	 * @param taskData
	 * @return
	 * @throws Exception
	 */
	private TaskProgress getTaskProcess(TaskData taskData) throws Exception {
		TaskProgress progress = new TaskProgress();
		String[] strArr = taskData.getDataIds().split(",");
		for (String data : strArr) {
			if (data != null) {
				if (data.contains("-")) {
					int left = Integer.valueOf(data.split("-")[0]);
					int right = Integer.valueOf(data.split("-")[1]);
					for (int i = left; i <= right; i++) {
						//执行任务
						doAction(taskData, progress	, i);
					}
				} else {
					doAction(taskData, progress, Integer.valueOf(data));
				}
			}
		}
		return progress;
	}

	/**
	 * 执行任务
	 * 
	 * @param taskData
	 * @param progressDTO
	 * @param id
	 *            任务数据的id
	 * @throws Exception
	 */
	private void doAction(TaskData taskData, TaskProgress progress, int id) throws Exception {
		try {
			//如果错误id的执行次数超过了100次，就默认为执行成功，是为了防止无限次错误调用
			if(taskData.getVersion() <= 100) {
				doAction(id, taskData.getVersion());
			}
			// 线程处理时，每处理完一个数据，就要把id加到完成队列(字符串)里--memcache
			// put 到memcache里
			progress.setSuccessIds(NumberConcatUtil.concatNumber(progress.getSuccessIds(), id));
			memcacheComp.set(taskData.getId() + "_" + taskData.getTaskId() + "_success", progress.getSuccessIds());
		} catch (Exception e) {
			// 3. 处理过程中如果出错，则把错误的id加到errorIds中
			progress.setErrorIds(NumberConcatUtil.concatNumber(progress.getErrorIds(), id));
			memcacheComp.set(taskData.getId() + "_" + taskData.getTaskId() + "_error", progress.getErrorIds());
		}
	}

	/**
	 * 业务执行完成后，对taskData进行后续处理
	 * 1. 处理newData列表的数据：newData中去掉这个task里的dataids（包含成功的和失败的，因为失败的又补充到后面了），加上执行错误的内容
	 * 2. 处理taskData礼拜的数据：第一步成功之后，TaskData可以从列表中直接移除
	 * @param progress
	 * @param taskData
	 */
	private void finishTaskData(TaskProgress progress, TaskData taskData) {
		// 封装上次处理错误需要再次处理的NewData对象
		NewData errorData = encapsulateNewDataByErrorIds(progress.getErrorIds(), taskData);
		try {
			//对newDataList列表中的数据进行更新处理
			boolean flag = updateNewDataList(taskData, errorData);
			if (flag) {
				// newDataList更新成功，则再去处理taskDataList
				updateTaskDataList(taskData);
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新taskData时，要以任务对象的唯一id来确认，不能以总任务id来确认
	 * 
	 * @param taskData
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	private void updateTaskDataList(TaskData taskData) throws KeeperException, InterruptedException {
		try {
			// /root/task这个节点路径需要持续监听
			Stat stat = zk.exists("/root/task", watcher);
			List<TaskData> taskDatas = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/task", watcher, null));
			for (TaskData data : taskDatas) {
				if (data.getId().equals(taskData.getId())) {
					taskDatas.remove(data);
					break;
				}
			}
			zk.setData("/root/task", ByteUtil.objectToByte(taskDatas), stat.getVersion());
		} catch (KeeperException.BadVersionException e) {
			updateTaskDataList(taskData);
		}
	}

	/**
	 * 更新NewData列表
	 */
	private boolean updateNewDataList(TaskData taskData, NewData errorData) throws KeeperException, InterruptedException {
		try {
			// task节点不需要监听新数据节点的变化
			Stat stat = zk.exists("/root/newdata", false);
			List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", false, null));
			Iterator<NewData> it = newDataList.iterator();
			while (it.hasNext()) {
				NewData newData = it.next();
				if (newData.getTaskId() == taskData.getTaskId() && newData.getStatus() == Constant.STATUS_NEWDATA_PUBLISH) {
					// 剩余还处于执行状态下的dataIds
					String surplusDoingDataIds = NumberSubstringUtil.substringNumber(newData.getDataIds(), taskData.getDataIds());
					if (surplusDoingDataIds.equals(newData.getDataIds())) {
						// 表示当前这个newData没有要删除的id，不需要任何修改
						continue;
					}
					if (surplusDoingDataIds.length() == 0) {
						// 表示所有数据都处理完成， 这里把newData从列表中删除即可
						it.remove();
					} else {
						// 还有剩余的id，需要更新dataIds
						newData.setDataIds(surplusDoingDataIds);
						if (errorData != null) {
							// 如果存在上次错误的id，则需要再添加会newDataList里
							newDataList.add(errorData);
						}
					}
				}
			}
			// 正常来说，一个task执行完，一定会更新newDataList，这里就不在判断了，就直接setData了
			stat = zk.setData("/root/newdata", ByteUtil.objectToByte(newDataList), stat.getVersion());
			if (stat != null) {
				return true;
			}
		} catch (KeeperException.BadVersionException e) {
			System.out.println("版本异常" + e.getMessage());
			// 这里递归的时候之所以又在方法里想重新执行了一遍业务，是因为有可能要处理的这个newData已经被其他任务节点处理过了，即dataIds在这段时间里被新处理过了
			return updateNewDataList(taskData, errorData);
		}
		return false;
	}

	/**
	 * 生成errorIds的NewData对象
	 * 
	 * @param progress
	 * @param taskData
	 * @return
	 */
	private NewData encapsulateNewDataByErrorIds(String errorIds, TaskData taskData) {
		NewData errorData = null;
		if (errorIds != null && errorIds.length() > 0) {
			errorData = encapsulateErrorIdsToNewData(errorIds, taskData);
		}
		return errorData;
	}

	/**
	 * 把errorids封装成newData对象
	 * 
	 * @param errorIds
	 * @param taskData
	 * @return
	 */
	private NewData encapsulateErrorIdsToNewData(String errorIds, TaskData taskData) {
		NewData errorData = new NewData();
		errorData.setDataIds(errorIds);
		errorData.setStatus(Constant.STATUS_NEWDATA_NOT_PUBLISH);
		errorData.setTaskId(taskData.getTaskId());
		errorData.setVersion(taskData.getVersion() + 1); // 递增版本，用于判断同一个任务下的erririd的执行次数
		return errorData;
	}

	/**
	 * 重新获取/root/task下的最新值来更新taskData的状态
	 * 
	 * @param taskDataId
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private void getNewDataForUpdateForDoingStatus(long taskDataId) throws Exception {
		// 睡眠一秒后重新获取最新值
		Stat stat = zk.exists("/root/task", watcher);
		List<TaskData> taskDatas = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/task", watcher, null));
		if (taskDatas != null && taskDatas.size() > 0) {
			for (TaskData taskData : taskDatas) {
				if (nodeName.equals(taskData.getNodeName()) && taskData.getStatus() == Constant.STATUS_TASKDATA_NOSTART) {
					changeDoingStatusForTaskData(taskDatas, taskData, stat);
				}
			}
		}
	}

	/**
	 * 业务处理方法
	 * @param id 任务数据id
	 * @param version 该任务id被执行的次数，当id执行错误重新执行一次，这个version就会加1
	 * 这里要注意，为了防止大量的重复错误调用，业务方需要根据version的值做特殊处理，默认执行100次之后就会认为成功
	 * @throws TaskHandleException 如果执行时抛出此异常，就任务该id执行失败，需重新执行
	 */
	public abstract void doAction(int id, int version) throws TaskHandleException;
}
