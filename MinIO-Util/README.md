# MinIO-Util

基于 Spring Boot 的 MinIO 文件存储工具包，提供开箱即用的文件上传、下载、删除及大文件分片上传功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 开发环境 |
| Spring Boot | 3.3.7 | 应用框架 |
| MinIO SDK | 8.5.10 | MinIO 客户端 |
| AWS S3 SDK v2 | 2.25.0 | 分片上传支持（MinIO 兼容 S3 协议） |
| Hutool | 5.8.43 | Java 工具类库 |
| Lombok | 1.18.34 | 简化代码 |

## 功能特性

### 基础文件服务 (MinIOBasicFileService)
- 通用文件上传（支持多种路径策略）
- 图片上传（支持自动生成缩略图）
- 文件下载
- 单个/批量文件删除
- 存储桶管理（检查、创建、设置访问策略）

### 高级文件服务 (MinIOAdvancedFileService)
- 大文件分片上传（支持断点续传）
- 预签名 URL 生成（预览/下载）
- 上传进度查询
- 分片上传任务管理（初始化、合并、取消）

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.course</groupId>
    <artifactId>MinIO-Util</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加 MinIO 配置：

```yaml
minio:
  endpoint: http://localhost:9000        # MinIO 服务地址
  access-key: your-access-key            # 访问密钥
  secret-key: your-secret-key            # 安全密钥
  bucket-name: your-bucket               # 默认存储桶名称
  enable-bucket-path-prefix: false       # 是否启用日期路径前缀
  connect-timeout: 10000                 # 连接超时（毫秒）
  write-timeout: 60000                   # 写入超时（毫秒）
  read-timeout: 60000                    # 读取超时（毫秒）
  multipart-threshold: 10485760          # 分片上传阈值（10MB）
  multipart-chunk-size: 5242880          # 分片大小（5MB）
  multipart-thread-pool-size: 4          # 分片上传并发线程数
  presigned-url-expiry: 604800           # 预签名URL有效期（秒，默认7天）
```

### 3. 直接使用

引入依赖并配置后，即可通过依赖注入使用：

```java
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinIOBasicFileService minIOBasicFileService;
    private final MinIOAdvancedFileService minIOAdvancedFileService;
    
    // ...
}
```

## 使用示例

### 基础文件上传

#### 方式一：自定义路径上传

```java
@PostMapping("/upload")
public FileUploadResponse upload(@RequestParam("file") MultipartFile file) {
    FileUploadRequest request = FileUploadRequest.builder()
            .bucketName("my-bucket")           // 可选，不传使用默认桶
            .pathStrategy(PathStrategy.CUSTOM_PATH)
            .customPath("documents/reports")   // 文件存储路径
            .maxFileSize(50 * 1024 * 1024L)    // 限制50MB
            .build();
    
    return minIOBasicFileService.uploadFile(file, request);
}
// 文件路径: documents/reports/2026/05/07/{filename}  (开启日期前缀)
// 文件路径: documents/reports/{filename}             (关闭日期前缀)
```

#### 方式二：按用户维度归档

```java
@PostMapping("/upload-by-uid")
public FileUploadResponse uploadByUid(@RequestParam("file") MultipartFile file,
                                       @RequestParam("uid") String uid) {
    FileUploadRequest request = FileUploadRequest.builder()
            .pathStrategy(PathStrategy.UID_FILE_TYPE)
            .uid(uid)
            .fileType(MinioFileTypeEnum.DOC)
            .build();
    
    return minIOBasicFileService.uploadFile(file, request);
}
// 文件路径: {uid}/DOC/2026/05/07/{filename}  (开启日期前缀)
// 文件路径: {uid}/DOC/{filename}             (关闭日期前缀)
```

### 图片上传（含缩略图）

```java
@PostMapping("/upload-image")
public FileUploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
    ImageUploadRequest request = ImageUploadRequest.builder()
            .customPath("images/avatars")
            .thumbnail(true)           // 生成缩略图
            .thumbnailScale(0.3f)      // 缩略图缩放比例 30%
            .maxFileSize(10 * 1024 * 1024L)  // 限制10MB
            .build();
    
    return minIOBasicFileService.uploadImage(file, request);
}
// 原图路径: images/avatars/2026/05/07/avatar.jpg
// 缩略图:   images/avatars/2026/05/07/avatar_thumb.jpg
```

### 文件下载

```java
@GetMapping("/download")
public void download(@RequestParam String objectName,
                     HttpServletResponse response) throws IOException {
    InputStream inputStream = minIOBasicFileService.downloadFile(null, objectName);
    
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=" + objectName);
    
    IOUtils.copy(inputStream, response.getOutputStream());
    response.flushBuffer();
}
```

### 文件删除

```java
// 删除单个文件
minIOBasicFileService.removeFile("my-bucket", "documents/report.pdf");

// 批量删除
List<String> objectNames = Arrays.asList("file1.pdf", "file2.pdf");
minIOBasicFileService.removeFiles("my-bucket", objectNames);
```

