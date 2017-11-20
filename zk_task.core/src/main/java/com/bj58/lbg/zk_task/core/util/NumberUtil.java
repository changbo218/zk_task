package com.bj58.lbg.zk_task.core.util;

public class NumberUtil {

	/**
	 * 返回最小值
	 * @param lower
	 * @return
	 */
	protected static long getLeft(String str) {
		if(str!=null && str.contains("-")) {
			return Long.valueOf(str.split("-")[0]);
		}
		return Long.valueOf(str);
	}
	
	/**
	 * 返回最大值
	 * @param lower
	 * @return
	 */
	protected static long getRight(String str) {
		if(str!=null && str.contains("-")) {
			return Long.valueOf(str.split("-")[1]);
		}
		return Long.valueOf(str);
	}

}
