package com.bj58.lbg.zk_demo;

import com.bj58.lbg.zk_task.core.util.PropertiesUtil;
import com.bj58.lbg.zk_task.schedule.ScheduleExecute;

/**
 * 调度节点客户端
 * @author 常博
 *
 */
public class ScheduleClient {

//	public static void main(String[] args) {
//		ScheduleExecute.startup();
//	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		PropertiesUtil.initProperties("config/zk_path.properties");
		
		ScheduleExecute.startup(1);
	}
}
