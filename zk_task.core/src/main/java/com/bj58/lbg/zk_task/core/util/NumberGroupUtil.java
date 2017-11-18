package com.bj58.lbg.zk_task.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据分组
 * @author 常博
 *
 */
public class NumberGroupUtil {
	/**
	 * 数据分组
	 * 该方法是把一个数字的字符串拆成size份，用于给task分配
	 * @param original 原数据
	 * @param size	分组的数量
	 * @return
	 */
	public static List<String> groupNumber(String original, int size) {
		if(original==null || original.length() == 0 || size <= 0) return null;
		List<String> list = new ArrayList<String>();
		int dataTotalCount = getDataTotalCount(original);	//总数据量
		//每份对应分得的数量
		int perCount = getPerCount(dataTotalCount, size);
		String[] strArr = original.split(",");
		int i=-1;
		loop: for(int j=1;j<=size;j++) {
			String data = "";	 
			int currentCount = 0;
			//如果j==size，表示只剩下最后的节点，这时只要把后面的补全就行了
			if(j==size && i<strArr.length - 1) {
				i++;
				for(int k=i;k<strArr.length;k++) {
					if(data.length() == 0) {
						data = strArr[k];
					} else {
						data = data +","+ strArr[k];
					}
				}
				list.add(data);
				break;
			}
			while(i<strArr.length-1) {
				i++;
				if(!strArr[i].contains("-")) { 
					//不含-，意味着是数字，每次currentCount加一即可
					currentCount++;
					if(currentCount <= perCount) {
						if(data.length() == 0) {
							data = strArr[i];
						} else {
							data = data +","+ strArr[i];
						}
						if(currentCount == perCount) {
							list.add(data);
							continue loop;
						}
					}
					//如果i的位置到最后，则直接返回
					if(i == strArr.length - 1) {
						list.add(data);
						continue loop;
					}
				} else {
					//包含-
					int count = getCount(strArr[i]);
					if(currentCount + count <= perCount) {
						//这段数字的范围可以全部覆盖
						currentCount = currentCount + count;
						if(data.length() == 0) {
							data = strArr[i];
						} else {
							data = data +","+ strArr[i];
						}
						if(currentCount == perCount) {
							list.add(data);
							continue loop;
						}
					} else if(currentCount + count > perCount) {
						//这段数据的范围不能完全覆盖
						int margin = perCount - currentCount;
						//这里面的margin一定会小于count， 且count最小一定是2，margin最小一定是1
						int left = getLeftInt(strArr[i]);
						int right = getRightInt(strArr[i]);
						//把左侧和右侧对应的数字范围计算出来
						//左侧添加到data中，
						//右侧赋值给strArr[i],即加到数组中,并且i--，把下标移到该数据之前
						String leftStr = "";
						String rightStr = "";
						if(count == 2) {  //3-4, 只需要3
							//这里的margin一定是1
							leftStr = left+"";
							rightStr = right+"";
						} else if(count > 2 && margin == 1) {  //3-5  要3   最左侧第一个
							leftStr = left+"";
							rightStr = (left+1)+"-"+right;
						} else if(count > 2 && (margin == count - 1)) {  // 3-5  要4及以前    最右侧倒数第二，因为不会有倒数第一的情况
							leftStr = left+"-"+(right-1);
							rightStr = right+"";
						} else {
							leftStr = left+"-"+(margin-1+left);
							rightStr = (margin+left)+"-"+right;
						}
						//处理右侧：把右侧设置回数组中，且i的位置减1
						strArr[i] = rightStr;
						i--;
						//处理左侧
						if(data.length() == 0) {
							data = leftStr;
						} else {
							data = data +","+ leftStr;
						}
						//执行到这段代码，说明一定达到了perCount，可以直接返回
						list.add(data);
						continue loop;
					}
					if(i == strArr.length - 1) {
						list.add(data);
						continue loop;
					}
				}
			}
		}
		return list;
	}

	/**
	 * 每份对应分得的数量
	 * @param dataTotalCount
	 * @param size
	 * @return
	 */
	private static int getPerCount(int dataTotalCount, int size) {
		int perCount = dataTotalCount / size ;
		if(perCount == 0) {
			perCount = 1;
		}
		return perCount;
	}

	/**
	 * 返回这个数据中包含数字的个数
	 * @param original
	 */
	private static int getDataTotalCount(String original) {
		int dataCount = 0;
		String[] strArr = original.split(",");
		for(int i=0;i<strArr.length;i++) {
			if(strArr[i].contains("-")) {
				int left = getLeftInt(strArr[i]);
				int right = getRightInt(strArr[i]);
				dataCount = dataCount + (right - left + 1);
			} else {
				dataCount++;
			}
		}
		return dataCount;
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
	
	private static Integer getCount(String str) {
		if(str==null || str.length() == 0) return 0;
		if(str.contains("-")) {
			int left = getLeftInt(str);
			int right = getRightInt(str);
			return (right - left + 1);
		} else {
			return 1;
		}
	}
	
	public static void main(String[] args) {
		List<String> s = NumberGroupUtil.groupNumber("2-5,7,11,14-20", 3);
		System.out.println(s);
	}
}
