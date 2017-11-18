package com.bj58.lbg.zk_task.task;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.task.service.TaskService;
import com.bj58.lbg.zk_task.task.util.ZookeeperTaskUtil;
import com.bj58.lbg.zk_task.task.watcher.TaskWatcher;

public class TaskExecute {

//	public static void main(String[] args) throws KeeperException, InterruptedException {
//		CountDownLatch countDownLatch = new CountDownLatch(1);
//		TaskWatcher watcher = new TaskWatcher(countDownLatch);
//		ZookeeperTaskUtil.init(watcher);
//		ZooKeeper zk = ZookeeperTaskUtil.getZookeeper();
//		if(zk.exists("/root", watcher) == null) {
//			zk.create("/root", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//		}
//		if(zk.exists("/root/task", watcher) == null) {
//			zk.create("/root/task", ByteUtil.objectToByte(new ArrayList<TaskData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//		}
//		String nodeName = zk.create("/root/task/task_node", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
//		System.out.println(nodeName);
//		watcher.setNodeName(nodeName.substring(nodeName.lastIndexOf("/")+1, nodeName.length()));
//		countDownLatch.countDown();
//		Thread.sleep(Integer.MAX_VALUE);
//	}
	
	public static void startup(TaskService taskService) {
		try {
			CountDownLatch countDownLatch = new CountDownLatch(1);
			TaskWatcher watcher = new TaskWatcher(countDownLatch, taskService);
			ZookeeperTaskUtil.init(watcher);
			ZooKeeper zk = ZookeeperTaskUtil.getZookeeper();
			if(zk.exists("/root", watcher) == null) {
				zk.create("/root", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if(zk.exists("/root/task", watcher) == null) {
				zk.create("/root/task", ByteUtil.objectToByte(new ArrayList<TaskData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String nodeName = zk.create("/root/task/task_node", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println(nodeName);
			watcher.setNodeName(nodeName.substring(nodeName.lastIndexOf("/")+1, nodeName.length()));
			countDownLatch.countDown();
			while(true) {
				Thread.sleep(Integer.MAX_VALUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("任务节点启动失败");
		} finally {
		}
		
	}
}
