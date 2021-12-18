package com.colin.atomic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @author colin
 * @create 2021-12-16 20:25
 */
public class AtomicToABADemo {

    static AtomicStampedReference<Integer> stampedReference = new AtomicStampedReference<>(100,1);
    static AtomicMarkableReference<Integer> markableReference = new AtomicMarkableReference<>(100,false);

    public static void main(String[] args) {

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+"\t 1次版本号"+stampedReference.getStamp());
            //故意暂停200毫秒，让后面的t4线程拿到和t3一样的版本号
            try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

            stampedReference.compareAndSet(100,101,stampedReference.getStamp(),stampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName()+"\t 2次版本号"+stampedReference.getStamp());
            stampedReference.compareAndSet(101,100,stampedReference.getStamp(),stampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName()+"\t 3次版本号"+stampedReference.getStamp());
        },"t3").start();

        new Thread(() -> {
            int stamp = stampedReference.getStamp();
            System.out.println(Thread.currentThread().getName()+"\t =======1次版本号"+stamp);
            //暂停2秒钟,让t3先完成ABA操作了，看看自己还能否修改
            try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
            boolean b = stampedReference.compareAndSet(100, 2020, stamp, stamp + 1);
            System.out.println(Thread.currentThread().getName()+"\t=======2次版本号"+stampedReference.getStamp()+"\t"+stampedReference.getReference());
        },"t4").start();

        System.out.println();
        System.out.println();
        System.out.println();
        try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("============AtomicMarkableReference不关心引用变量更改过几次，只关心是否更改过======================");

        new Thread(() -> {
            boolean marked = markableReference.isMarked();
            System.out.println(Thread.currentThread().getName()+"\t 1次版本号"+marked);

            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            markableReference.compareAndSet(100,101,marked,!marked);
            System.out.println(Thread.currentThread().getName()+"\t 2次版本号"+markableReference.isMarked());

            markableReference.compareAndSet(101,100,markableReference.isMarked(),!markableReference.isMarked());
            System.out.println(Thread.currentThread().getName()+"\t 3次版本号"+markableReference.isMarked());
        },"t5").start();

        new Thread(() -> {
            boolean marked = markableReference.isMarked();
            System.out.println(Thread.currentThread().getName()+"\t 1次版本号"+marked);

            //暂停几秒钟线程
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            markableReference.compareAndSet(100,2020,marked,!marked);
            System.out.println(Thread.currentThread().getName()+"\t"+markableReference.getReference()+"\t"+markableReference.isMarked());
        },"t6").start();

    }
}
