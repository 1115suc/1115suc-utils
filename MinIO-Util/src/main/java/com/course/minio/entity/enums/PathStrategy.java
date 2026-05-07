package com.course.minio.entity.enums;

public enum PathStrategy {
    /** 按用户维度归档: {uid}/{fileType.dirName}/[yyyy/MM/dd/]{filename} */
    UID_FILE_TYPE,
    /** 按自定义路径归档: [customPath/][yyyy/MM/dd/]{filename} */
    CUSTOM_PATH
}