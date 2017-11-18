package com.bj58.lbg.zk_task.core.util;

import com.bj58.spat.memcached.MemcachedClient;

public class Test {
	public static void main(String[] args) {
		
		try {
//			MemcachedClient client = MemcachedClient.getInstrance("D:\\58ganji\\workspace_chq\\com.bj58.lbg.zookeeper.core\\config\\memcache.xml");
//			System.out.println(client);
			MemcacheComp.getMemcacheComp().set("1", 1, 10000);
			System.out.println(MemcacheComp.getMemcacheComp().get("1"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
