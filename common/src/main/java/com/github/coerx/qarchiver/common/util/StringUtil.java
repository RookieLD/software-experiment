package com.github.coerx.qarchiver.common.util;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class StringUtil {
    private static final CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    public static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean validateCronExpression(String cronExpression){
        if (cronExpression == null) {
            return false;
        }
        try{
            Cron cron = cronParser.parse(cronExpression);
            cron.validate();
        } catch (Exception e){
            return false;
        }
        return true;
    }
}
