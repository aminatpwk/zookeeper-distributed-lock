package com.amina.zookeeperlock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.*;
import main.java.com.amina.zookeeperlock.service.ZooKeeperLockService;
import main.java.com.amina.zookeeperlock.service.ZooKeeperLockServiceImpl;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.concurrent.Future;
import java.util.ArrayList;
import main.java.com.amina.zookeeperlock.Locker;
import java.util.Collections;
import java.util.Objects;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import java.lang.InterruptedException;

public class Main{
    private ExecutorService executorService;
    private ZooKeeperLockService zookeeper;
    private String rootPath;

    public static void main(String[] args) {
        ZooKeeperLockService zookeeper = null;
        try{
            zookeeper = getZooKeeperLockService();
            final String path = "/test";
            ensurePathExists(zookeeper, path);
            new Main(zookeeper, path).run();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(zookeeper != null){
                try{
                    zookeeper.close();
                    System.out.println("Zookeeper connection closed");
                }catch(InterruptedException e){
                    System.err.println("Error closing Zookeeper connection: " + e.getMessage());
                }
            }
        }        
    }

    private static  ZooKeeperLockService getZooKeeperLockService() throws IOException, InterruptedException{
        final CountDownLatch latch = new CountDownLatch(1);
        final String CONN_STRING = "localhost:2181";
        final int TIMEOUT = 2000;
        System.out.println("Connecting to Zookeeper at " + CONN_STRING);
        ZooKeeper zookeeper = new ZooKeeper(CONN_STRING, TIMEOUT, new Watcher(){
            @Override
            public void process(WatchedEvent event){
                if(event.getState() == KeeperState.SyncConnected){
                    System.out.println("Connected to Zookeeper");
                    latch.countDown();
                }
            }
        });
        boolean connected = latch.await(10, TimeUnit.SECONDS);
        if(!connected){
            throw new IOException("Timed out waiting for Zookeeper connection");
        }
        return new ZooKeeperLockServiceImpl(zookeeper);
    }

    private Main(ZooKeeperLockService zookeeper, String rootPath){
        this.executorService = Executors.newFixedThreadPool(4);
        this.zookeeper = zookeeper;
        this.rootPath = rootPath;
    }

    private static void ensurePathExists(ZooKeeperLockService zookeeper, String path){
        System.out.println("Ensuring path exists: " + path);
    }

    void run(){
        try{
            doRun();
            System.out.println("Task completed successfully");
        }catch(InterruptedException e){
            System.err.println("Task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }catch(ExecutionException e){
            System.err.println("Task execution failed: " + e.getMessage());
            e.printStackTrace();
        }finally{
            System.out.println("Shutting down executor");
            executorService.shutdown();
            try{
                if(!executorService.awaitTermination(5, TimeUnit.SECONDS)){
                    executorService.shutdownNow();
                }
            }catch(InterruptedException e){
                executorService.shutdownNow();
            }
        }
    }

    private void doRun() throws InterruptedException, ExecutionException{
        List<Future<?>> futures = new ArrayList<>();
        final int N = 10;
        System.out.println("Starting " + N + " tasks");
        for(int i = 0; i < N; i++){
            Future<?> future = executorService.submit(new Locker(zookeeper, rootPath, i));
            futures.add(future);
        }

        for(int i = 0; i< N; i++){
            futures.get(i).get();
        }
    }
}