package com.bj58.lbg.zk_task.core.util;

public class Constant {

	public static final int STATUS_TASKDATA_NOSTART = 0; // 任务未开始
	public static final int STATUS_TASKDATA_DOING = 1;	 // 任务处理中
	public static final int STATUS_TASKDATA_FINISH = 2;	 // 任务已完成
	
	public static final int STATUS_NEWDATA_NOT_PUBLISH = 0; //数据未分配
	public static final int STATUS_NEWDATA_PUBLISH = 1;		//数据已分配
	
	public static final String DEFAULT_ROOT_PATH = "/root"; //默认根路径
	public static final String DEFAULT_TASK_PATH = "/task"; //默认任务路径
	public static final String DEFAULT_SCHEDULE_PATH = "/schedule"; //默认任务路径
}
