package com.sukaiyi.bandwagonvps.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by sukaiyi on 2017/05/27.
 */

public class Utils {
    public static void copyToClipboard(Context context,String str){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bandwagon VPS",str);
        clipboard.setPrimaryClip(clip);
    }
}
