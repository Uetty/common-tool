package com.uetty.common.tool.algorithm;

import java.util.List;

/**
 * 多线程方式快速排序
 * @author vince
 */
@SuppressWarnings("unused")
public class MultiQuickSort<T extends Comparable<T>> {

	private final List<T> list;
	private int threadListSize = 500000;
	private boolean asc = true;
	private int threadSize = 1;
	
	public MultiQuickSort(List<T> list) {
		
		this.list = list;// 不会存在同时修改同一个index的线程，无须加锁
	}
	
	public MultiQuickSort<T> sortAsc() {
		this.asc = true;
		return this;
	}
	
	public MultiQuickSort<T> sortDesc() {
		this.asc = false;
		return this;
	}
	
	private boolean t1InRight(T t1, T t2) {
		boolean t1InRight = t1.compareTo(t2) > 0;
		return asc == t1InRight;
	}
	
	/**
	 * 当任务数达到多少时启用新线程
	 */
	@SuppressWarnings("UnusedReturnValue")
	public MultiQuickSort<T> setBusyLine(int size) {
		this.threadListSize = size;
		return this;
	}
	
	public void execute() {
		try {
			quickSort();
		} catch (Throwable e) {
			if (e instanceof QuickSortException) {
				throw (QuickSortException)e;
			}
			throw new QuickSortException(e);
		}
	}
	
	private void quickSort() throws InterruptedException {
		threadSize = 1;
		quickSortMedianPlace(0, list.size() - 1);
	}
	
	
	private void quickSortMedianPlace(int left, int right) throws InterruptedException {
		if (left >= right) return;
		for (int m = 0; m < 1; m++) {// 假循环，防止极端数据
			int i = left;// 左边游标值
			int j = right;// 右边游标值
			T medianValue = list.get(i);// 初始以左边界值作为中间值
			int posDir = -1;// 初始从右往左检测
			while (i != j) {// 当i与j位置重叠
				if (posDir > 0) {// 从左往右
					if (t1InRight(list.get(i), medianValue)) {// 在左边找到应该在中间值右边的值
//					if (list.get(i).compareTo(medianValue) > 0) {
						list.set(j, list.get(i));// 将找到的值放到右边上次出值的位置
						posDir = -1;// 查找方向交替
						j--;// 右边的游标值减少
					} else {
						i++;
					}
				} else {// 从右往左
					if (t1InRight(medianValue, list.get(j))) {// 在右边找到应该在中间值左边的值
//					if (medianValue.compareTo(list.get(j)) > 0) {
						list.set(i, list.get(j));// 找到的值放到左边的上次出值的位置
						posDir = 1;// 查找方向交替
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
				T first = list.get(left);
				int exchangeIndex = left + sz / 3 + ((int)(Math.random() * sz / 3));
				list.set(left, list.get(exchangeIndex));
				list.set(exchangeIndex, first);
				m--;
				continue;
			}
			
			// 左右子区间分别递归
			boolean newThread = false;
			
			if ((sz / 2.5 / threadSize / threadSize > threadListSize) && ls > threadListSize && rs > threadListSize) {
				// 如果左右区间的数据量均较大
				threadSize++;
				newThread = true;
			}
			
			if (newThread) {
				// 快速排序存在扩展性，可以使用多线程排序加速
				// 注意这里只启一个新线程，不然算法会有两个优化问题：
				// 1. 排序算法在时间利用上会存在启动线程阶段的空档期 2. 线程启动本身是比较消耗时间资源盲目启用降低效率
				// 应充分利用已有线程，在新线程的启动阶段让旧线程的排序代码继续运行
				if (ls > rs) {
					SortRunable runable = new SortRunable(i + 1, right);
					Thread thread = new Thread(runable);
					thread.setDaemon(true);
					thread.start();
					quickSortMedianPlace(left, i - 1);
					thread.join();
				} else {
					SortRunable runable = new SortRunable(left, i - 1);
					Thread thread = new Thread(runable);
					thread.setDaemon(true);
					thread.start();
					quickSortMedianPlace(i + 1, right);
					thread.join();
				}
			} else {
				quickSortMedianPlace(left, i - 1);
				quickSortMedianPlace(i + 1, right);
			}
		}
	}
	
	/**
	 * 排序辅助线程
	 */
	private class SortRunable implements Runnable {
		int left;
		int right;
		public SortRunable(int left, int right) {
			this.left = left;
			this.right = right;
		}
		@Override
		public void run() {
			try {
				quickSortMedianPlace(left, right);
			} catch (InterruptedException e) {
				throw new QuickSortException(e);
			}
		}
	}
	
	@SuppressWarnings("serial")
	public static class QuickSortException extends RuntimeException {
		
		public QuickSortException(Throwable e) {
			super(e);
		}
		
	}
}
