package com.xiaogu.xgvolleyex;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/**
 * Created by Phyllis on 15-5-23.
 */
public abstract class BaseWebUploadImgRequest extends BaseWebRequest {

    public BaseWebUploadImgRequest(Context context) {
        super(context);
    }

    @Override
    protected Request getRequest(int httpMethodName, String url, Object params,
                                 TypeToken targetType,
                                 OnJobFinishListener listener,
                                 Response.ErrorListener errorListener) {

        if(!(params instanceof HashMap)) {
            throw new RuntimeException(
                    "The third parameter should be an instanceof HashMap<String,Object>");
        } else {
            HashMap<String, Object> maps = (HashMap<String, Object>)params;
            return new MultipartRequest(httpMethodName, url, maps,
                                                            getSuccessListener(targetType,listener),
                                                            errorListener);
        }

    }
}
