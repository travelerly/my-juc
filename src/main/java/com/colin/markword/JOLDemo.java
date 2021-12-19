package com.colin.markword;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author colin
 * @create 2021-12-19 12:12
 */
public class JOLDemo {

    public static void main(String[] args) {
        ObjectTest objectTest = new ObjectTest();
        System.out.println(ClassLayout.parseInstance(objectTest).toPrintable());
    }
}

class ObjectTest{
    private boolean flag;
    private int x;
    private double y;
}
