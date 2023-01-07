package com.hzc.mymall.product.web;

import com.hzc.mymall.product.entity.CategoryEntity;
import com.hzc.mymall.product.service.CategoryService;
import com.hzc.mymall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-01 18:19
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 首页映射
     * @return
     */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        // 1. 查出所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();


        // thymeleaf 会自动加默认前缀：classpath:/templates/
        // 且会自动加默认后缀：.html
        // 视图解析器会拼接完整的路径：classpath:/templates/ + "路径" + .html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    // index/catalog.json
    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        // 获取一把锁，只要锁的名字一致，就是同一把锁
        RLock rLock = redisson.getLock("my-lock");

        // 加锁
        // 如果锁已经被其他人获取，则会阻塞等待
        // rLock.lock();
        // 1. 如果业务执行时间过长，在业务执行时间中服务宕机了，如果负责储存这个分布式锁的Redisson节点宕机以后，
        // 而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。为了避免这种情况的发生，
        // Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，不断的延长锁的有效期。
        // 这样即使节点宕机，锁最终还是会过期被自动删除，
        // 默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定。
        // 2. 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
        // 3. 以上两点保证了Redisson不会出现死锁问题

        // 带过期时间的锁，10秒自动解锁，这里要确保过期时间要大于业务执行时间
        // 注意带过期时间的方法，锁的时间到期后不会自动续期
        // 1, 如果传递了过期时间参数，就发送给Redis执行lua脚本，进行占锁，默认超时就是我们指定的时间参数
        // 2, 如果未指定过期时间参数，就使用默认看门狗时间30000L ms，同时只要占锁成功，
        //    每隔1/3看门狗过期时间（10S）就会启动定时任务重新设置过期时间，过期时间还是看门狗过期时间
        rLock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println(Thread.currentThread().getId() + "加锁成功，执行业务...");
            Thread.sleep(30000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            System.out.println(Thread.currentThread().getId() + "释放锁");
            rLock.unlock();
        }

        return "hello";
    }

    // 读写锁保证一定能读到最新数据（读操作会被写操作阻塞），修改期间，写锁是一个排他锁，读锁是一个共享锁
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock writeLock = lock.writeLock();
        String s = "";
        try {
            // 改数据加写锁，读数据加读锁
            writeLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock readLock = lock.readLock();
        String s = "";
        try {
            // 加读锁
            readLock.lock();
            Thread.sleep(30000);
            s = redisTemplate.opsForValue().get("writeValue");
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }

        return s;
    }

    /**
     * 信号量可以用作分布式限流作用
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 获取一个信号，获取一个值，信号量会减1
        // 如果信号量为0，则会阻塞，直到其他线程释放信号量
//        park.acquire();
        boolean b = park.tryAcquire();
        if(b) {
            // 执行业务
        }else{
            return "error";
        }
        return "ok => " + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 释放一个信号，信号量会增1
        park.release();
        return "ok";
    }

    /**
     * 闭锁测试
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); // 等待闭锁都完成(这里是5个)

        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown(); // 计数减一

        return id + "班的人都走了";
    }

}
