package com.lisz.lock;

import com.lisz.config.MyConf;
import com.lisz.config.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLock {
	private ZooKeeper zk;

	@Before
	public void conn() {
		zk = ZKUtils.getZk();
	}

	@Test
	public void lock() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			new Thread(){
				@Override
				public void run() {
					final String threadName = Thread.currentThread().getName();
					WatcherCallback watcherCallback = new WatcherCallback(zk, threadName);
					// 每一个线程

					// 抢锁
					watcherCallback.tryLock();
					// 干活
					System.out.println(threadName + ": Gan huo");
					// 睡一秒，避免第一个干完活了，后面的还没watch （exist）它所创建的path（或节点）的删除事件
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 释放锁
					watcherCallback.unLock();
				}
			}.start();
		}
		while (true) {
			Thread.sleep(1000);
		}
	}

	@After
	public void close() {
		try {
			zk.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
