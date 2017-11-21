package com.bj58.lbg.zk_task.schedule.service;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ScheduleService implements Runnable{

	protected ZooKeeper zk;
	protected Watcher watcher;
	protected String schedulePath;
	protected String taskPath;
	
	public ScheduleService(ZooKeeper zk, Watcher watcher, String schedulePath, String taskPath) {
		super();
		this.watcher = watcher;
		this.zk = zk;
		this.schedulePath = schedulePath;
		this.taskPath = taskPath;
	}

	public void run() {}
}
