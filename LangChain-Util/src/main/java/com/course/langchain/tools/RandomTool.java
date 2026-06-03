package com.course.langchain.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class RandomTool {

    @Tool("生成一个指定范围内的随机整数，包含 min 和 max 边界")
    public int randomInt(@P("最小值（含）") int min, @P("最大值（含）") int max) {
        log.info("调用工具 randomInt: [{}, {}]", min, max);
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Tool("生成一个不带横线的 32 位 UUID 字符串")
    public String uuid() {
        String result = UUID.randomUUID().toString().replace("-", "");
        log.info("调用工具 uuid: {}", result);
        return result;
    }
}
