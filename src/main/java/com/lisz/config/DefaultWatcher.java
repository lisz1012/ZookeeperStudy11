package com.lisz.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

public class DefaultWatcher implements Watcher {
	private CountDownLatch latch;
	public DefaultWatcher(CountDownLatch init) {
		latch = init;
	}

	@Override
	public void process(WatchedEvent watchedEvent) {
		System.out.println(watchedEvent);
		switch (watchedEvent.getState()) {
			case Unknown:
				break;
			case Disconnected:
				break;
			case NoSyncConnected:
				break;
			case SyncConnected:
				System.out.println("Connected");
				latch.countDown();
				break;
			case AuthFailed:
				break;
			case ConnectedReadOnly:
				break;
			case SaslAuthenticated:
				break;
			case Expired:
				break;
			case Closed:
				break;
		}
	}
}
