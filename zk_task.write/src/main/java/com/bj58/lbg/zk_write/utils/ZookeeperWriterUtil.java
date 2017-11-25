package com.bj58.lbg.zk_write.utils;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperWriterUtil {

	private static volatile ZooKeeper zk;
	static String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
	private static int sessionTimeout = 5000;
	
	public static void init(Watcher watcher, CountDownLatch countDownLatch) {
		if(zk == null) {
			synchronized (ZookeeperWriterUtil.class) {
				if(zk == null) {
					try {
						zk = new ZooKeeper(hosts, sessionTimeout, watcher);
						countDownLatch.await();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static ZooKeeper getZookeeper() {
		return zk;
	}
	
	public static void closeConnect() {
		try {
			if(zk != null) {
				zk.close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
