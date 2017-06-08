package com.sukaiyi.bandwagonvps.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.sukaiyi.bandwagonvps.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        mDefaultHandler.uncaughtException(thread, ex);
    }

    private void handleException(Throwable ex) {
        collectDeviceInfo(mContext);
        saveCrashInfo2File(mContext, ex);
    }

    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    private void saveCrashInfo2File(Context context, Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        sb.append(writer.toString());

        long timestamp = System.currentTimeMillis();
        String time = formatter.format(new Date());
        String fileName = "crash-" + time + "-" + timestamp + ".log";
        try {
            File file = context.getExternalCacheDir();
            if (file == null) {
                return;
            }
            String path = file.getAbsolutePath() + "/" + fileName;
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否存在崩溃日志
     *
     * @return 是否存在
     */
    private boolean checkCrash() {
        java.io.File file = mContext.getExternalCacheDir();
        for (java.io.File f : file.listFiles()) {
            String fileName = f.getName();
            boolean r = fileName.matches("crash.+\\.log");
            if (r) {
                return true;
            }
        }
        return false;
    }

    public void sendCrashLog(Context context) {
        if (checkCrash()) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            String[] tos = {"im.sukaiyi@gmail.com"};
            intent.putExtra(Intent.EXTRA_EMAIL, tos);
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.str_crash_report_tips));

            ArrayList<Uri> fileUris = new ArrayList<>();
            java.io.File file = mContext.getExternalCacheDir();
            for (java.io.File f : file.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }
                String fileName = f.getName();
                boolean r = fileName.matches("crash.+\\.log");
                if (r) {
                    fileUris.add(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", f));
                }
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
            intent.setType("text/plain");
            Intent.createChooser(intent, context.getString(R.string.str_choose_email_tips));
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.str_no_log_file_tips), Toast.LENGTH_LONG).show();
        }
    }
}