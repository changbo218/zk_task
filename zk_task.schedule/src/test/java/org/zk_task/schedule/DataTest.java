package org.zk_task.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;

public class DataTest {
	private long taskId;		//本次任务的编号id，用于关联执行taskData的taskid
	private String dataIds;		//要执行的所有id范围  1-6000000
	private int status;  		//标志，用于判断新数据有没有分配过 0没有， 1有
	private int version;		//版本，因为未来错误的任务id同样会放到这个newData里，所以这里设置版本来记录id被执行了多少次，当处理N次还不成功的时候，可以做一些其他的处理
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
		int sessionTimeout = 5000;
		ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, null);
//		List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", false, null));
		List<NewData> newDataList = new ArrayList<NewData>();
		NewData newData = new NewData();
		newData.setTaskId(System.currentTimeMillis());
		newData.setDataIds("5734312-11656983");
		newData.setStatus(Constant.STATUS_NEWDATA_NOT_PUBLISH);
		newData.setVersion(1);
		newDataList.add(newData);
		Stat stat = zk.setData("/root/newdata", ByteUtil.objectToByte(newDataList) , -1);
	}
	
	public static void main2(String[] args) throws IOException, KeeperException, InterruptedException {
		String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
		int sessionTimeout = 5000;
		ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, null);
		List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData("/root/newdata", false, null));
		System.out.println(newDataList);
	}
	
	public static void main5(String[] args) throws IOException, KeeperException, InterruptedException {
		String hosts = "112.124.116.226:2181,112.124.116.226:2182,112.124.116.226:2183";
		int sessionTimeout = 5000;
		ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, null);
		//List<TaskData> taskDataList = (List<TaskData>) ByteUtil.byteToObject(zk.getData("/root/newdata", false, null));
		List<TaskData> taskDataList = new ArrayList<TaskData>();
		TaskData taskData = new TaskData();
		taskData.setTaskId(System.currentTimeMillis());
		taskData.setDataIds("1-10");
		taskData.setStatus(Constant.STATUS_TASKDATA_NOSTART);
		taskData.setVersion(1);
		taskDataList.add(taskData);
		Stat stat = zk.setData("/root/task", ByteUtil.objectToByte(taskDataList) , -1);
	}
}
