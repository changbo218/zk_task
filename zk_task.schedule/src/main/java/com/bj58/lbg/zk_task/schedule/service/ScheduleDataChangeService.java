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
import com.bj58.lbg.zk_task.core.util.NumberGroupUtil;

public class ScheduleDataChangeService extends ScheduleService{

	public ScheduleDataChangeService(ZooKeeper zk, Watcher watcher) {
		super(zk, watcher);
	}
	
	@Override
	public void run() {
		nodeDataChangeForNewData();
	}

	/**
	 * newdata节点发生变化（可能有新数据，可能删除老数据）
	 */
	public void nodeDataChangeForNewData() {
		try {
			Stat stat = zk.exists("/root/newdata", watcher);
			List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", watcher, null));
			System.out.println("nodeDataChange: " + newDataList);
			for (NewData newData : newDataList) {
				if(newData.getStatus() == Constant.STATUS_NEWDATA_NOT_PUBLISH) {
					changeNewDataStatus(newData, newDataList, stat);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  更新还未分配的新数据，如果更新成功，则分配的工作就由这个watcher来处理
	 *  如果分配失败，出现版本错误，说明已有其他的watcher处理过，本watcher重新获取新数据，处理其他的新数据
	 */
	private void changeNewDataStatus(NewData newData, List<NewData> newDataList, Stat stat) throws KeeperException, InterruptedException {
		newData.setStatus(Constant.STATUS_NEWDATA_PUBLISH);
		try {
			stat = zk.setData("/root/newdata", ByteUtil.objectToByte(newDataList), stat.getVersion());
			if(stat != null) {
				//如果更新成功了，则开始分配数据
				System.out.println("更新newData状态成功"+newDataList);
				publishNewData(newData);
			}
		} catch (KeeperException.BadVersionException e) {
			// 如果发生版本错误的异常
			System.out.println("出现版本异常" + e.getMessage());
			//发生一次版本错误，就说明数据肯定是旧的，后面的更细肯定失败，就需要再重新取一次数据
			getNewDataForUpdateForPublishStatus();
		}
	}
	
	/**
	 * 重新获取/root/newdata下的最新值来更新newData的状态
	 * @param taskDataId
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private void getNewDataForUpdateForPublishStatus() throws KeeperException, InterruptedException {
		//睡眠一秒后重新获取最新值
		Thread.sleep(100);
		Stat stat = zk.exists("/root/newdata", watcher);
		List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", watcher, null));
		for (NewData newData : newDataList) {
			if(newData.getStatus() == Constant.STATUS_NEWDATA_NOT_PUBLISH) {
				changeNewDataStatus(newData, newDataList, stat);
			}
		}
	}
	
	/**
	 * 开始分配数据
	 * 1. 先确认当前有多少个可以处理任务的节点
	 * 2. 把要处理的数据基本平均分配到所有节点上
	 * @param newData
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	private void publishNewData(NewData newData) throws KeeperException, InterruptedException {
		List<TaskData> _taskDatas = new ArrayList<TaskData>();   //存放分配好的任务数据
		List<String> nodes = zk.getChildren("/root/task", watcher);
		int nodeSize = nodes.size();
		List<String> list = NumberGroupUtil.groupNumber(newData.getDataIds(), nodeSize);
		if(list != null && list.size() > 0) {
			//这组数据需要统一id，以供后续查找
			for (int i=0;i<list.size();i++) {
				TaskData taskData = new TaskData();
				taskData.setId(nodes.get(i)+"_"+System.currentTimeMillis());
				taskData.setTaskId(newData.getTaskId());
				taskData.setNodeName(nodes.get(i));
				taskData.setDataIds(list.get(i));
				taskData.setStatus(Constant.STATUS_TASKDATA_NOSTART);
				taskData.setCreatetime(new Date());
				taskData.setVersion(newData.getVersion());
				_taskDatas.add(taskData);
			}
		}
		addTasks(_taskDatas);
	}

	/**
	 * 把分配好的任务加入到/root/task下
	 * @param _taskDatas
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	private void addTasks(List<TaskData> _taskDatas) throws KeeperException, InterruptedException {
		try {
			Stat stat = zk.exists("/root/task", watcher);
			List<TaskData> taskDatas = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/task", watcher, null));
			if(taskDatas == null) {
				taskDatas = new ArrayList<TaskData>();
			}
			taskDatas.addAll(_taskDatas);
			zk.setData("/root/task", ByteUtil.objectToByte(taskDatas), stat.getVersion());
		} catch (KeeperException.BadVersionException e) {
			// 如果发生版本错误的异常
			System.out.println("出现版本异常" + e.getMessage());
			addTasks(_taskDatas);
		}
	}
}
