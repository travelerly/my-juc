package com.colin.aqs;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author colin
 * @create 2021-12-19 17:25
 */
public class AQSDemo {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock(false);
        reentrantLock.lock();
        reentrantLock.unlock();
    }
}
