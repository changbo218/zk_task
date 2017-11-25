package com.bj58.lbg.zk_write;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.PropertiesUtil;
import com.bj58.lbg.zk_write.service.TaskWriterService;
import com.bj58.lbg.zk_write.utils.ZookeeperWriterUtil;
import com.bj58.lbg.zk_write.waterer.WriterWatcher;

public class TaskWriterHandler {

	private static String schedulePath;
	private static TaskWriterService service;
	
	/**
	 * 默认路径写入初始化链接
	 */
	private static void initConnect()  {
		String rootPath = Constant.DEFAULT_ROOT_PATH;
		schedulePath = rootPath+Constant.DEFAULT_SCHEDULE_PATH;
		initConnect(rootPath, schedulePath);
	}
	
	/**
	 * 指定路径写入初始化
	 * @param pathIndex 配置文件的索引位置
	 */
	private static void initConnect(int pathIndex)  {
		String rootPath = PropertiesUtil.rootPath;
		schedulePath = PropertiesUtil.schedulePaths.get(pathIndex);
		initConnect(rootPath, schedulePath);
	}
	
	/**
	 * 写入初始化
	 * @param rootPath  根路径
	 * @param schedulePath  调度路径
	 */
	private static void initConnect(String rootPath, String schedulePath) {
		try {
			CountDownLatch countDownLatch = new CountDownLatch(1);
			WriterWatcher watcher = new WriterWatcher(countDownLatch);
			ZookeeperWriterUtil.init(watcher, countDownLatch);
			ZooKeeper zk = ZookeeperWriterUtil.getZookeeper();
			if(zk.exists(rootPath, false) == null) {
				zk.create(rootPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if(zk.exists(schedulePath, false) == null) {
				zk.create(schedulePath, ByteUtil.objectToByte(new ArrayList<NewData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			service = new TaskWriterService(schedulePath);
			System.out.println("写入准备工作完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static void closeConnect() {
		ZookeeperWriterUtil.closeConnect();
	}
	
	/**
	 * 写入数据
	 * @param ids 要写入的ids
	 * @param cover 是否覆盖掉之前的数据   true表示覆盖，false表示添加
	 * @throws Exception 
	 */
	public static void write(List<Long> dataIds, boolean cover) throws Exception {
		initConnect();
		service.write(dataIds, cover);
		closeConnect();
	}
	
	/**
	 * 写入数据
	 * @param ids 要写入的ids
	 * @param cover 是否覆盖掉之前的数据   true表示覆盖，false表示添加
	 * @throws Exception 
	 */
	public static void write(int path_index, List<Long> dataIds, boolean cover) throws Exception {
		initConnect(path_index);
		service.write(dataIds, cover);
		closeConnect();
	}

}
