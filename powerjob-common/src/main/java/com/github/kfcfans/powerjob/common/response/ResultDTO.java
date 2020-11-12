package com.github.kfcfans.powerjob.common.response;

import com.github.kfcfans.powerjob.common.OmsSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 请求返回的结果对象
 *
 * @author tjq
 * @since 2020/3/30
 */
@Getter
@Setter
@ToString
public class ResultDTO<T> implements OmsSerializable {

    private boolean success;
    // 数据（success为 true 时存在）
    private T data;
    // 错误信息（success为 false 时存在）
    private String message;
    //返回值
    private String code;
    public static <T> ResultDTO<T> success(T data) {
        ResultDTO<T> r = new ResultDTO<>();
        r.success = true;
        r.data = data;
        return r;
    }
    public static <T> ResultDTO<T> success(T data,String message,String code) {
        ResultDTO<T> r = new ResultDTO<>();
        r.message = message;
        r.code=code;
        r.success = true;
        r.data = data;
        return r;

    }


    public static <T> ResultDTO<T> success(String message,String code) {
        ResultDTO<T> r = new ResultDTO<>();
        r.success = true;
        r.message = message;
        r.code=code;
        return r;
    }
    public static <T> ResultDTO<T> failed(String message) {
        ResultDTO<T> r = new ResultDTO<>();
        r.success = false;
        r.message = message;
        return r;
    }
    public static <T> ResultDTO<T> failed(Throwable t) {
        return failed(ExceptionUtils.getStackTrace(t));
    }

}
