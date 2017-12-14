package com.bj58.lbg.zk_task.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.monitor.util.ZookeeperMonitorUtil;

@Controller
@RequestMapping("monitor")
public class MonitorController {

	@RequestMapping("/task/default")
	public String taskData(Model model) {
		ZooKeeper zk = ZookeeperMonitorUtil.getZookeeper();
		String taskPath = Constant.DEFAULT_ROOT_PATH + Constant.DEFAULT_TASK_PATH;
		try {
			List<TaskData> taskDataList = (List<TaskData>) ByteUtil.byteToObject(zk.getData(taskPath, null, null));
			if(taskDataList == null || taskDataList.size() == 0) {
				taskDataList = new ArrayList<>();
				TaskData taskData = new TaskData();
				taskData.setId("1");
				taskData.setNodeName("nodename1");
				taskData.setStatus(1);
				taskData.setTaskId(123);
				taskData.setVersion(0);
				taskData.setDataIds("123");
				taskData.setCreatetime(new Date());
				taskDataList.add(taskData);
			}
			System.out.println(taskDataList);
			model.addAttribute("taskDataList", taskDataList);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "task";
	}
}
