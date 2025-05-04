package main.java.com.amina.zookeeperlock;
public class DLockException extends Exception{
    public DLockException(String message) {
        super(message);
    }

    public DLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
