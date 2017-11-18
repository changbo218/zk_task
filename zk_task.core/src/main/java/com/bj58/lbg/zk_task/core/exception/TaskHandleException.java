package com.bj58.lbg.zk_task.core.exception;

/**
 * 任务执行失败异常
 * 业务代码执行失败抛出此异常，可以让上层代码知道该条id需重试
 * @author 常博
 *
 */
public class TaskHandleException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7968849136174484449L;

	
}
