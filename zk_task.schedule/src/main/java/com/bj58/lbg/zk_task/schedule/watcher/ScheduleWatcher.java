package com.bj58.lbg.zk_task.schedule.watcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.bj58.lbg.zk_task.schedule.service.ScheduleDataChangeService;
import com.bj58.lbg.zk_task.schedule.service.ScheduleTaskNodeChangeService;
import com.bj58.lbg.zk_task.schedule.util.ZookeeperScheduleUtil;

public class ScheduleWatcher implements Watcher {

	private CountDownLatch countDownLatch;
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public ScheduleWatcher(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}
	
	/**
	 * 当zk创建完成后，调用此方法开始监听
	 */
	public void openListener() {
		countDownLatch.countDown();
	}

	/**
	 * 该方法是一个阻塞方法
	 */
	public void process(WatchedEvent event) {
		System.out.println("ScheduleWatcher event " + event.getPath() + " " + event.getType() + " " + event.getState());
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(event.getType() == EventType.NodeDataChanged) {
			if(event.getPath().equals("/root/newdata")) {
				//处理newdata下的数据变化事件
				pool.execute(new ScheduleDataChangeService(ZookeeperScheduleUtil.getZookeeper(), this));
			}
		}
		if(event.getType() == EventType.NodeChildrenChanged) {
			if(event.getPath().equals("/root/task")) {
				//处理任务节点发生变化的事件
				pool.execute(new ScheduleTaskNodeChangeService(ZookeeperScheduleUtil.getZookeeper(), this));
			}
		}
	}
	
}
