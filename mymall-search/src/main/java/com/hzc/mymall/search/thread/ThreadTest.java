package com.hzc.mymall.search.thread;

import java.util.concurrent.*;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-03 23:20
 */
public class ThreadTest {
    public static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 	- 继承Thread
     * 	- 实现Runnable接口
     * 	- 实现Callable接口 + FutureTask（可以拿到返回结果，可以处理异常）
     * 	- 线程池
     * @param args
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("main...start...");

//        Thread01 thread01 = new Thread01();
//        thread01.start();
//        // thread01.join();
//
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();
//
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        // FutureTask#get() 等待线程运行完成（会阻塞调用线程的执行）返回结果
//        Integer integer = futureTask.get();
//        System.out.println("FutureTask 执行结果是：" + integer);

        // 在业务代码里面，一般不用上面三种操作线程的方式，而是使用线程池来执行
        // 【所有的多线程任务应该交给线程池来执行】
        // 当前系统中线程池最好控制在一两个之内，异步任务都提交给线程池去执行
//        executorService.execute(new Runnable01());
//        executorService.shutdown();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        System.out.println("main...end...");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
