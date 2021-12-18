package com.colin.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-12-15 17:18
 */
public class DeamonThread {

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+"\t 开始运行，"
                    +(Thread.currentThread().isDaemon() ? "守护线程":"用户线程"));
            while (true) {

            }
        }, "t1");

        // 线程的 daemon 属性为 true 表示是守护线程，false 表示是用户线程
        t1.setDaemon(true);
        t1.start();

        // 3 秒钟后主线程再运行
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }

        System.out.println("----------main 线程运行完毕");

    }
}
