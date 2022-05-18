package com.colin.thread;

import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2022-05-18 16:10
 */
public class InterruptDemo {

    public static void main(String[] args) {

        // testBlockInterrupt();

        testInterrupted();
    }

    // 测试 interrupted() 方法的特点（调用后，会清除中断标志位）
    private static void testInterrupted() {
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println("111111");
        Thread.currentThread().interrupt();
        System.out.println("222222");
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
    }

    // 阻塞状态下使用 interrupted
    private static void testBlockInterrupt() {
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
}
