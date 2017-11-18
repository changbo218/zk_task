package com.bj58.lbg.zk_task.schedule.service;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ScheduleService implements Runnable{

	protected ZooKeeper zk;
	protected Watcher watcher;
	
	public ScheduleService(ZooKeeper zk, Watcher watcher) {
		super();
		this.watcher = watcher;
		this.zk = zk;
	}

	public void run() {}
}
