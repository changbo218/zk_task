package com.bj58.lbg.zk_task.core.entity;

import java.io.Serializable;

public class NewData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7816421982667783023L;
	private long taskId;		//本次任务的编号id，用于关联执行taskData的taskid
	private String dataIds;		//要执行的所有id范围  1-6000000
	private int status;  		//标志，用于判断新数据有没有分配过 0没有， 1有
	private int version;		//版本，因为未来错误的任务id同样会放到这个newData里，所以这里设置版本来记录id被执行了多少次，当处理N次还不成功的时候，可以做一些其他的处理
	
	public String getDataIds() {
		return dataIds;
	}
	public void setDataIds(String dataIds) {
		this.dataIds = dataIds;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getVersion() {
		return version;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "NewData [taskId=" + taskId + ", dataIds=" + dataIds + ", status=" + status + ", version=" + version
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataIds == null) ? 0 : dataIds.hashCode());
		result = prime * result + status;
		result = prime * result + (int) (taskId ^ (taskId >>> 32));
		result = prime * result + version;
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
		NewData other = (NewData) obj;
		if (dataIds == null) {
			if (other.dataIds != null)
				return false;
		} else if (!dataIds.equals(other.dataIds))
			return false;
		if (status != other.status)
			return false;
		if (taskId != other.taskId)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	
}
