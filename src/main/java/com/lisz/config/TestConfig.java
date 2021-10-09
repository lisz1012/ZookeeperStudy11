package com.lisz.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestConfig {
	private ZooKeeper zk;

	@Before
	public void conn() {
		zk = ZKUtils.getZk();
	}

	// 并没有给每种场景都线性地写一个方法堆砌起来，而是把这些方法都事先准备好，用了回调和Watch事件把他们粘连起来
	@Test
	public void getConf() {
		final MyConf myConf = new MyConf();
		WatcherCallback watcherCallback = new WatcherCallback(zk, myConf);

		watcherCallback.await();
		// 1. 节点不存在 2。 节点存在
		while (true) {
			if (myConf.getConf().equals("")) {
				System.out.println("Conf lost...");
				watcherCallback.await();
			}
			System.out.println(myConf.getConf());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
