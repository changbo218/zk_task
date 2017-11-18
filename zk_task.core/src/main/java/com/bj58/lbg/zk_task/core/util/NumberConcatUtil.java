package com.bj58.lbg.zk_task.core.util;

/**
 * 数字拼接
 * 把 3,4,5,6,9 拼接成3-6,9
 * @author 常博
 *
 */
public class NumberConcatUtil {

	public static String concatNumber(String original, String current) {
		if(current != null && current.length() > 0) {
			if(current.contains(",")) {
				String[] strArr = current.split(",");
				for (String data : strArr) {
					if(data.contains("-")) {
						int left = Integer.valueOf(data.split("-")[0]);
						int right = Integer.valueOf(data.split("-")[1]);
						for (int i = left; i <= right; i++) {
							original = concatNumber(original, i);
						}
					} else {
						original = concatNumber(original, Integer.valueOf(data));
					}
				}
			} else {
				if(current.contains("-")) {
					int left = Integer.valueOf(current.split("-")[0]);
					int right = Integer.valueOf(current.split("-")[1]);
					for (int i = left; i <= right; i++) {
						original = concatNumber(original, i);
					}
				} else {
					original = concatNumber(original, Integer.valueOf(current));
				}
			}
		}
		return original;
	}
	
	public static String concatNumber(String original, Integer current) {
		if(original !=null && original.length() > 0) {
			if(!original.contains(",")) {
				//如  3-7 或  4
				if(original.contains("-")) {
					// 3-7
					Integer low = Integer.valueOf(original.split("-")[0]);
					Integer high = Integer.valueOf(original.split("-")[1]);
					if(current < low) {
						if(current == low - 1) {
							return current+"-"+high;
						} else {
							return current+","+original;
						}
					} else if(current > high){
						if(current == high + 1) {
							return low + "-" + current;
						} else {
							return original + ","+current;
						}
					}
					
				} else {
					// 4
					Integer ori = Integer.valueOf(original);
					if(current < ori) {
						if(current == ori - 1) {
							return current+"-"+ori;
						} else {
							return current +","+ori;
						}
					} else if(current > ori){
						if(current == ori+1) {
							return ori + "-" + current;
						} else {
							return ori + "," + current;
						}
					} else {
						return original;
					}
				}
			} else {
				// 3,11-22,34,55,67-199
				String[] strArr = original.split(",");
				// 要先判断current应该保存的位置
				// 1. 判断是不是属于最左边，或者是最右边
				String lower = strArr[0];
				String higher = strArr[strArr.length - 1];
				Integer minInt = getLeftInt(lower);
				Integer maxInt = getRightInt(higher);
				if(current == minInt || current == maxInt) return original;
				if(current < minInt) {
					//向左补充
					return leftMerge(current, original, lower, minInt);
				} else if(current > maxInt) {
					//向右补充
					return rightMerge(current, original, higher, maxInt);
				}
				//2. 判断是否已经显示的包含，比如就是3,11,22,34这样的数，有一下几种情况  ,current- ; -current, ; ,current,  如果显示包含就直接返回
				if(original.contains(","+current+"-") || original.contains("-"+current+",") || original.contains(","+original+",")) return original;
				//3. 判断current 是不是出现在 n-n中, 如果是，则直接返回
				for (String part : strArr) {
					if(part.contains("-")) {
						Integer left = Integer.valueOf(part.split("-")[0]);
						Integer right = Integer.valueOf(part.split("-")[1]);
						if(current >= left && current <= right) {
							return original;
						}
					}
				}
				//4. 找到current应在的位置
				for (int i=0;i<strArr.length-1;i++) {
					int j = i+1;
					Integer lowRight = getRightInt(strArr[i]);
					Integer higtLeft = getLeftInt(strArr[j]);
					if(current > lowRight && current < higtLeft) {
						//找到这个位置
						if(current == lowRight + 1 && current != higtLeft - 1) {
							//临界左
							if(!strArr[i].contains("-")) {
								//不含-  如 ,3, current=4
								if(i == 0) {
									//i==0说明是在第一个位置后面
									String substring = original.substring(original.indexOf(","), original.length());
									return strArr[i]+"-"+current+substring;
								} else {
									//后面的则可以用两个逗号来直接替换
									return original.replace(","+strArr[i]+",", ","+strArr[i]+"-"+current+",");
								}
							} else {
								// 包含-， 如1-4, current=5
								return original.replace("-"+lowRight+",", "-"+current+",");
							}
						} else if(current == higtLeft - 1 && current != lowRight + 1) {
							//临界右
							if(!strArr[j].contains("-")) {
								//不含-， 如   1-2,7  current=6
								if(j == strArr.length-1) {
									//j==strArr.length-1说明是在最后一个位置后面
									String substring = original.substring(0, original.lastIndexOf(",")+1);
									return substring+current+"-"+strArr[j];
								} else {
									return original.replace(","+strArr[j]+",", ","+current+"-"+strArr[j]+",");
								}
							} else {
								// 包含-， 如  1,5-7,9  current=4
								return original.replace(","+higtLeft+"-", ","+current+"-");
							}
						} else if(current == higtLeft - 1 && current == lowRight + 1) {
							//既临界左还临界右
							if(strArr.length == 2) {
								// 如果长度是2 ，  如   7,9 current是8
								return getLeftInt(strArr[i])+"-"+getRightInt(strArr[j]);
							}
							else if(i == 0) {
								//i==0说明是在第一个位置后面
								return original.replace(strArr[i]+","+strArr[j]+",", getLeftInt(strArr[i])+"-"+getRightInt(strArr[j])+",");
							}
							else if(j == strArr.length - 1) {
								//j==strArr.length-1说明是在最后一个位置后面
								return original.replace(","+strArr[i]+","+strArr[j], ","+getLeftInt(strArr[i])+"-"+getRightInt(strArr[j]));
							}
							else {
								//i,j 都处在中间位置
								return original.replace(","+strArr[i]+","+strArr[j]+",", ","+getLeftInt(strArr[i])+"-"+getRightInt(strArr[j])+",");
							}
						} else if(current != higtLeft - 1 && current != lowRight + 1) {
							//既不临界左也不临界右
							if(strArr.length == 2) {
								// 如果长度是2 ，  如   5-7,13 current是10
								return strArr[i]+","+current+","+strArr[j];
							}
							else if(i == 0) {
								//i==0说明是在第一个位置后面
								return original.replace(strArr[i]+","+strArr[j]+",", strArr[i]+","+current+","+strArr[j]+",");
							}
							else if(j == strArr.length - 1) {
								//j==strArr.length-1说明是在最后一个位置后面
								return original.replace(","+strArr[i]+","+strArr[j], ","+strArr[i]+","+current+","+strArr[j]);
							}
							else {
								//i,j 都处在中间位置
								return original.replace(","+strArr[i]+","+strArr[j]+",", ","+strArr[i]+","+current+","+strArr[j]+",");
							}
						}
					}
				}
			}
			return original;
		} else {
			return String.valueOf(current);
		}
	}