### 大文件分片上传

分片上传适用于大文件（默认超过 10MB 自动切换），支持断点续传。

#### 步骤一：初始化分片上传

```java
@PostMapping("/multipart/init")
public MultipartUploadInitResponse initMultipartUpload(@RequestBody MultipartUploadInitRequest request) {
    // request 包含：fileName, fileSize, chunkSize(可选), contentType(可选) 等
    return minIOAdvancedFileService.initiateMultipartUpload(request);
}
// 返回：uploadId, objectName, totalParts, 各分片预签名URL等信息
```

#### 步骤二：客户端直传分片

客户端拿到预签名 URL 后，直接向 MinIO 上传分片（不经过服务端），每个分片上传成功后返回 ETag。

```javascript
// 前端示例
for (let i = 0; i < parts.length; i++) {
    const response = await fetch(parts[i].presignedUrl, {
        method: 'PUT',
        body: fileChunk,
        headers: { 'Content-Type': 'application/octet-stream' }
    });
    const etag = response.headers.get('ETag');
    // 保存 partNumber 和 etag
}
```

#### 步骤三：合并分片

```java
@PostMapping("/multipart/complete")
public FileUploadResponse completeMultipartUpload(@RequestBody MultipartUploadCompleteRequest request) {
    // request 包含：objectName, uploadId, parts(List<PartETag>)
    return minIOAdvancedFileService.completeMultipartUpload(request);
}
```

#### 查询上传进度

```java
MultipartUploadProgressResponse progress = minIOAdvancedFileService.getUploadProgress(
    objectName, 
    uploadId, 
    totalParts, 
    bucketName
);
// 返回：已上传分片列表、上传百分比、已上传大小等
```

#### 取消上传

```java
// 上传失败或超时时调用，清理已上传的碎片
minIOAdvancedFileService.abortMultipartUpload(objectName, uploadId, bucketName);
```

### 获取文件 URL

```java
// 获取临时预览 URL（适用于私有桶）
String previewUrl = minIOAdvancedFileService.getPreviewUrl(bucketName, objectName);

// 获取永久公开 URL（适用于公开桶）
String publicUrl = minIOAdvancedFileService.getPublicUrl(bucketName, objectName);
```

### 存储桶管理

```java
// 检查存储桶是否存在，不存在则创建
minIOBasicFileService.checkBucket("my-bucket");

// 设置存储桶为公开读
minIOBasicFileService.setBucketPolicy("my-bucket", true);

// 设置存储桶为私有
minIOBasicFileService.setBucketPolicy("my-bucket", false);
```

## 路径策略说明

### CUSTOM_PATH（自定义路径）

```
[customPath/][yyyy/MM/dd/]{filename}
```

- `customPath`：自定义目录前缀
- 开启日期前缀时自动添加 `yyyy/MM/dd/`

示例：
```
配置: enable-bucket-path-prefix=true
customPath = "docs/reports"
结果: docs/reports/2026/05/07/report.pdf
```

### UID_FILE_TYPE（用户维度归档）

```
{uid}/{fileType.dirName}/[yyyy/MM/dd/]{filename}
```

- `uid`：用户唯一标识
- `fileType`：文件类型枚举（DOC、IMAGE、VIDEO 等）

示例：
```
配置: enable-bucket-path-prefix=true
uid = "user123", fileType = IMAGE
结果: user123/IMAGE/2026/05/07/avatar.jpg
```

## 文件类型枚举

| 枚举值 | 目录名 | 支持的文件扩展名 |
|--------|--------|------------------|
| DOC | DOC | doc, docx |
| EXCEL | EXCEL | xls, xlsx |
| PPT | PPT | ppt, pptx |
| PDF | PDF | pdf |
| IMAGE | IMAGE | jpg, jpeg, png, gif, webp |
| VIDEO | VIDEO | mp4, avi, mov, wmv, flv, mkv, webm |
| MARKDOWN | MarkDown | md, markdown |
| TXT | TXT | txt |
| OTHER | OTHER | 其他类型 |

## 支持的文件类型

工具默认支持以下文件扩展名：

```
图片: jpg, png, gif, webp, jpeg
文档: pdf, doc, docx, md, txt, xls, xlsx, ppt, pptx
代码: html, css, js
视频: mp4, avi, mov, wmv, flv
```

如需扩展支持更多类型，可修改 `MinIOBasicFileServiceImpl` 中的 `ALLOWED_EXTENSIONS` 集合。

## 注意事项

1. **分片上传阈值**：文件大小超过 `multipart-threshold`（默认 10MB）才会启用分片上传
2. **分片大小**：最小 5MB（S3 协议限制），最后一片可以小于此值
3. **预签名 URL**：有效期默认 7 天，可在配置中调整
4. **断点续传**：通过 `getUploadProgress` 查询已上传分片，实现断点续传
5. **缩略图**：目前仅支持图片格式生成缩略图

## License

MIT
