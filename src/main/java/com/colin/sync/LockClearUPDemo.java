package com.colin.sync;

/**
 * @author colin
 * @create 2021-12-19 16:20
 *
 * 锁消除：
 * 从 JIT 角度看相当于无视它，synchronized(object) 不存在了，这个锁对象并没有被共用扩散到其它线程使用，
 * 极端的说就是根本没有加这个锁对象的底层机器码，消除了锁的使用。
 * 每个线程加锁的对象都不是同一个，相当于多线程各自处理各自的锁对象，没有产生锁竞争，JIT 无视这样的锁。
 */
public class LockClearUPDemo {

    static Object objectLock = new Object();//正常的

    public void method(){

        // 锁消除，JIT 会无视它，synchronized(对象锁)不存在了。不正常的。（每个线程锁的对象都不是同一个）
        Object object = new Object();
        synchronized (object){
            System.out.println("-----hello LockClearUPDemo"+"\t"+object.hashCode()+"\t"+objectLock.hashCode());
        }
    }

    public static void main(String[] args) {
        LockClearUPDemo lockClearUPDemo = new LockClearUPDemo();
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                lockClearUPDemo.method();
            },"thread-i").start();
        }
    }
}
