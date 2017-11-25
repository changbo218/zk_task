package com.bj58.lbg.zk_demo;

import com.bj58.lbg.zk_task.core.exception.TaskHandleException;
import com.bj58.lbg.zk_task.core.util.PropertiesUtil;
import com.bj58.lbg.zk_task.task.TaskExecute;
import com.bj58.lbg.zk_task.task.service.TaskService;

/**
 * 任务节点客户端
 * @author 常博
 *
 */
public class TaskClient {

//	public static void main(String[] args) {
//		TaskExecute.startup(new TestTaskService());
//	}
	
	public static void main(String[] args) {
		PropertiesUtil.initProperties("config/zk_path.properties");
		TaskExecute.startup(1, new TestTaskService());
	}
}

class TestTaskService extends TaskService {

	@Override
	public void doAction(long id, int version) throws TaskHandleException {
		System.out.println("当前处理的数据id: " + id + " 版本：" + version);
	}
	
}
