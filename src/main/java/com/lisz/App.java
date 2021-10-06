package com.lisz;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws Exception{
        //zk是有session的概念，没有连接池的概念
        //多个IP会进行负载，指不定负载给哪一个。watch观察回调分为两类：new zk的时候传入的：session级别的，跟node和path没有关系
        // watch的注册只发生在读类型的调用上：getData、exists...
        final CountDownLatch latch = new CountDownLatch(1);
        // 第二个参数sessionTimeout决定了临时节点的生存时间，程序执行完了多长时间之后节点会消失
        final ZooKeeper zooKeeper = new ZooKeeper(
                "192.168.1.21:2181,192.168.1.22:2181,192.168.1.23:2181,192.168.1.24:2181",
                3000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                final Event.KeeperState state = event.getState();
                final Event.EventType type = event.getType();
                final String path = event.getPath();
                System.out.println(event);
                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
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
                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
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
        });

        latch.await();

        final ZooKeeper.States state = zooKeeper.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("connecting");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed...");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        // 返回值很有意义，尤其是创建带序列号的节点的时候
        String pathName = zooKeeper.create("/xxxx", "olddata".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        final Stat stat = new Stat();
        final byte[] node = zooKeeper.getData("/xxxx", new Watcher() {
            // getData的时候注册，这个节点的数据改变的时候回调用这个回调
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch: " + event);
                try {
                    // true: default Watcher被注册： new Zookeeper()的那个watcher
                    // this：仍将使用外层的这个Watcher, 每次修改完就触发上面的打印，然后继续注册，用getData
                    zooKeeper.getData("/xxxx", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println(new String(node));

        // 这个时候触发上面Watcher的process方法
        final Stat stat1 = zooKeeper.setData("/xxxx", "newdata".getBytes(), 0);
        //还会触发吗？不会，除非在process 里面每次都重新注册watcher
        final Stat stat2 = zooKeeper.setData("/xxxx", "newdata01".getBytes(), stat1.getVersion());

        System.out.println(Thread.currentThread().getName() + " ----- async start -----");
        zooKeeper.getData("/xxxx", false, new AsyncCallback.DataCallback() {
            // 异步, 另一个线程中执行下面的方法，getData返回的时候调用下面这个方法, 这里有阻塞，异步会减少空转
            @Override
            public void processResult(int rc, String s, Object ctx, byte[] data, Stat stat) {
                System.out.println(Thread.currentThread().getName() + " ----- async callback -----");
                System.out.println(Thread.currentThread().getName() + " " + new String(data));
            }
        }, "abc"); // "abc"可以是任何对象，就是上面参数中ctx的
        System.out.println(Thread.currentThread().getName() + " ----- async over -----");

        //Thread.sleep(2000);
        Thread.sleep(222222);

    }
}
