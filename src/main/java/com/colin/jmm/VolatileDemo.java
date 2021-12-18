package com.colin.jmm;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author colin
 * @create 2021-12-16 16:51
 */
public class VolatileDemo {

    // 不加 volatile，没有可见性，程序无法结束
    static boolean flag = true;

    // 加 volatile 修饰，保证可见性，程序可以正常结束
    // static volatile boolean flag = true;


    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+"\t come in");
            while (flag)
            {

            }
            System.out.println(Thread.currentThread().getName()+"\t flag被修改为false,退出.....");
        },"t1").start();

        //暂停2秒钟后让main线程修改flag值
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        flag = false;

        System.out.println("main线程修改完成");
    }
}
