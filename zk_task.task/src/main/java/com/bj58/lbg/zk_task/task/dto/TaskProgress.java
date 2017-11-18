package com.bj58.lbg.zk_task.task.dto;

import java.io.Serializable;

/**
 * 任务进度
 * @author 常博
 *
 */
public class TaskProgress implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1178919543753686756L;
	//执行任务成功的id
	private String successIds = "";
	//执行任务失败的id
	private String errorIds = "";
	
	public String getSuccessIds() {
		return successIds;
	}
	public void setSuccessIds(String successIds) {
		this.successIds = successIds;
	}
	public String getErrorIds() {
		return errorIds;
	}
	public void setErrorIds(String errorIds) {
		this.errorIds = errorIds;
	}
	
}
