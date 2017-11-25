package com.bj58.lbg.zk_write.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.bj58.lbg.zk_task.core.entity.NewData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.NumberConcatUtil;
import com.bj58.lbg.zk_write.utils.ZookeeperWriterUtil;

public class TaskWriterService {
	
	private String schedulePath;
	
	public TaskWriterService(String schedulePath) {
		this.schedulePath = schedulePath;
	}

	/**
	 * 写入数据
	 * @param ids 要写入的ids
	 * @param cover 是否覆盖掉之前的数据   true表示覆盖，false表示添加
	 * @throws Exception 
	 */
	public void write(List<Long> dataIds, boolean cover) throws Exception {
		String ids = "";
		if(dataIds != null && dataIds.size() > 0) {
			for (long id : dataIds) {
				ids = NumberConcatUtil.concatNumber(ids, id);
			}
		}
		write(ids, cover);
	}
	
	private void write(String ids, boolean cover) throws Exception {
		if(cover) {
			setNewData(ids);
		} else {
			appendNewData(ids);
		}
	}

	private void setNewData(String ids) throws Exception {
		ZooKeeper zk = ZookeeperWriterUtil.getZookeeper();
		List<NewData> newDataList = new ArrayList<NewData>();
		NewData newData = new NewData();
		newData.setTaskId(System.currentTimeMillis());
		newData.setDataIds(ids);
		newData.setStatus(Constant.STATUS_NEWDATA_NOT_PUBLISH);
		newData.setVersion(1);
		newDataList.add(newData);
		zk.setData(schedulePath, ByteUtil.objectToByte(newDataList), -1);
	}

	private void appendNewData(String ids) throws KeeperException, InterruptedException {
		ZooKeeper zk = ZookeeperWriterUtil.getZookeeper();
		Stat stat = zk.exists(schedulePath, false);
		List<NewData> newDataList = (List<NewData>) ByteUtil.byteToObject(zk.getData(schedulePath, false, null));
		NewData newData = new NewData();
		newData.setTaskId(System.currentTimeMillis());
		newData.setDataIds(ids);
		newData.setStatus(Constant.STATUS_NEWDATA_NOT_PUBLISH);
		newData.setVersion(1);
		newDataList.add(newData);
		try {
			zk.setData(schedulePath, ByteUtil.objectToByte(newDataList), stat.getVersion());
		} catch (KeeperException.BadVersionException e) {
			appendNewData(ids);
		}
	}
}
