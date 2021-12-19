package com.colin.sync;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author colin
 * @create 2021-12-19 12:46
 */
public class MyObject {

public static void main(String[] args) {
    Object o = new Object();

    System.out.println("10进制hash码："+o.hashCode());
    System.out.println("16进制hash码："+Integer.toHexString(o.hashCode()));
    System.out.println("2进制hash码："+Integer.toBinaryString(o.hashCode()));

    System.out.println( ClassLayout.parseInstance(o).toPrintable());
}
}
