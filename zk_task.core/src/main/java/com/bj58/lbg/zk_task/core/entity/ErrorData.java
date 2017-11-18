package com.bj58.lbg.zk_task.core.entity;

import java.io.Serializable;

public class ErrorData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8322032289860688310L;
	private String errorIds; 	//执行错误的id节点
	private int status;
	public String getErrorIds() {
		return errorIds;
	}
	public void setErrorIds(String errorIds) {
		this.errorIds = errorIds;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "ErrorData [errorIds=" + errorIds + ", status=" + status + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorIds == null) ? 0 : errorIds.hashCode());
		result = prime * result + status;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorData other = (ErrorData) obj;
		if (errorIds == null) {
			if (other.errorIds != null)
				return false;
		} else if (!errorIds.equals(other.errorIds))
			return false;
		if (status != other.status)
			return false;
		return true;
	}
	
}
