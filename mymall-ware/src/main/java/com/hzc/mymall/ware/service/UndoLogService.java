package com.hzc.mymall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:50:36
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

