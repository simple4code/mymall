package com.hzc.mymall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.member.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:38:05
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

