package main.java.com.amina.zookeeperlock.service;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import java.util.List;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooDefs.Ids;

public interface ZooKeeperLockService {
    String createLockNode(String path) throws KeeperException, InterruptedException;
    List<String> getChildren(final String path, Watcher watcher) throws KeeperException, InterruptedException;
    void deleteLockNode(String path) throws KeeperException, InterruptedException;
    void close() throws InterruptedException;
    Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException;
}
