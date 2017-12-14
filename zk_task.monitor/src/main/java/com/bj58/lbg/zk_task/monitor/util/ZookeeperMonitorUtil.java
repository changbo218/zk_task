package com.bj58.lbg.zk_task.monitor.util;

import java.io.IOException;

import org.apache.zookeeper.ZooKeeper;

public class ZookeeperMonitorUtil {

	private static volatile ZooKeeper zk;
	static String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
	private static int sessionTimeout = 50000;
	
	public static void init() {
		if(zk == null) {
			synchronized (ZookeeperMonitorUtil.class) {
				if(zk == null) {
					try {
						zk = new ZooKeeper(hosts, sessionTimeout, new MonitorsWatcher());
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
