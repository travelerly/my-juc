package com.colin.sync;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author colin
 * @create 2021-12-19 13:51
 */
public class BiasedLockDemo {

    private static Object object = new Object();

public static void main(String[] args) {

    Object object = new Object();

    new Thread(()->{
        synchronized (object){
            System.out.println(ClassLayout.parseInstance(object).toPrintable());
        }
    },"thread-A").start();

    new Thread(()->{
        synchronized (object){
            System.out.println(ClassLayout.parseInstance(object).toPrintable());
        }
    },"thread-B").start();
}
}
