package com.colin.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author colin
 * @create 2021-12-16 11:23
 */
public class LockSupportDemo {

    public static void main(String[] args) {



    }

    // LockSupport 类的 park() 和 unpark() 方法测试
    private static void lockSupportApiTest() {
        //正常使用+不需要锁块
        Thread t1 = new Thread(() -> {
            // 模拟先唤醒在阻塞。park() 与 unpark() 使用无顺序要求
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName()+" "+"1111111111111");
            LockSupport.park();
            System.out.println(Thread.currentThread().getName()+" "+"2222222222222------end被唤醒");

        },"t1");
        t1.start();

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        // 主线程唤醒线程 t1
        LockSupport.unpark(t1);
        System.out.println(Thread.currentThread().getName()+"   -----LockSupport.unparrk() invoked over");
    }

    // condition 类的 await() 和 signal() 方法测试
    private static void conditionApiDemo() {
        /**
         * Condition 类的 await()、signal() 方法分别是阻塞和唤醒线程
         * 必须在 lock() 和 unlock() 内使用，否则会抛出异常 IllegalMonitorStateException
         */
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"start");
                condition.await();
                System.out.println(Thread.currentThread().getName()+"\t"+"被唤醒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        },"t1").start();

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(3L); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            lock.lock();
            try
            {
                condition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
            System.out.println(Thread.currentThread().getName()+"\t"+"通知了");
        },"t2").start();

        // 模拟异常：先 signal，后 await，阻塞线程不会被唤醒，会抛出异常 IllegalMonitorStateException
        /*try { TimeUnit.SECONDS.sleep(3L); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"start");
                condition.await();
                System.out.println(Thread.currentThread().getName()+"\t"+"被唤醒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        },"t1").start();*/
    }

    // Object 类的 wait 和 notify 方法测试
    private static void objectApiDemo() {
        Object objectLock = new Object(); //同一把锁，类似资源类

        new Thread(() -> {
            synchronized (objectLock) {
                try {
                    objectLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 异常测试：wait、notify、notifyAll 脱离同步代码块 synchronized，会产生异常 IllegalMonitorStateException。
            // wait、notify、notifyAll 必须在 synchronized 内部使用。
            try {
                objectLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName()+"\t"+"被唤醒了");
        },"t1").start();

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(3L); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            synchronized (objectLock) {
                objectLock.notify();
            }

            //objectLock.notify();

            // 异常测试：wait() 方法必须在 notify() 方法前运行，否则会造成等待中的线程无法被唤醒，程序无法结束。
            /*synchronized (objectLock) {
                try {
                    objectLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
        },"t2").start();
    }
}
