package com.hzc.mymall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-04 0:37
 */
public class CompletableFutureTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start...");
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10/2;
//            System.out.println("运算结果：" + i);
//        }, executor);

//        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运算结果：" + i);
//            return i;
//        }, executor).whenComplete((res, exception) -> {
//            // 可以得到异常信息，但是无法修改返回数据
//            System.out.println("异步任务成功完成了...结果是：" + res + ";异常是：" + exception);
//        }).exceptionally(throwable -> {
//            // 可以感知异常，同时返回默认值
//            return 10;
//        });

        // 方法执行完成后的处理
//        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运算结果：" + i);
//            return i;
//        }, executor).handle((res, throwable) -> {
//            if(res != null) {
//                return res * 2;
//            }
//            if(throwable != null) {
//                return 0;
//            }
//            return 0;
//        });

//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运算结果：" + i);
//            return i;
//        }, executor).thenRunAsync(() -> System.out.println("任务2启动了"), executor);

//        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程启动：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("任务1线程结束：" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程启动：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//            System.out.println("任务2线程结束");
//            return "hello";
//        }, executor);

//        future1.runAfterBothAsync(future2, () -> {
//            System.out.println("任务3开始");
//        },executor);
//        future1.thenAcceptBothAsync(future2, (f1, f2) -> {
//            System.out.println("任务3开始... 之前的结果：" + f1 + "-->>" + f2);
//        }, executor);
//        CompletableFuture<String> thenCombineAsync = future1.thenCombineAsync(future2, (f1, f2) -> {
//            return "之前的结果：" + f1 + " ==> " + f2;
//        }, executor);
//        System.out.println(thenCombineAsync.get());

        /**
         * 两个任务，只要有一个完成，就执行任务3
         */
//        future1.runAfterEitherAsync(future2, () -> {
//            System.out.println("任务3开始之前的结果");
//        }, executor);

//        future1.acceptEitherAsync(future2, (res) -> {
//            System.out.println("任务3开始之前的结果：" + res);
//        }, executor);

//        CompletableFuture<String> future = future1.applyToEitherAsync(future2, res -> res.toString() + "任务3返回结果", executor);
//        System.out.println(future.get());

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "iphone.jpg";
        });

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "白色+125g";
        });

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            System.out.println("查询商品的介绍信息");
            return "苹果";
        });

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        allOf.join(); // 等待所有结果完成
//        allOf.get();

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();

        System.out.println("main...end...");
    }
}
