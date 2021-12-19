package com.colin.threadlocal;

import java.lang.ref.PhantomReference;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author colin
 * @create 2021-12-18 17:12
 */
public class DateUtilsDemo {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final ThreadLocal<SimpleDateFormat>  sdf_threadLocal =
            ThreadLocal.withInitial(()-> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        sdfUnSafeTest();
    }


    private static void sdfUnSafeTest() {
        for (int i = 1; i <=30; i++) {
            new Thread(() -> {
                try {
                    // 未使用 ThreadLocal
                    // System.out.println(DateUtilsDemo.parseDate("2021-11-11 11:11:11"));

                    // 使用 ThreadLocal
                    // System.out.println(DateUtilsDemo.parseDateTL("2021-11-11 11:11:11"));

                    // 使用 DateTimeFormatter 代替 SimpleDateFormat
                    // System.out.println(DateUtilsDemo.parse("2021-11-11 11:11:11"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }

    // 模拟并发环境下使用 SimpleDateFormat 的 parse 方法将字符串转换成 Date 对象
    public static Date parseDate(String stringDate)throws Exception {
        return sdf.parse(stringDate);
    }

    // ThreadLocal 可以确保每个线程都可以得到各自单独的一个 SimpleDateFormat 的对象，那么自然也就不存在竞争问题了。
    public static Date parseDateTL(String stringDate)throws Exception {
        return sdf_threadLocal.get().parse(stringDate);
    }

    // DateTimeFormatter 代替 SimpleDateFormat
    public static LocalDateTime parse(String dateString) {
        return LocalDateTime.parse(dateString,DATE_TIME_FORMAT);
    }
}
