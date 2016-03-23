

package com.xiaogu.xgvolleyex.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

public class NetworkUtils
{



    public static boolean isNetworkEnable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null)
        {
            return network.isAvailable();
        }
        return false;
    }

    public static boolean isSimExist(Context context)
    {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean isWifiEnable(Context context)
    {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.isWifiEnabled();
    }

    public static void setWifiEnable(Context context, boolean enable)
    {
//        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//        wm.setWifiEnabled(enable);
    }

    public static boolean getMobileNetEnable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        return mobile == State.CONNECTED;
    }

    public static void setMobileNetEnable(Context context, boolean enable)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try
        {
            boolean isMobileDataEnable = getMobileNetEnable(context);
            if (enable != isMobileDataEnable)
            {
                invokeBooleanArgMethod(cm, "setMobileDataEnabled", enable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    @SuppressWarnings ({ "rawtypes", "unchecked" })
    private static Object invokeBooleanArgMethod(ConnectivityManager cm, String methodName, boolean value)
        throws Exception
    {
        Class ownerClass = cm.getClass();
        Class[] argsClass = new Class[1];
        argsClass[0] = boolean.class;

        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(cm, value);
    }

    // =======================================
    // Inner Classes/Interfaces
    // =======================================
}
