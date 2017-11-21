package com.bj58.lbg.zk_task.schedule.watcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.bj58.lbg.zk_task.schedule.service.ScheduleDataChangeService;
import com.bj58.lbg.zk_task.schedule.service.ScheduleTaskNodeChangeService;
import com.bj58.lbg.zk_task.schedule.util.ZookeeperScheduleUtil;

public class ScheduleWatcher implements Watcher {

	private ExecutorService pool = Executors.newCachedThreadPool();
	private String schedulePath;
	private String taskPath;
	
	public ScheduleWatcher(String schedulePath, String taskPath) {
		this.schedulePath = schedulePath;
		this.taskPath = taskPath;
	}
	
	/**
	 * 该方法是一个阻塞方法
	 */
	public void process(WatchedEvent event) {
		System.out.println("ScheduleWatcher event " + event.getPath() + " " + event.getType() + " " + event.getState());
		if(event.getType() == EventType.NodeDataChanged) {
			if(event.getPath().equals(schedulePath)) {
				//处理调度节点下的数据变化事件
				pool.execute(new ScheduleDataChangeService(ZookeeperScheduleUtil.getZookeeper(), this, schedulePath, taskPath));
			}
		}
		if(event.getType() == EventType.NodeChildrenChanged) {
			if(event.getPath().equals(taskPath)) {
				//处理任务子节点发生变化的事件
				pool.execute(new ScheduleTaskNodeChangeService(ZookeeperScheduleUtil.getZookeeper(), this, schedulePath, taskPath));
			}
		}
	}
	
}
