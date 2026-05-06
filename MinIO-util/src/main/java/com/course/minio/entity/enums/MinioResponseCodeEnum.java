package com.course.minio.entity.enums;

import com.course.minio.exception.CommonException;

/**
 * MinIO 响应码枚举
 */
public enum MinioResponseCodeEnum {

    CHECK_BUCKET_FAIL(1001, "检查存储桶失败", 500),
    SET_POLICY_FAIL(1002, "设置存储桶策略失败", 500),
    UPLOAD_FAIL(1003, "文件上传失败", 500),
    GET_PREVIEW_URL_FAIL(1004, "获取预览URL失败", 500),
    DOWNLOAD_FAIL(1005, "下载文件失败", 500),
    DELETE_FILE_FAIL(1006, "删除文件失败", 500),
    DELETE_BATCH_FAIL(1007, "批量删除文件失败", 500),
    FILE_EXCEPTION(1008, "文件异常", 500),
    FILE_ALREADY_EXISTS(1009, "文件已存在，请更改文件名或选择其他文件", 409);

    private final int code;
    private final String msg;
    private final int status;

    MinioResponseCodeEnum(int code, String msg, int status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public int getStatus() {
        return status;
    }

    /**
     * 转换为 CommonException
     */
    public CommonException toException() {
        return new CommonException(this.msg, this.code, this.status);
    }

    /**
     * 转换为 CommonException
     */
    public CommonException toException(Throwable cause) {
        return new CommonException(this.msg, this.code, this.status, cause);
    }
}