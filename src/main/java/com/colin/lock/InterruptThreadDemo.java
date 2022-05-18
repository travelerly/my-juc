package com.colin.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author colin
 * @create 2021-12-16 10:02
 */
public class InterruptThreadDemo {

    private static volatile boolean isStop = false;

    private final static AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    public static void main(String[] args) {
        staticInterruptTest();
        // atomicBooleanInterruptTest();
    }

    // static interrupt 测试
    private static void staticInterruptTest() {
        /**
         * 作用是测试当前线程是否被中断（检查中断标志），返回一个boolean并清除中断状态，
         * 第二次再调用时中断状态已经被清除，将返回一个false。
         */
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println("111111");
        Thread.currentThread().interrupt();
        System.out.println("222222");
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
    }

    // interrupt() 与 sleep() 配合使用测试
    private static void interruptAndSleepTest() {
        Thread t1 = new Thread(() -> {
            while (true){
                if (Thread.currentThread().isInterrupted()){
                    System.out.println(Thread.currentThread().getName() + "线程----→ isInterrupted()=true，自己退出了");
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                }catch (Exception e){
                    // 若没有 Thread.currentThread().interrupt()，程序将不会停止，因为 sleep() 抛出异常后，将中断标志位设置为 false
                    // Thread.currentThread().interrupt();
                	e.printStackTrace();
                }
                System.out.println("===测试===");
            }
        },"t1");
        t1.start();

        try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        t1.interrupt();
    }

    // isInterrupted 测试
    private static void isInterruptedTest() {
        Thread t1 = new Thread(() -> {
            for (int i=0;i<300;i++) {
                System.out.println("-------"+i);
            }
            System.out.println("after t1.interrupt()--第2次---: "+Thread.currentThread().isInterrupted());
        },"t1");
        t1.start();

        System.out.println("before t1.interrupt()----: "+t1.isInterrupted());

        // 实例方法 interrupt() 仅仅是设置线程的中断状态位设置为 true，不会停止线程
        t1.interrupt();

        // 活动状态,t1 线程还在执行中
        try { TimeUnit.MILLISECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("after t1.interrupt()--第1次---: "+t1.isInterrupted());

        // 非活动状态，t1 线程不在执行中，已经结束执行了。
        try { TimeUnit.MILLISECONDS.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("after t1.interrupt()--第3次---: "+t1.isInterrupted());
    }

    // Thread API 方法中断线程（interrupt()、isInterrupted()）
    private static void threadApiTest() {
        Thread t1 = new Thread(() -> {
            while(true)
            {
                if(Thread.currentThread().isInterrupted())
                {
                    System.out.println("-----t1 线程被中断了，break，程序结束");
                    break;
                }
                System.out.println("-----hello");
            }
        }, "t1");
        t1.start();

        System.out.println("**************"+t1.isInterrupted());
        //暂停5毫秒
        try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }

        // interrupt()：无返回值
        t1.interrupt();
        // isInterrupted()：有返回值
        System.out.println("**************"+t1.isInterrupted());
    }

    // atomicBoolean 实现线程中断
    private static void atomicBooleanInterruptTest() {
        Thread t1 = new Thread(() -> {
            while(atomicBoolean.get())
            {
                try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                System.out.println("-----hello");
            }
        }, "t1");
        t1.start();

        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }

        atomicBoolean.set(false);
    }

    // volatile 实现线程中断
    private static void volatileInterruptTest() {
        new Thread(() -> {
            while(true)
            {
                if(isStop)
                {
                    System.out.println(Thread.currentThread().getName()+"线程------isStop = true,自己退出了");
                    break;
                }
                System.out.println("-------hello interrupt");
            }
        },"t1").start();

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        isStop = true;
    }
}
