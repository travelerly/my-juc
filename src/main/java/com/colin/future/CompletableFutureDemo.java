package com.colin.future;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author colin
 * @create 2021-12-15 18:04
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        thenCombineTest();

    }

    // 对计算结果进行合并，先完成的先等着，等待其它分支任务
    private static void thenCombineTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> thenCombineResult = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in 1");
            return 10;
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in 2");
            return 20;
        }), (x,y) -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in 3");
            return x + y;
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in 4");
            return 30;
        }),(a,b) -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in 5");
            return a * b;
        });
        System.out.println("-----主线程结束，END");
        System.out.println(thenCombineResult.get());


        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    // 对计算速度进行选用，选取运行快的任务进行计算
    private static void applyToEitherTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in ");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
            return 10;
        });

        CompletableFuture<Integer> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in ");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            return 20;
        });

        CompletableFuture<Integer> thenCombineResult = completableFuture1.applyToEither(completableFuture2,f -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "---come in ");
            return f + 1;
        });

        System.out.println(Thread.currentThread().getName() + "\t" + thenCombineResult.get());

        /**
         * ForkJoinPool.commonPool-worker-9	---come in
         * ForkJoinPool.commonPool-worker-2	---come in
         * ForkJoinPool.commonPool-worker-2	---come in
         * main	21
         */
    }

    // 任务顺序执行
    private static void thenTest() {
        // 任务 A 执行完毕后执行 B，并且 B 不需要 A 的结果
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenRun(() -> {}).join());

        // 任务 A 执行完毕后执行 B，B 需要 A 的结果，但是任务 B 无返回值
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenAccept(resultA -> {}).join());

        // 任务 A 执行完毕后执行 B，B 需要 A 的结果，同时任务 B 有返回值
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenApply(resultA -> resultA + " resultB").join());

        /**
         * 执行结果：
         * null
         * null
         * resultA resultB
         */
    }

// 计算结果存在依赖关系，这两个线程串行化（当前步骤发生异常，携带异常参数继续走下一步）
private static void handleTest() {
    //当一个线程依赖另一个线程时用 handle 方法来把这两个线程串行化,
    // 异常情况：有异常也可以往下一步走，根据带的异常参数可以进一步处理
    CompletableFuture.supplyAsync(() -> {
        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("111");
        return 1024;
    }).handle((f,e) -> {
        int age = 10/0;
        System.out.println("222");
        return f + 1;
    }).handle((f,e) -> {
        System.out.println("333");
        return f + 1;
    }).whenCompleteAsync((v,e) -> {
        System.out.println("*****v: "+v);
    }).exceptionally(e -> {
        e.printStackTrace();
        return null;
    });

    System.out.println("-----主线程结束，END");

    // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
    try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

    /**
     * 异常结果
     * -----主线程结束，END
     * 111
     * 333
     * *****v: null
     * java.util.concurrent.CompletionException: java.lang.NullPointerException
     */
}

    // 计算结果存在依赖关系，这两个线程串行化（当前步骤发生异常，不走下一步，哪步出错，就停在哪步）
    private static void thenApplyTest() {
        // 当一个线程依赖另一个线程时用 thenApply 方法来把这两个线程串行化,
        CompletableFuture.supplyAsync(() -> {
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("111");
            return 1024;
        }).thenApply(f -> {
            System.out.println("222");
            return f + 1;
        }).thenApply(f -> {
            int age = 10/0; // 异常情况：哪步出错就停在哪步。
            System.out.println("333");
            return f + 1;
        }).whenCompleteAsync((v,e) -> {
            System.out.println("*****v: "+v);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        System.out.println("-----主线程结束，END");

        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        /**
         * 正常结果
         * -----主线程结束，END
         * 111
         * 222
         * 333
         * *****v: 1026
         */

        /**
         * 异常结果
         * -----主线程结束，END
         * 111
         * 222
         * *****v: null
         * java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
         */
    }


    /**
     * 主动触发计算。
     * 当 completableFuture.get() 时发生阻塞，complete 可以结束阻塞，立即返回 complete 面里面的值
     * 当 completableFuture.get() 时立即获取到结果，complete 不做计算
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void completeTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            return 1024;
        });

        // 注释掉暂停线程，get 还没有算完只能返回 complete 方法设置的2048；暂停2秒钟线程，异步线程能够计算完成返回get
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        /**
         * 当 completableFuture.get() 时发生阻塞，complete 可以结束阻塞，立即返回 complete 面里面的值
         * 当 completableFuture.get() 时立即获取到结果，complete 不做计算
         */
        System.out.println(completableFuture.complete(2048)+"\t"+completableFuture.get());

        // 当 completableFuture.get() 时，若 supplyAsync() 方法尚未计算完成，结果为：true	2048
        // 当 completableFuture.get() 时，若 supplyAsync() 方法计算完成，结果为：false	1024
    }

    // 异步任务结束/异常后回调方法
    private static void callBackTest() {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("-----计算结束耗时1秒钟，result： "+result);
            if(result > 6)
            {
                // 模拟异常
                int age = 10/0;
            }
            return result;
        }).whenComplete((v,e) ->{ // 异步任务 supplyAsync 正常结束后，回调 whenComplete 方法
            if(e == null)
            {
                System.out.println("-----result: "+v);
            }
        }).exceptionally(e -> { // 异步任务 supplyAsync 发生异常后，回调 exceptionally 方法
            System.out.println("-----exception: "+e.getCause()+"\t"+e.getMessage());
            return -44;
        });

        //主线程不要立刻结束，否则 CompletableFuture 默认使用的线程池会立刻关闭:暂停3秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    // supplyAsync：有返回值
    private static void supplyAsyncTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
            //暂停几秒钟线程
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ThreadLocalRandom.current().nextInt(100);
        });

        System.out.println(completableFuture.get());

    }

    // runAsync：无返回值
    private static void runAsyncTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName()+"\t"+"-----come in");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("-----task is over");
        });
        System.out.println(future.get());
    }

}
