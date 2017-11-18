package com.bj58.lbg.zk_task.task.util;

import java.io.IOException;

import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.task.watcher.TaskWatcher;

public class ZookeeperTaskUtil {

	private static volatile ZooKeeper zk;
	static String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
	private static int sessionTimeout = 50000;
	
	public static void init(TaskWatcher watcher) {
		if(zk == null) {
			synchronized (ZookeeperTaskUtil.class) {
				if(zk == null) {
					try {
						zk = new ZooKeeper(hosts, sessionTimeout, watcher);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static ZooKeeper getZookeeper() {
		return zk;
	}
	
}
