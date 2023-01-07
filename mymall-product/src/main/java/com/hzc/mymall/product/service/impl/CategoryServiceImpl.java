package com.hzc.mymall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hzc.mymall.product.service.CategoryBrandRelationService;
import com.hzc.mymall.product.vo.Catelog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.product.dao.CategoryDao;
import com.hzc.mymall.product.entity.CategoryEntity;
import com.hzc.mymall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 组装成父子树形结构
        // 2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 批量删除业务
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 1.检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到 catelogid 完整路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有表的关联数据
     * @CacheEvict: 失效模式
     * @Caching 可组织多个缓存注解
     * 存储同一类型数据，都可以指定同一个分区
     * @param category
     */
    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'level1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    // allEntries设置为true，会删除指定分区下所有数据
    @CacheEvict(value = "category", allEntries = true)
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        if(StringUtils.isNotEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 查找一级分类数据
     * @Cacheable 默认行为：
     *      1，如果缓存中有，方法不用调用
     *      2，key默认自动生成，缓存的名字::SimpleKey []（自主生成的key值）
     *      3，缓存的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4，默认ttl时间 -1
     * 自定义：
     *      1，指定生成的缓存使用的key，key属性指定接受spel表达式
     *      2，指定缓存的数据的存货时间，配置文件中修改ttl spring.cache.redis.time-to-live=3600000
     *      3，将数据保存为json格式
     * @return
     */
    // @Cacheable 注解代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，直接从缓存中获取返回
    // 如果缓存中没有，会调用方法，最后将方法的结果存入缓存
    @Cacheable(value = "category", key = "'level1Categorys'", sync = true) // 每一个需要缓存的数据需要指定放到哪个名字的缓存【缓存的分区（按照业务类型分）】
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities =
                baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 查找所有分类数据，并按要求组织
     * 使用缓存优化查找
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        /**
         * 1. 将数据库的多次查找变为一次
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        // 2. 封装数据
        Map<String, List<Catelog2Vo>> collect = level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 查找该一级分类下的所有二级分类
                    List<CategoryEntity> categoryEntities =
                            getParent_cid(selectList, v.getCatId());
                    // 封装二级分类为指定格式
                    List<Catelog2Vo> catelog2Vos = null;
                    if (categoryEntities != null) {
                        catelog2Vos = categoryEntities.stream().map(item -> {
                            Catelog2Vo catelog2Vo =
                                    new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                            // 查找当前二级分类的三级分类
                            List<CategoryEntity> level3Catlog =
                                    getParent_cid(selectList, item.getCatId());
                            if(level3Catlog != null) {
                                List<Catelog2Vo.Category3Vo> category3Vos = level3Catlog.stream().map(catlog -> {
                                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo();
                                    category3Vo.setId(catlog.getCatId().toString());
                                    category3Vo.setName(catlog.getName());
                                    category3Vo.setCatalog2Id(item.getCatId().toString());
                                    return category3Vo;
                                }).collect(Collectors.toList());

                                catelog2Vo.setCatalog3List(category3Vos);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }

                    return catelog2Vos;
                }));

        return collect;
    }

    /**
     * 查找所有分类数据，并按要求组织
     * 使用缓存优化查找
     * @return
     */
    // @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        // 加入缓存逻辑，缓存中存放的数据是 json 字符串，拿出的json字符串需要逆化为能用的对象类型

        /**
         * todo:
         * 1. 空结果缓存：解决缓存穿透
         * 2. 设置过期时间（加随机值）：解决缓存雪崩
         * 3. 加锁：解决缓存击穿
         */

        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)) {
            // 缓存没有命中，查询数据库
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedissonLock();
            // 将从数据库中查询的数据放入缓存
            return catelogJsonFromDb;
        }

        // 将 json 字符串转为指定对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                new TypeReference<Map<String, List<Catelog2Vo>>>() {});

        return result;
    }

    /**
     * 查找所有分类数据，并按要求组织
     * 从数据库中直接查询并封装
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {
        // 1. 占分布式锁
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        }finally {
            lock.unlock();
        }

        return dataFromDb;
    }

    /**
     * 查找所有分类数据，并按要求组织
     * 从数据库中直接查询并封装
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {
        // 1. 占分布式锁
        String uuid = UUID.randomUUID().toString();
        // // 设置过期时间操作必须和加锁操作是原子的
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if(lock) {
            // 加锁成功
            // 设置过期时间操作必须和加锁操作是原子的
//            redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            }finally {
                // 获取数据成功，删除锁
//            redisTemplate.delete("lock");
                // 这里必须保证判断锁归属与删除锁操作是原子操作
                // 判断获取的锁是不是自己线程加的锁，是才进行删除，避免删除其他线程加的锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)) {
//                redisTemplate.delete("lock");
//            }
                // 使用 redis + lua 脚本删除锁，保证删锁操作的原子性
                String script = "if redis.call('get', KEYS[1])==ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            return dataFromDb;
        }else {
            // 加锁失败...自旋重试
            // 休眠100毫秒重试
            try {
                Thread.sleep(200);
            }catch (Exception e){}
            return getCatelogJsonFromDbWithLocalLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        // 得到锁以后，应该再去缓存中确定一次，如果没有命中才需要继续查询
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(!StringUtils.isEmpty(catalogJSON)) {
            // 缓存命中，直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                    new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            return result;
        }

        /**
         * 1. 将数据库的多次查找变为一次
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        // 2. 封装数据
        Map<String, List<Catelog2Vo>> collect = level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 查找该一级分类下的所有二级分类
                    List<CategoryEntity> categoryEntities =
                            getParent_cid(selectList, v.getCatId());
                    // 封装二级分类为指定格式
                    List<Catelog2Vo> catelog2Vos = null;
                    if (categoryEntities != null) {
                        catelog2Vos = categoryEntities.stream().map(item -> {
                            Catelog2Vo catelog2Vo =
                                    new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                            // 查找当前二级分类的三级分类
                            List<CategoryEntity> level3Catlog =
                                    getParent_cid(selectList, item.getCatId());
                            if(level3Catlog != null) {
                                List<Catelog2Vo.Category3Vo> category3Vos = level3Catlog.stream().map(catlog -> {
                                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo();
                                    category3Vo.setId(catlog.getCatId().toString());
                                    category3Vo.setName(catlog.getName());
                                    category3Vo.setCatalog2Id(item.getCatId().toString());
                                    return category3Vo;
                                }).collect(Collectors.toList());

                                catelog2Vo.setCatalog3List(category3Vos);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }

                    return catelog2Vos;
                }));

        // 从数据库查询到数据后，将数据放在 redis 中，这一步也应该放在锁范围内，防止数据还没放入缓存就释放锁
        // 导致其他线程再次查询数据库
        catalogJSON = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catalogJSON", catalogJSON, 1, TimeUnit.DAYS);

        return collect;
    }

    /**
     * 查找所有分类数据，并按要求组织
     * 从数据库中直接查询并封装
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock() {
        // 得到锁以后，应该再去缓存中确定一次，如果没有命中才需要继续查询
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(!StringUtils.isEmpty(catalogJSON)) {
            // 缓存命中，直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                    new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            return result;
        }

        synchronized (this) {
            /**
             * 1. 将数据库的多次查找变为一次
             */
            List<CategoryEntity> selectList = this.baseMapper.selectList(null);

            // 1. 查出所有1级分类
            List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
            // 2. 封装数据
            Map<String, List<Catelog2Vo>> collect = level1Categorys.stream()
                    .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                        // 查找该一级分类下的所有二级分类
                        List<CategoryEntity> categoryEntities =
                                getParent_cid(selectList, v.getCatId());
                        // 封装二级分类为指定格式
                        List<Catelog2Vo> catelog2Vos = null;
                        if (categoryEntities != null) {
                            catelog2Vos = categoryEntities.stream().map(item -> {
                                Catelog2Vo catelog2Vo =
                                        new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                                // 查找当前二级分类的三级分类
                                List<CategoryEntity> level3Catlog =
                                        getParent_cid(selectList, item.getCatId());
                                if(level3Catlog != null) {
                                    List<Catelog2Vo.Category3Vo> category3Vos = level3Catlog.stream().map(catlog -> {
                                        Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo();
                                        category3Vo.setId(catlog.getCatId().toString());
                                        category3Vo.setName(catlog.getName());
                                        category3Vo.setCatalog2Id(item.getCatId().toString());
                                        return category3Vo;
                                    }).collect(Collectors.toList());

                                    catelog2Vo.setCatalog3List(category3Vos);
                                }
                                return catelog2Vo;
                            }).collect(Collectors.toList());
                        }

                        return catelog2Vos;
                    }));

            // 从数据库查询到数据后，将数据放在 redis 中，这一步也应该放在锁范围内，防止数据还没放入缓存就释放锁
            // 导致其他线程再次查询数据库
            catalogJSON = JSON.toJSONString(collect);
            redisTemplate.opsForValue().set("catalogJSON", catalogJSON, 1, TimeUnit.DAYS);

            return collect;
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
        return selectList.stream()
                .filter(item -> item.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1. 收集当前节点id
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if(entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId())
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());

        return children;
    }

}