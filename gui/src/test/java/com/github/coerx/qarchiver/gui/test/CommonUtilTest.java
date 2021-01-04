package com.github.coerx.qarchiver.gui.test;

import com.github.coerx.qarchiver.gui.common.CommonUtil;
import org.junit.jupiter.api.Test;

public class CommonUtilTest {

    @Test
    public void humanReadableByteCountBinTest() {
        System.out.println(CommonUtil.humanReadableByteCountBin(2048));
    }
}