	/**
	 * current 向右补充
	 * @param current
	 * @param original
	 * @return
	 */
	private static String rightMerge(Integer current, String original, String higher, Integer maxInt) {
		if(!higher.contains("-")) {
			// ,4
			if(current == maxInt+1) {
				return original + "-"+current;
			} else {
				return original + "," + current;
			}
		} else {
			//, 4-7
			if(current == maxInt+1) {
				String substring = original.substring(0, original.lastIndexOf("-")+1);
				return substring + current;
			} else {
				return original + "," + current;
			}
		}
	}

	/**
	 * current 向左补充
	 * @param current
	 * @param original
	 * @return
	 */
	private static String leftMerge(Integer current, String original, String lower, Integer minInt) {
		if(!lower.contains("-")) {
			// 3,
			if(current == minInt-1) {
				return current + "-"+ original;
			} else {
				return current + "," + original;
			}
		} else {
			//4-7, 
			if(current == minInt-1) {
				String substring = original.substring(original.indexOf("-"), original.length());
				return current + substring;
			} else {
				return current + "," + original;
			}
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
		String s = NumberConcatUtil.concatNumber("2-4,6,9-15,22,24,28,30-31,44-59", "29-42,46,54-70");
		long t2 = System.currentTimeMillis();
		System.out.println(s + "    "+(t2-t1));
		System.out.println(Integer.MAX_VALUE);
	}
}
