package com.lisz.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatcherCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback,
		AsyncCallback.StatCallback{
	private final String threadName;
	private final ZooKeeper zk;
	private CountDownLatch latch = new CountDownLatch(1);
	private String pathName;

	public WatcherCallback(ZooKeeper zk, String threadName) {
		this.zk = zk;
		this.threadName = threadName;
	}

	public void tryLock() {
		try {
			// if (threadName.equals(zk.getData("/"), this, ...)) //重入锁
			zk.create("/lock", threadName.getBytes(),
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "abc");
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void unLock() {
		try {
			zk.delete(pathName, -1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(WatchedEvent event) {
		// 如果第一个哥们儿，那个锁释放了，则只有第二个收到回调事件
		// 如果不是第一个哥们儿，挂了，也能造成他的后面收到这个通知，从而然他后面的哥们儿watch挂掉的这个哥们前面的
		switch (event.getType()) {
			case None:
				break;
			case NodeCreated:
				break;
			case NodeDeleted:
				zk.getChildren("/", false, this, "abc");
				break;
			case NodeDataChanged:
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

	// zk.create()的回调
	@Override
	public void processResult(int rc, String path, Object ctx, String name) {
		if (name != null) {
			System.out.println(threadName + " creates node: " + name); //打印节点名有序，但是线程名是乱序的
			pathName = name;
			zk.getChildren("/", false, this, "abc");
		}
	}

	// getChildren callback
	@Override
	public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
		// 一定能看到自己前面的
//		System.out.println(threadName + " look locks: ");
//		for (String child : children) {
//			System.out.println(child);
//		}
		Collections.sort(children);
		final int index = children.indexOf(pathName.substring(1));
		// 是不是第一个
		if (index == 0) {
			System.out.println(threadName + " I'm the 1st");
			try {
				// 为的是实现重入锁，在tryLock的时候检查一下数据，如果是threadName就直接获得锁
				zk.setData("/", threadName.getBytes(), -1);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			latch.countDown();
		} else {
			final String prev = children.get(index - 1);
			zk.exists("/" + prev, this, this, "aaa");
		}
//		if (!children.isEmpty() && pathName.equals(children.get(0))) {
//			latch.countDown();
//		}
	}

	// zk.exists()的callback
	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		// 按理来说要做判断，这里偷懒了
	}
}
