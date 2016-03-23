package com.xiaogu.xgvolleyex.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaogu.xgvolleyex.BaseWebRequest;

/**
 * Created by Phyllis on 15-4-15.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BaseWebRequest.tryToResumeRequest(context);
    }

}
