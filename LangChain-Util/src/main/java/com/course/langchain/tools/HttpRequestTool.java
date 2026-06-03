package com.course.langchain.tools;

import cn.hutool.http.HttpUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpRequestTool {

    private static final int TIMEOUT_MILLIS = 10_000;

    @Tool("向指定 URL 发送 HTTP GET 请求，返回响应的文本内容。适合查询网页或调用开放接口")
    public String get(@P("完整的请求地址，必须以 http:// 或 https:// 开头") String url) {
        log.info("调用工具 httpGet: {}", url);
        try {
            return HttpUtil.createGet(url).timeout(TIMEOUT_MILLIS).execute().body();
        } catch (Exception e) {
            log.error("httpGet 请求失败: {}", url, e);
            return "请求失败：" + e.getMessage();
        }
    }

    @Tool("向指定 URL 发送 HTTP POST 请求，请求体为 JSON 字符串，返回响应的文本内容")
    public String postJson(@P("完整的请求地址，必须以 http:// 或 https:// 开头") String url,
                           @P("JSON 格式的请求体字符串") String jsonBody) {
        log.info("调用工具 httpPostJson: {}", url);
        try {
            return HttpUtil.createPost(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .timeout(TIMEOUT_MILLIS)
                    .execute()
                    .body();
        } catch (Exception e) {
            log.error("httpPostJson 请求失败: {}", url, e);
            return "请求失败：" + e.getMessage();
        }
    }
}
