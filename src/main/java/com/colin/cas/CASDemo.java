package com.colin.cas;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author colin
 * @create 2021-12-16 19:27
 */
public class CASDemo {

    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger();
        atomicInteger.getAndIncrement();
    }
}
