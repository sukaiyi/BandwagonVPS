package com.sukaiyi.bandwagonvps.utils;

import java.util.Locale;

/**
 * Created by sukaiyi on 2017/05/24.
 */

public class Switch {

    public static String b2GB(long b){
        return String.format(Locale.CHINA,"%.2f",b/1024.0/1024/1024) + "GB";
    }

    public static String b2MB(long b){
        return String.format(Locale.CHINA,"%.2f",b/1024.0/1024) + "MB";
    }

    public static String b2KB(long b){
        return String.format(Locale.CHINA,"%.2f",b/1024.0) + "KB";
    }

    public static String b2Any(long b){
        if(b<0){
            return "NULL";
        }else if(b < 1024){
            return b+"b";
        }else if(b < 1024*1024){
            return b2KB(b);
        }else if(b < 1024*1024*1024){
            return b2MB(b);
        } else {
            b2GB(b);
        }
        return b2GB(b);
    }
}
