package main.java.com.amina.zookeeperlock;

import main.java.com.amina.zookeeperlock.service.ZooKeeperLockService;
import main.java.com.amina.zookeeperlock.DLock;

public class Locker implements Runnable{
    private ZooKeeperLockService zookeeper;
    private String rootPath;
    private int id;

    public Locker(ZooKeeperLockService zookeeper, String rootPath, int id){
        this.zookeeper = zookeeper;
        this.rootPath = rootPath;
        this.id = id;
    }

    @Override
    public void run(){
        DLock dLock = new DLock(zookeeper, rootPath);
        System.out.println("Locker " + id + " waiting for lock...");
        try{
            dLock.lock();
            System.out.println("Locker " + id + " acquired lock.");
            try{
                System.out.println("Locker " + id + " is doing work...");
                Thread.sleep(1000); // Simulate work
            }catch(InterruptedException e){
                System.out.println("Locker " + id + " interrupted while doing work.");
                Thread.currentThread().interrupt();
            }

            System.out.println("Locker " + id + " finished work. Releasing lock.");
            dLock.release();
            System.out.println("Locker " + id + " released lock.");
        }catch(DLockException e){
            System.err.println("Locker " + id + " failed to acquire lock: " + e.getMessage());
            if(e.getCause() != null){
                e.getCause().printStackTrace();
            }
        }    
    }
}
