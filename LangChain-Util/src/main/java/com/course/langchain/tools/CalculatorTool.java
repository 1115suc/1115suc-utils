package com.course.langchain.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Slf4j
@Component
public class CalculatorTool {

    /** 除法及开方等无法整除运算的精度（有效数字位数） */
    private static final MathContext MATH_CONTEXT = new MathContext(50);

    @Tool("大数相加，支持任意精度的整数或小数，返回它们的和")
    public String add(@P("加数，数字字符串") String a, @P("被加数，数字字符串") String b) {
        log.info("调用工具 add: {} + {}", a, b);
        return toBigDecimal(a).add(toBigDecimal(b)).toPlainString();
    }

    @Tool("大数相减，支持任意精度，返回 a 减去 b 的差")
    public String subtract(@P("被减数，数字字符串") String a, @P("减数，数字字符串") String b) {
        log.info("调用工具 subtract: {} - {}", a, b);
        return toBigDecimal(a).subtract(toBigDecimal(b)).toPlainString();
    }

    @Tool("大数相乘，支持任意精度，返回它们的积")
    public String multiply(@P("乘数，数字字符串") String a, @P("被乘数，数字字符串") String b) {
        log.info("调用工具 multiply: {} * {}", a, b);
        return toBigDecimal(a).multiply(toBigDecimal(b)).toPlainString();
    }

    @Tool("大数相除，支持任意精度，返回 a 除以 b 的商（最多保留 50 位有效数字）")
    public String divide(@P("被除数，数字字符串") String a, @P("除数，数字字符串，不能为 0") String b) {
        log.info("调用工具 divide: {} / {}", a, b);
        BigDecimal divisor = toBigDecimal(b);
        if (divisor.signum() == 0) {
            throw new IllegalArgumentException("除数不能为 0");
        }
        return toBigDecimal(a).divide(divisor, MATH_CONTEXT).toPlainString();
    }

    @Tool("大数取余，返回 a 除以 b 的余数")
    public String remainder(@P("被除数，数字字符串") String a, @P("除数，数字字符串，不能为 0") String b) {
        log.info("调用工具 remainder: {} % {}", a, b);
        BigDecimal divisor = toBigDecimal(b);
        if (divisor.signum() == 0) {
            throw new IllegalArgumentException("除数不能为 0");
        }
        return toBigDecimal(a).remainder(divisor).toPlainString();
    }

    @Tool("计算 base 的 exponent 次幂，exponent 为非负整数")
    public String power(@P("底数，数字字符串") String base, @P("指数，非负整数") int exponent) {
        log.info("调用工具 power: {} ^ {}", base, exponent);
        if (exponent < 0) {
            throw new IllegalArgumentException("指数必须为非负整数");
        }
        return toBigDecimal(base).pow(exponent).toPlainString();
    }

    @Tool("计算一个非负数的平方根，支持任意精度（最多保留 50 位有效数字）")
    public String sqrt(@P("被开方数，数字字符串，必须大于等于 0") String value) {
        log.info("调用工具 sqrt: sqrt({})", value);
        BigDecimal number = toBigDecimal(value);
        if (number.signum() < 0) {
            throw new IllegalArgumentException("被开方数不能为负数");
        }
        return number.sqrt(MATH_CONTEXT).toPlainString();
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数字格式: " + value);
        }
    }
}
