package com.bj58.lbg.zk_demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;

import com.bj58.lbg.zk_write.TaskWriterHandler;

public class Demo {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		List<Long> ids = new ArrayList<Long>();
		for(long i = 1; i<= 100000; i++) {
			ids.add(i);
		}
		try {
			TaskWriterHandler.write(ids, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	
