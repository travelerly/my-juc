package com.colin.threadlocal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-12-18 15:41
 */
public class ThreadLocalDemo {

    public static void main(String[] args) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        House house = new House();

        new Thread(() -> {
            try {
                for (int i = 1; i <=3; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();//如果不清理自定义的 ThreadLocal 变量，可能会影响后续业务逻辑和造成内存泄露等问题
            }
        },"t1").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <=2; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();
            }
        },"t2").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <=5; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();
            }
        },"t3").start();
        System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());

    }

    // 三个售票员卖完50张票务，总量完成即可，吃大锅饭，售票员每个月固定月薪
    private static void simpleTest() {
        MovieTicket movieTicket = new MovieTicket();
        for (int i = 1; i <= 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    movieTicket.saleTicket();
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, String.valueOf(i)).start();
        }
    }
}

class MovieTicket {

    int number = 50;
    public synchronized void saleTicket() {
        if(number > 0) {
            System.out.println(Thread.currentThread().getName()+"\t"+"号售票员卖出第： "+(number--));
        }else{
            System.out.println("--------卖完了");
        }
    }
}

class House {
    ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);
    public void saleHouse() {
        Integer value = threadLocal.get();
        value++;
        threadLocal.set(value);
    }
}