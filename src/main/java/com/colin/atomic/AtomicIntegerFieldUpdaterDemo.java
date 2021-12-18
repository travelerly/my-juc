package com.colin.atomic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author colin
 * @create 2021-12-16 20:38
 */
public class AtomicIntegerFieldUpdaterDemo {

    public static void main(String[] args) {

        BankAccount bankAccount = new BankAccount();

        for (int i = 1; i <=1000; i++) {
            int finalI = i;
            new Thread(() -> {
                bankAccount.transferMoney(bankAccount);
            },String.valueOf(i)).start();
        }

        //暂停毫秒
        try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        System.out.println(bankAccount.money);

    }
}

class BankAccount {
    private String bankName = "CCB";//银行
    public volatile int money = 0;//钱数
    AtomicIntegerFieldUpdater<BankAccount> accountAtomicIntegerFieldUpdater = AtomicIntegerFieldUpdater.newUpdater(BankAccount.class,"money");

    //不加锁+性能高，局部微创
    public void transferMoney(BankAccount bankAccount)
    {
        accountAtomicIntegerFieldUpdater.incrementAndGet(bankAccount);
    }
}
