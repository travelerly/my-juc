package com.colin.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author colin
 * @create 2021-12-19 19:41
 */
public class LockDownGradingDemo {

    public static void main(String[] args) {

        /**
         * 锁降级：遵循获取写锁→再获取读锁→再释放写锁的次序，写锁能够降级成为读锁。
         * 如果一个线程占有了写锁，在不释放写锁的情况下，它还能占有读锁，即写锁降级为读锁。
         */

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


        writeLock.lock();
        System.out.println("-------正在写入");


        readLock.lock();
        System.out.println("-------正在读取");

        writeLock.unlock();

    }
}
