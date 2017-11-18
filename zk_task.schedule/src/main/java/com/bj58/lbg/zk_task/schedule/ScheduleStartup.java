package com.bj58.lbg.zk_task.schedule;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.schedule.compensation.CompensationHandler;
import com.bj58.lbg.zk_task.schedule.util.ZookeeperScheduleUtil;
import com.bj58.lbg.zk_task.schedule.watcher.ScheduleWatcher;

public class ScheduleStartup {

	public static void main(String[] args) throws KeeperException, InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		ScheduleWatcher watcher = new ScheduleWatcher(countDownLatch);
		ZookeeperScheduleUtil.init(watcher);
		ZooKeeper zk = ZookeeperScheduleUtil.getZookeeper();
		if(zk.exists("/root", watcher) == null) {
			zk.create("/root", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		if(zk.exists("/root/newdata", watcher) == null) {
			zk.create("/root/newdata", ByteUtil.objectToByte(new ArrayList<NewData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		zk.create("/root/newdata/schedule_node", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		zk.getChildren("/root/task", watcher);
		CompensationHandler handler = new CompensationHandler(zk, watcher);
		handler.dataCompensation();
		Thread.sleep(Integer.MAX_VALUE);
	}
	
}
