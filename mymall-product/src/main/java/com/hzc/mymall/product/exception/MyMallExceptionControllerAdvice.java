package com.hzc.mymall.product.exception;

import com.hzc.common.exception.BizCodeEnum;
import com.hzc.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  统一处理异常
 * </p>
 *
 * @author hzc
 * @since 2022-12-29 23:02
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.hzc.mymall.product.controller"})
public class MyMallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现异常：{}，异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            errors.put(item.getField(), item.getDefaultMessage());
        });

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errors);
    }

    // 其他没捕获的异常都会在这里进行处理，先暂时屏蔽掉，不然所有异常都会在这里被捕获，导致无法排查错误
//    @ExceptionHandler(value = Throwable.class)
//    public R handleException(Throwable throwable) {
//        log.error("出现异常：{}，异常类型：{}", throwable.getMessage(), throwable.getClass());
//        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
//    }
}
