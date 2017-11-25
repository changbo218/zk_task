package com.bj58.lbg.zk_task.schedule;

import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.PropertiesUtil;
import com.bj58.lbg.zk_task.schedule.compensation.CompensationHandler;
import com.bj58.lbg.zk_task.schedule.util.ZookeeperScheduleUtil;
import com.bj58.lbg.zk_task.schedule.watcher.ScheduleWatcher;

public class ScheduleExecute {

	public static void main(String[] args) throws KeeperException, InterruptedException {
		startup();
	}
	
	/**
	 * 默认路径启动
	 */
	public static void startup()  {
		String rootPath = Constant.DEFAULT_ROOT_PATH;
		String schedulePath = rootPath+Constant.DEFAULT_SCHEDULE_PATH;
		String taskPath = rootPath+Constant.DEFAULT_TASK_PATH;
		startup(rootPath, schedulePath, taskPath);
	}
	
	/**
	 * 指定路径启动
	 * @param pathIndex 配置文件的索引位置
	 */
	public static void startup(int pathIndex)  {
		String rootPath = PropertiesUtil.rootPath;
		String schedulePath = PropertiesUtil.schedulePaths.get(pathIndex);
		String taskPath = PropertiesUtil.taskPaths.get(pathIndex);
		startup(rootPath, schedulePath, taskPath);
	}
	
	/**
	 * 调度节点启动
	 * @param rootPath  根路径
	 * @param schedulePath  调度路径
	 * @param taskPath	任务路径
	 */
	private static void startup(String rootPath, String schedulePath, String taskPath) {
		try {
			ScheduleWatcher watcher = new ScheduleWatcher(schedulePath, taskPath);
			ZookeeperScheduleUtil.init(watcher);
			ZooKeeper zk = ZookeeperScheduleUtil.getZookeeper();
			if(zk.exists(rootPath, watcher) == null) {
				zk.create(rootPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if(zk.exists(schedulePath, watcher) == null) {
				zk.create(schedulePath, ByteUtil.objectToByte(new ArrayList<NewData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				//开启对schedulePath的监听
				zk.exists(schedulePath, watcher);
			}
			String nodeName = zk.create(schedulePath+"/schedule_node", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			zk.getChildren(taskPath, watcher);
			//数据补偿处理
			CompensationHandler handler = new CompensationHandler(zk, watcher, schedulePath, taskPath);
			handler.dataCompensation();
			System.out.println("调度节点"+nodeName+"启动完成");
			Thread.sleep(Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
