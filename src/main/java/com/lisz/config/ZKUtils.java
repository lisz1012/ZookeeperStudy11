package com.lisz.config;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {
	private static ZooKeeper zk;
	// 之后的存取操作，路径都是以/testConf为基准的
	private static String addresses = "192.168.1.21:2181,192.168.1.22:2181,192.168.1.23:2181,192.168.1.24:2181/testConf";
	private static CountDownLatch init = new CountDownLatch(1);
	private static Watcher watcher = new DefaultWatcher(init);
	public static ZooKeeper getZk() {
		try {
			zk = new ZooKeeper(addresses, 1000, watcher);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			init.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return zk;
	}
}
