package com.hzc.common.exception;

/**
 * <p>
 *  错误码和错误信息定义类
 *  1，错误码定义规则为5个数字
 *  2，前两位表示业务场景，最后三位表示错误码。例如：100001。10：通用 001：系统未知异常
 *  3，维护错误码后需要维护错误描述，将他们定义为枚举形式
 *  错误码列表：
 *      10：通用
 *          001：参数格式校验
 *          002：短信验证码频率过高
 *      11：商品
 *      12：订单
 *      13：购物车
 *      14：物流
 *      15：用户
 *      21：库存
 * </p>
 *
 * @author hzc
 * @since 2022-12-29 23:14
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率过高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "用户已存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "账号或者密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足");

    private Integer code;
    private String message;

    BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
