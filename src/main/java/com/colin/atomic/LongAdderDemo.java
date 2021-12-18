package com.colin.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

/**
 * @author colin
 * @create 2021-12-18 09:41
 */
public class LongAdderDemo {
/**
 * 初始化 cells 或者扩容 cells 需要获取锁
 * 0 表示无锁状态；1 表示其它线程已经持有了锁
 */

    public static void main(String[] args) throws InterruptedException {

        ClickNumberNet clickNumberNet = new ClickNumberNet();

        long startTime;
        long endTime;
        CountDownLatch countDownLatch = new CountDownLatch(50);
        CountDownLatch countDownLatch2 = new CountDownLatch(50);
        CountDownLatch countDownLatch3 = new CountDownLatch(50);
        CountDownLatch countDownLatch4 = new CountDownLatch(50);

        // 验证 int number
        startTime = System.currentTimeMillis();
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                try {
                    for (int j = 1; j <=100 * 10000; j++) {
                        clickNumberNet.clickBySync();
                    }
                }finally {
                    countDownLatch.countDown();
                }
            },String.valueOf(i)).start();
        }
        countDownLatch.await();
        endTime = System.currentTimeMillis();
        System.out.println("costTime: "+(endTime - startTime)+" 毫秒"+ "\t number++" +"\t clickBySync result: "+clickNumberNet.number);

        // 验证 AtomicLong atomicLong
        startTime = System.currentTimeMillis();
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                try {
                    for (int j = 1; j <=100 * 10000; j++) {
                        clickNumberNet.clickByAtomicLong();
                    }
                }finally {
                    countDownLatch2.countDown();
                }
            },String.valueOf(i)).start();
        }
        countDownLatch2.await();
        endTime = System.currentTimeMillis();
        System.out.println("costTime: "+(endTime - startTime) +" 毫秒"+ "\t atomicLong.incrementAndGet()" +"\t clickByAtomicLong result: "+clickNumberNet.atomicLong);

        // 验证 LongAdder longAdder
        startTime = System.currentTimeMillis();
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                try {
                    for (int j = 1; j <=100 * 10000; j++) {
                        clickNumberNet.clickByLongAdder();
                    }
                }finally {
                    countDownLatch3.countDown();
                }
            },String.valueOf(i)).start();
        }
        countDownLatch3.await();
        endTime = System.currentTimeMillis();
        System.out.println("costTime: "+(endTime - startTime) +" 毫秒"+"\t longAdder.increment()"+"\t clickByLongAdder result: "+clickNumberNet.longAdder.sum());

        // 验证 LongAccumulator longAccumulator
        startTime = System.currentTimeMillis();
        for (int i = 1; i <=50; i++) {
            new Thread(() -> {
                try {
                    for (int j = 1; j <=100 * 10000; j++) {
                        clickNumberNet.clickByLongAccumulator();
                    }
                }finally {
                    countDownLatch4.countDown();
                }
            },String.valueOf(i)).start();
        }
        countDownLatch4.await();
        endTime = System.currentTimeMillis();
        System.out.println("costTime: "+(endTime - startTime) +" 毫秒"+"\t longAccumulator.accumulate(1)"+"\t clickByLongAccumulator result: "+clickNumberNet.longAccumulator.longValue());

    }
}

class ClickNumberNet {

    int number = 0;
    public synchronized void clickBySync() {
        number++;
    }

    AtomicLong atomicLong = new AtomicLong(0);
    public void clickByAtomicLong() {
        atomicLong.incrementAndGet();
    }

    LongAdder longAdder = new LongAdder();
    public void clickByLongAdder() {
        longAdder.increment();
    }

    LongAccumulator longAccumulator = new LongAccumulator((x,y) -> x + y,0);
    public void clickByLongAccumulator() {
        longAccumulator.accumulate(1);
    }
}