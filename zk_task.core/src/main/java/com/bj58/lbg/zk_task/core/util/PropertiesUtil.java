package com.bj58.lbg.zk_task.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class PropertiesUtil {

	private static Properties properties = new Properties();
	public static List<String> taskPaths = new ArrayList<String>();
	public static List<String> schedulePaths = new ArrayList<String>();
	public static String rootPath;
	
	public static void initProperties(String filename) {
		InputStream fis =PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);    
		try {
			properties.load(fis);
			initRootPath();
			initTaskPath();
			initSchedulePath();
		} catch (IOException e) {
			System.out.println("properties 配置文件找不到:" +filename);
			e.printStackTrace();
		}    
	}
	
	private static void initRootPath() {
		rootPath = properties.getProperty("root_path");
	}

	private static void initTaskPath() {
		int i=0;
		while(true) {
			String task_path = properties.getProperty("task_path_"+i);
			if(StringUtils.isNotBlank(task_path)) {
				taskPaths.add(rootPath+task_path);
			} else {
				break;
			}
			i++;
		}
	}

	private static void initSchedulePath() {
		int i=0;
		while(true) {
			String schedule_path = properties.getProperty("schedule_path_"+i);
			if(StringUtils.isNotBlank(schedule_path)) {
				schedulePaths.add(rootPath+schedule_path);
			} else {
				break;
			}
			i++;
		}
	}

}
