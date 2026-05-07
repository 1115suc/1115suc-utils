package com.course.minio.entity.enums;

import cn.hutool.core.util.StrUtil;

/**
 * MinIO 文件类型
 */
public enum MinioFileTypeEnum {
    PPT("PPT"),
    DOC("DOC"),
    MARKDOWN("MarkDown"),
    PDF("PDF"),
    EXCEL("EXCEL"),
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    TXT("TXT"),
    OTHER("OTHER");

    private final String dirName;

    MinioFileTypeEnum(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }

    public static MinioFileTypeEnum from(String value) {
        if (StrUtil.isBlank(value)) {
            return OTHER;
        }
        String v = value.trim();
        for (MinioFileTypeEnum t : values()) {
            if (StrUtil.equalsIgnoreCase(t.name(), v) || StrUtil.equalsIgnoreCase(t.dirName, v)) {
                return t;
            }
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "md", "markdown")) {
            return MARKDOWN;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "docx", "doc")) {
            return DOC;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "pptx", "ppt")) {
            return PPT;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "xlsx", "xls", "excel")) {
            return EXCEL;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "jpg", "jpeg", "png", "gif", "webp", "image")) {
            return IMAGE;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "video")) {
            return VIDEO;
        }
        if (StrUtil.equalsAnyIgnoreCase(v, "txt", "text")) {
            return TXT;
        }
        if (StrUtil.equalsIgnoreCase(v, "pdf")) {
            return PDF;
        }
        return OTHER;
    }
}