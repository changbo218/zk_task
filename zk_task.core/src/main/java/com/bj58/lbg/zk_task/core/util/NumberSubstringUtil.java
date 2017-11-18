package com.bj58.lbg.zk_task.core.util;

/**
 * 数据去除
 * 3-6,9 去掉 4 拼接成  3,5-6,9
 * @author 常博
 *
 */
public class NumberSubstringUtil {

	public static String substringNumber(String original, String current) {
		if(current != null && current.length() > 0) {
			if(current.contains(",")) {
				String[] strArr = current.split(",");
				for (String data : strArr) {
					if(data.contains("-")) {
						int left = Integer.valueOf(data.split("-")[0]);
						int right = Integer.valueOf(data.split("-")[1]);
						for (int i = left; i <= right; i++) {
							original = substringNumber(original, i);
						}
					} else {
						original = substringNumber(original, Integer.valueOf(data));
					}
				}
			} else {
				if(current.contains("-")) {
					int left = Integer.valueOf(current.split("-")[0]);
					int right = Integer.valueOf(current.split("-")[1]);
					for (int i = left; i <= right; i++) {
						original = substringNumber(original, i);
					}
				} else {
					original = substringNumber(original, Integer.valueOf(current));
				}
			}
		}
		return original;
	}
	
	public static String substringNumber(String original, Integer current) {
		if(original !=null && original.length() > 0) {
			if(!original.contains(",")) {
				//如  3-7 或  4
				if(original.contains("-")) {
					// 3-7
					Integer low = Integer.valueOf(original.split("-")[0]);
					Integer high = Integer.valueOf(original.split("-")[1]);
					if(low == current) {  //current = 3
						if(current + 1 == high) {   //3-4
							return high+"";
						} else {					//3-5
							return (low+1)+"-"+high;
						}
					}
					else if(high == current) { 		// current = 7
						if(current - 1 == low) {// 6-7
							return low + "";
						} else {				// 3-7
							return low+"-"+(high-1);
						}
					} else if(low < current && current < high) {
						if(low + 1 == current && current + 1 == high) { // 3-5  current=4
							return low +","+high;
						} else if(low + 1 == current && current + 1 < high) {  // 3-7 c=4
							return low + "," + (current + 1) + "-" + high;
						} else if(low + 1 < current && current + 1 == high) {  // 3-7 c=6
							return low+"-"+(current-1)+","+high;
						} else if(low + 1 < current && current + 1 < high) {   // 3-9 c=6
							return low+"-"+(current-1)+","+(current+1)+"-"+high;
						}
					}
				} else {
					// 4
					Integer ori = Integer.valueOf(original);
					if(current == ori) {
						return "";
					} else {
						return original;
					}
				}
			} else {
				//包含,
				// 3,11-22,34,55,67-199
				String[] strArr = original.split(",");
				// 遍历每一项数据，如果是数字则直接比相等，如果是范围，则直接判断是不是属于此范围
				for (int i=0;i<strArr.length;i++) {
					if(!strArr[i].contains("-")) {
						//不包含-，则是数字
						if(Integer.valueOf(strArr[i]) == current) {
							if(i == 0) {
								return original.substring(original.indexOf(",")+1, original.length());
							} else if(i == strArr.length - 1) {
								return original.substring(0, original.lastIndexOf(","));
							} else {
								return original.replace(","+current+",", ",");
							}
						}
					} else {
						//包含-，是范围  3-7
						Integer low = getLeftInt(strArr[i]);
						Integer high = getRightInt(strArr[i]);
						if(low == current) {  //current = 3
							if(current + 1 == high) {   //3-4
								if(i == 0) {	//第一个值
									return original.substring(original.indexOf("-")+1, original.length());
								} else if(i == strArr.length - 1) {
									return original.substring(0, original.lastIndexOf(",")+1)+high;
								} else {
									return original.replace(","+strArr[i]+",", ","+high+",");
								}
							} else {					//3-5
								if(i == 0) {
									return original.replace(strArr[i]+",", (current+1)+"-"+high+",");
								} else if(i == strArr.length - 1) {
									return original.replace(","+strArr[i], ","+(current+1)+"-"+high);
								} else {
									return original.replace(","+strArr[i]+",", ","+(current+1)+"-"+high+",");
								}
							}
						}
						else if(high == current) { 		// current = 7
							if(current - 1 == low) {// 6-7
								if(i == 0) {	//第一个值
									return low+original.substring(original.indexOf(","),original.length());
								} else if(i == strArr.length - 1) {
									return original.substring(0, original.lastIndexOf(",")+1)+low;
								} else {
									return original.replace(","+strArr[i]+",", ","+low+",");
								}
							} else {				// 3-7
								if(i == 0) {
									return original.replace(strArr[i]+",", low+"-"+(high-1)+",");
								} else if(i == strArr.length - 1) {
									return original.replace(","+strArr[i], ","+low+"-"+(high-1));
								} else {
									return original.replace(","+strArr[i]+",", ","+low+"-"+(high-1)+",");
								}
							}
						} else if(low < current && current < high) {   
							//找到这个位置
							if(current == low + 1 && current != high - 1) {  //3-7  c=4
								//临界左
								if(i == 0) { //第一个
									return original.replace(strArr[i]+",", low+","+(current+1)+"-"+high+",");
								} else if(i == strArr.length - 1) { //最后一个
									return original.replace(","+strArr[i], ","+low+","+(current+1)+"-"+high);
								} else {
									return original.replace(","+strArr[i]+",", ","+low+","+(current+1)+"-"+high+",");
								}
							} else if(current == high - 1 && current != low + 1) {
								//临界右
								if(i == 0) { //第一个
									return original.replace(strArr[i]+",", low+"-"+(current-1)+","+high+",");
								} else if(i == strArr.length - 1) { //最后一个
									return original.replace(","+strArr[i], ","+low+"-"+(current-1)+","+high);
								} else {
									return original.replace(","+strArr[i]+",", ","+low+"-"+(current-1)+","+high+",");
								}
							} else if(current == high - 1 && current == low + 1) {
								//既临界左还临界右
								if(i == 0) { //第一个
									return original.replace(strArr[i]+",", low+","+high+",");
								} else if(i == strArr.length - 1) { //最后一个
									return original.replace(","+strArr[i], ","+low+","+high);
								} else {
									return original.replace(","+strArr[i]+",", ","+low+","+high+",");
								}
							} else if(current != high - 1 && current != low + 1) {
								//既不临界左也不临界右
								if(i == 0) { //第一个
									return original.replace(strArr[i]+",", low+"-"+(current-1)+","+(current+1)+"-"+high+",");
								} else if(i == strArr.length - 1) { //最后一个
									return original.replace(","+strArr[i], ","+low+"-"+(current-1)+","+(current+1)+"-"+high);
								} else {
									return original.replace(","+strArr[i]+",", ","+low+"-"+(current-1)+","+(current+1)+"-"+high+",");
								}
							}
						}
					}
				}
			}
			return original;
		} else {
			return "";
		}
	}



	/**
	 * 返回最小值
	 * @param lower
	 * @return
	 */
	private static Integer getLeftInt(String str) {
		if(str!=null && str.contains("-")) {
			return Integer.valueOf(str.split("-")[0]);
		}
		return Integer.valueOf(str);
	}
	
	/**
	 * 返回最小值
	 * @param lower
	 * @return
	 */
	private static Integer getRightInt(String str) {
		if(str!=null && str.contains("-")) {
			return Integer.valueOf(str.split("-")[1]);
		}
		return Integer.valueOf(str);
	}

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		String s = NumberSubstringUtil.substringNumber("6,9-15,22,24,28", "6,28");
		long t2 = System.currentTimeMillis();
		System.out.println(s + "    "+(t2-t1));
		System.out.println(55/4);
	}
}
