package com.lisz.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatcherCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

	private ZooKeeper zk;
	private MyConf myConf;
	private CountDownLatch latch = new CountDownLatch(1);

	public WatcherCallback(ZooKeeper zk, MyConf myConf) {
		this.zk = zk;
		this.myConf = myConf;
	}

	// 修改的时候会触发Watcher的process方法
	@Override
	public void process(WatchedEvent watchedEvent) {
		switch (watchedEvent.getType()) {
			case None:
				break;
			case NodeCreated:
				zk.getData("/AppConf", this, this, "ABC");
				break;
			case NodeDeleted:
				// 容忍性
				myConf.setConf("");
				latch = new CountDownLatch(1);
				break;
			case NodeDataChanged:
				latch = new CountDownLatch(1);
				zk.getData("/AppConf", this, this, "ABC");
				break;
			case NodeChildrenChanged:
				break;
			case DataWatchRemoved:
				break;
			case ChildWatchRemoved:
				break;
			case PersistentWatchRemoved:
				break;
		}
	}

	//getData是异步的，这是他的callback方法, 拿到data之后
	@Override
	public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
		if (data != null) {
			myConf.setConf(new String(data));
			latch.countDown();
		}

	}

	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		if (stat != null) { // 节点存在的情况
			zk.getData(path, this, this, "ABC");
		}
	}

	public void await() {
		// exist 和getData有注册的效果
		// 创建节点的时候，先走到process方法中的 case NodeDeleted
		// 然后再走到第一个processResult中，然后在countDown，解放下面的这个await
		zk.exists("/AppConf", this, this, "ABC");
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
