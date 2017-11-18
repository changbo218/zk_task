package com.bj58.lbg.zk_task.task;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.task.util.ZookeeperTaskUtil;
import com.bj58.lbg.zk_task.task.watcher.TaskWatcher;

public class TaskExecuteTest {

	@Test
	public void startup() throws KeeperException, InterruptedException {
		TestTaskService taskService = new TestTaskService();
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
		Thread.sleep(Integer.MAX_VALUE);
	}
}
