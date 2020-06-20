package com.uetty.common.tool.algorithm;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SortTool {

	/**
	 * 选择排序
	 * <p>每次与最大值相比，找出最大值的位置
	 * @param list 需要排序的列表
	 */
	public static void selectionSort(List<Integer> list) {
		for (int lastIndex = list.size() - 1; lastIndex > 0; lastIndex--) {// 最大值最终安放的位置
			int maxIndex = 0;
			for (int j = 1; j <= lastIndex; j++) {// 将最大值的位置查找出来
				if (list.get(maxIndex) < list.get(j)) {
					maxIndex = j;
				}
			}
			
			if (maxIndex != lastIndex) {// 交换最大值的位置与最终安放位置的值交换
				// 交换值的次数少，这是选择排序的优势
				Integer max = list.get(maxIndex);
				list.set(maxIndex, list.get(lastIndex));
				list.set(lastIndex, max);
			}
			// 劣势，比较次数多
		}
	}
	
	/**
	 * 冒泡排序
	 * <p>每次比相邻元素，将更大元素一级一级推到最后面（就跟冒泡一样）
	 * @param list 需要排序的列表
	 */
	public static void bubbleSort(List<Integer> list) {
		for (int lastIndex = list.size() - 1; lastIndex > 0; lastIndex--) {// 最大值冒泡到的位置
			boolean exchange = false;
			for(int j = 0; j < lastIndex; j++) {// 将最大的数一级一级的冒泡到最后
				Integer cacheJ = list.get(j);
				if (cacheJ > list.get(j + 1)) {
					// 交换
					list.set(j, list.get(j + 1));
					list.set(j + 1, cacheJ);
					exchange = true;
				}
			}
			if (!exchange) {// 该轮未交换过，说明已经全部有序
				// 可以检测提前完成排序，这正是是冒泡排序的优势点
				break;
			}
			// 劣势，交换次数可能会比较多
		}
	}

	/**
	 * 快速排序中值归位
	 * <p>把更大的数放右边，更小的数放左边（左右交替收缩范围进行，保证上次置换的出值位置作为本次置换的入值位置）
	 * @param list 需要排序的列表
	 * @param left 左边位置
	 * @param right 右边位置
	 */
	private static void quickSortMedianPlace(List<Integer> list, int left, int right) {
		if (left >= right) return;
		for (int m = 0; m < 1; m++) {
			int i = left;// 左边游标值
			int j = right;// 右边游标值
			Integer medianValue = list.get(i);// 初始以左边界值作为中间值
			int posDir = -1;// 初始从右往左检测
			while (i != j) {// 当i与j位置重叠
				if (posDir > 0) {// 从左往右
					if (list.get(i).compareTo(medianValue) > 0) {// 在左边找到比中间值大的值
						list.set(j, list.get(i));// 将找到的值放到右边上次出值的位置
						posDir *= -1;// 查找方向交替
						j--;// 右边的游标值减少
					} else {
						i++;
					}
				} else {// 从右往左
					if (medianValue.compareTo(list.get(j)) > 0) {// 在右边找到比中间值小的值
						list.set(i, list.get(j));// 找到的值放到左边的上次出值的位置
						posDir *= -1;// 查找方向交替
						i++;// 左边游标值增加
					} else {
						j--;
					}
				}
			}
			// 找到了中间值的位置
			list.set(i, medianValue);
			
			// 如果出现极端数据重新洗牌
			int rs = right - i;
			int ls = i - left;
			int sz = right - left;
			float percent = rs == 0 ? 0 : ((float)ls / sz);
			if (((sz > 10000) && (percent > 0.92 || percent < 0.08))
					|| ((sz > 500) && (percent > 0.98 || percent < 0.02))) {// 防止极端数据，数据洗牌重来
				Integer first = list.get(left);
				int exchangeIndex = left + sz / 3 + ((int)(Math.random() * sz / 3));
				list.set(left, list.get(exchangeIndex));
				list.set(exchangeIndex, first);
				m--;
				continue;
			}
			
			// 左右区间分别递归
			// 快速排序存在扩展性，可以使用多线程排序加速
			quickSortMedianPlace(list, left, i - 1);
			quickSortMedianPlace(list, i + 1, right);
		}
	}
	/**
	 * 快速排序
	 * <p>把更大的数放右边，更小的数放左边（左右交替收缩范围进行，保证上次置换的出值位置作为本次置换的入值位置），并递归
	 * @param list 需要排序的列表
	 */
	public static void quickSort(List<Integer> list) {
		quickSortMedianPlace(list, 0, list.size() - 1);
	}
	
	public static void multiQuickSort(List<Integer> list) {
		MultiQuickSort<Integer> quickSort = new MultiQuickSort<>(list);
		quickSort.setBusyLine(300000);
		quickSort.execute();
	}
	
	public static void main(String[] args) throws InterruptedException {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 20000000; i++) {
			list.add((int)(Math.random() * 1000000));
		}
		System.out.println("sort start");

		long st;
		List<Integer> source;
		
		source = new ArrayList<>(list);
		System.out.println("\nquick sort");
		st = System.currentTimeMillis();
		quickSort(source);
		System.out.println(System.currentTimeMillis() - st);
//		System.out.println(source);

		Thread.sleep(2000);
		System.out.println("\nmquick sort");
		source = new ArrayList<>(list);
		st = System.currentTimeMillis();
		multiQuickSort(source);
		System.out.println(System.currentTimeMillis() - st);
//		System.out.println(source);
		
//		source = new ArrayList<>(list);
//		System.out.println("\nbubble sort");
//		st = System.currentTimeMillis();
//		bubbleSort(source);
//		System.out.println(System.currentTimeMillis() - st);
//		
//		source = new ArrayList<>(list);
//		System.out.println("\nselection sort");
//		st = System.currentTimeMillis();
//		selectionSort(source);
//		System.out.println(System.currentTimeMillis() - st);
	}
}
