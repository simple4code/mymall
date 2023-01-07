package com.hzc.common.constant;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-30 17:44
 */
public class ProductConstant {
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");

        int code;
        String message;

        AttrEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum StatusEnum {
        NEW_SPU(0, "新建"), SPU_UP(1, "商品上架"), SPU_DOWN(2, "商品下架");

        int code;
        String message;

        StatusEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
