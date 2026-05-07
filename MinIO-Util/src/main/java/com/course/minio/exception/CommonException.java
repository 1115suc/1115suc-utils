package com.course.minio.exception;

/**
 * MinIO 自定义异常
 */
public class CommonException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String msg; //异常中的信息
    private int code;
    private int status; //http状态码，按照http协议规范，如：200,201,400等

    public CommonException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CommonException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public CommonException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public CommonException(String msg, int code, int status) {
        super(msg);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

    public CommonException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public CommonException(String msg, int code, int status, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
