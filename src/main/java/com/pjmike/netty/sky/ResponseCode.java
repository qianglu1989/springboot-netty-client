package com.pjmike.netty.sky;

/**
 * @ClassName ResponseCode
 * @Author QIANGLU
 * @Date 2020/3/20 1:37 下午
 * @Version 1.0
 */
public enum ResponseCode {

    RESP_TYPE_HEARTBEAT(0);

    private Integer code;

    private ResponseCode(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
