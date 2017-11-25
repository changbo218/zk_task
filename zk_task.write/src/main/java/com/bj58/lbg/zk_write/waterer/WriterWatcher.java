package com.bj58.lbg.zk_write.waterer;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class WriterWatcher implements Watcher {
	
	private CountDownLatch countDownLatch;
	
	public WriterWatcher(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	public void process(WatchedEvent event) {
		if(event.getState()==KeeperState.SyncConnected){
			countDownLatch.countDown();
		}
	}

}
