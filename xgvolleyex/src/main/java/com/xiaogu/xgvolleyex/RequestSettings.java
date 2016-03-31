package com.xiaogu.xgvolleyex;

import android.content.Context;

import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Phyllis on 16/3/31.
 */
public class RequestSettings {
    private boolean isHttpsMode;
    private boolean isVerifyAllHostname;
    private int certificatedFileResID;
    private HttpStack httpStack;

    public void setIsHttpsMode(boolean isHttpsMode) {
        this.isHttpsMode = isHttpsMode;
    }

    public void setIsVerifyAllHostname(boolean isVerifyAllHostname) {
        this.isVerifyAllHostname = isVerifyAllHostname;
    }

    public void setCertificatedFileResID(int certificatedFileResID) {
        this.certificatedFileResID = certificatedFileResID;
    }

    public void setHttpStack(HttpStack httpStack) {
        this.httpStack = httpStack;
    }

    public boolean isHttpsMode() {
        return isHttpsMode;
    }

    public boolean isVerifyAllHostname() {
        return isVerifyAllHostname;
    }

    public int getCertificatedFileResID() {
        return certificatedFileResID;
    }

    public HttpStack getHttpStack(Context context) {
        if(httpStack == null){
            if(isHttpsMode){
                SSLSocketFactory factory = SSLSocketFactoryCreator.getSSLSocketFactory(context,certificatedFileResID);
                if(isVerifyAllHostname){
                    httpStack = new VerifyAllHostNameHurlStack(null, factory);
                }else{
                    httpStack = new HurlStack(null,factory);
                }
            }
        }
        return httpStack;
    }
}
