package com.course.langchain.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class DateTimeTool {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool("获取当前的日期和时间，格式为 yyyy-MM-dd HH:mm:ss")
    public String now() {
        String result = LocalDateTime.now().format(DATETIME_FORMATTER);
        log.info("调用工具 now: {}", result);
        return result;
    }

    @Tool("获取今天的日期，格式为 yyyy-MM-dd")
    public String today() {
        String result = LocalDate.now().format(DATE_FORMATTER);
        log.info("调用工具 today: {}", result);
        return result;
    }

    @Tool("计算两个日期之间相差的天数，日期格式为 yyyy-MM-dd")
    public long daysBetween(@P("开始日期，格式 yyyy-MM-dd") String startDate,
                            @P("结束日期，格式 yyyy-MM-dd") String endDate) {
        log.info("调用工具 daysBetween: {} ~ {}", startDate, endDate);
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        return ChronoUnit.DAYS.between(start, end);
    }

    @Tool("在指定日期上增加（或减少）若干天，返回新的日期，格式为 yyyy-MM-dd")
    public String plusDays(@P("基准日期，格式 yyyy-MM-dd") String date,
                           @P("要增加的天数，负数表示往前推") long days) {
        log.info("调用工具 plusDays: {} + {} 天", date, days);
        return LocalDate.parse(date, DATE_FORMATTER).plusDays(days).format(DATE_FORMATTER);
    }

    @Tool("查询指定日期是星期几，日期格式为 yyyy-MM-dd")
    public String dayOfWeek(@P("日期，格式 yyyy-MM-dd") String date) {
        log.info("调用工具 dayOfWeek: {}", date);
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        return switch (localDate.getDayOfWeek()) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}
