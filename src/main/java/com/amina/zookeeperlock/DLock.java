package main.java.com.amina.zookeeperlock;

import main.java.com.amina.zookeeperlock.service.ZooKeeperLockService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import main.java.com.amina.zookeeperlock.DLockException;
import java.util.Collections;
import java.util.Objects;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

public class DLock {
    private final ZooKeeperLockService zookeeper;
    private final String rootPath;
    private final int RETRY_INTERVAL = 500;
    private  String lockPath = null;
    private  String lockName = "lock-";

    public DLock(ZooKeeperLockService zookeeper, String rootPath){
        this.zookeeper = zookeeper;
        this.rootPath = rootPath;
    }

    public void lock() throws DLockException{
        try{
            doLock();
        }catch(KeeperException | InterruptedException e){
            throw new DLockException("Gabim ne kapjen e lockut", e);
        }
    }

    private void doLock() throws KeeperException, InterruptedException{
        lockPath = zookeeper.createLockNode(rootPath + "/" + lockName);
        String thisNodeName = lockPath.substring(lockPath.lastIndexOf("/") + 1);
        final Object lock = new Object();
        while(true){
            synchronized(lock){
                List<String> children = zookeeper.getChildren(rootPath, new Watcher() {
                    @Override
                    public void process(WatchedEvent event){
                        synchronized(lock){
                            lock.notifyAll();
                        }
                    }
                });
              Collections.sort(children);
              
              if(thisNodeName.equals(children.get(0))){
                return;
              }else{
                String watchNode = null;
                for(int i =0; i<children.size(); i++){
                    if(thisNodeName.equals(children.get(i))){
                        watchNode = children.get(i-1);
                        break;
                    }
                }
                if(watchNode != null){
                    String watchNodePath = rootPath + "/" + watchNode;
                    if(zookeeper.exists(watchNodePath, new Watcher(){
                        @Override
                        public void process(WatchedEvent event){
                            synchronized(lock){
                                lock.notifyAll();
                            }
                        }
                    }) == null){
                        continue;
                    }
                }
              }

              lock.wait(RETRY_INTERVAL);
            }
        }
    }

    public void release() throws DLockException{
        if(Objects.isNull(lockPath)){
            return;
        }

        try{
            zookeeper.deleteLockNode(lockPath);
            lockPath = null;
        }catch (InterruptedException | KeeperException e){
            throw new DLockException("Gabim ne lirimin e lockut", e);
        }
    }

    private boolean exists(String path, Watcher watcher) throws KeeperException, InterruptedException{
        return zookeeper.exists(path, watcher) != null;
    }
}
