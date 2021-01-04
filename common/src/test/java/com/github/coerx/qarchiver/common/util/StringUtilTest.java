package com.github.coerx.qarchiver.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilTest {
    @Test
    public void validateCronExpressionTest(){
        Assertions.assertTrue(StringUtil.validateCronExpression("47 6 * * 7"));
        Assertions.assertTrue(StringUtil.validateCronExpression("47 6 * * fri"));
        Assertions.assertFalse(StringUtil.validateCronExpression("66 6 * * 7"));
        Assertions.assertFalse(StringUtil.validateCronExpression("66 6 * * fria"));
        Assertions.assertTrue(StringUtil.validateCronExpression("47     6 *  jan 7"));
    }
}
