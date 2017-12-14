package com.bj58.lbg.zk_task.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import com.bj58.lbg.zk_task.monitor.util.ZookeeperMonitorUtil;

@SpringBootApplication
@RestController
public class MonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitorApplication.class, args);
		ZookeeperMonitorUtil.init();
	}
	
}
