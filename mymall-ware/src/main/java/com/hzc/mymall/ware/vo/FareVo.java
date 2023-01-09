package com.hzc.mymall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 13:20
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
