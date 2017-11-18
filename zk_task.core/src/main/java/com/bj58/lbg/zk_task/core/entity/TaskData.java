package com.bj58.lbg.zk_task.core.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务列表
 * @author 常博
 *
 */
public class TaskData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6736758392991335267L;
	private String id;			//这个任务的独有id，可以唯一确定这个任务的id，默认处理节点的name+时间戳
	private long taskId;		 //对应NewData总任务的编号 默认取时间戳
	private String nodeName;  //节点编号  001
	private int status; 	 //0 未处理   1正在处理  2处理完成
	private String dataIds;	//要处理的数据范围 1-1500000
	private Date createtime;	// 创建时间
	private int version;	//版本，同newData分配过来的版本
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public String getDataIds() {
		return dataIds;
	}
	public void setDataIds(String dataIds) {
		this.dataIds = dataIds;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "TaskData [id=" + id + ", taskId=" + taskId + ", nodeName=" + nodeName + ", status=" + status
				+ ", dataIds=" + dataIds + ", createtime=" + createtime + ", version=" + version + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createtime == null) ? 0 : createtime.hashCode());
		result = prime * result + ((dataIds == null) ? 0 : dataIds.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
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
		TaskData other = (TaskData) obj;
		if (createtime == null) {
			if (other.createtime != null)
				return false;
		} else if (!createtime.equals(other.createtime))
			return false;
		if (dataIds == null) {
			if (other.dataIds != null)
				return false;
		} else if (!dataIds.equals(other.dataIds))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nodeName == null) {
			if (other.nodeName != null)
				return false;
		} else if (!nodeName.equals(other.nodeName))
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
