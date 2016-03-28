package com.xiaogu.xgvolleyex;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import com.xiaogu.xgvolleyex.utils.JsonUtils;
import com.xiaogu.xgvolleyex.utils.NetworkStateReceiver;
import com.xiaogu.xgvolleyex.utils.NetworkUtils;

import org.json.JSONObject;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Request will not be performed if the certificate factory can not be created when you use the https
 * mode
 * Created by Phyllis on 15-4-11.
 */
public abstract class BaseWebRequest {
    private RequestQueue mQueue;
    private static RequestQueue mTryBestQueue;
    private int mTimeOut = 10 * 1000;
    private static List<Request> mSuspendedRequest;
    private static boolean mIsRegistered = false;


    public BaseWebRequest(Context context) {

        mQueue = getRequestQueue(context.getApplicationContext());

    }

    private void setTimeOut(int timeOut) {
        mTimeOut = timeOut;
    }

    private RequestQueue getRequestQueue(Context context) {
        if(isHttpsMode()) {
            return createHttpsQueue(context);

        } else {
            return Volley.newRequestQueue(context);
        }
    }

    private RequestQueue createHttpsQueue(Context context) {
        SSLSocketFactory factory = getSSLSocketFactory(context);

        HurlStack stack = new HurlStack(null, factory);
        return Volley.newRequestQueue(context, stack);

    }

    private SSLSocketFactory getSSLSocketFactory(Context context) {
        int certificateId = getCertificateId();
        if(certificateId == 0) {
            throw new RuntimeException(
                    "Id of certificate file not provided.Please override the method " +
                            "getCertificateId() and return the right id");
        }

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory
                    .generateCertificate(context.getResources().openRawResource(certificateId));


            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            TrustManagerFactory managerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            managerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, managerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * default request mode:post
     *
     * @return The  request used to request data from web server,you can use the request to
     * control the request like cancel it.
     */
    protected Request sendRequest(String url, Map<String,Object> params, TypeToken targetType,
                                  OnJobFinishListener listener) {
        return sendRequest(Request.Method.POST, url, params, targetType, listener);
    }

    /**
     * @param params Null is allowed.
     *               It will be transformed to json format and added to request body when in POST request method.
     *              However, it will be combined with the url when in GET request method .
     */
    protected Request sendRequest(int httpMethod, String url, Map<String,Object> params, TypeToken targetType,
                                  OnJobFinishListener listener) {
        Request jsonObjectRequest = getRequest(httpMethod, url, params,
                                               targetType, listener
                                              );

        mQueue.add(jsonObjectRequest);
        VolleyLog.d("sent request to url"+jsonObjectRequest.getUrl());
        return jsonObjectRequest;


    }

    protected Request sendRequest(int httpMethod, String url, Map<String,Object> params, TypeToken targetType,
                                  OnJobFinishListener listener,
                                  Response.ErrorListener netErrorListener) {

        Request jsonObjectRequest = getRequest(httpMethod, url, params,
                                               targetType, listener,
                                               netErrorListener
                                              );
        mQueue.add(jsonObjectRequest);
        VolleyLog.d( "sent request to url:" + jsonObjectRequest.getUrl());
        return jsonObjectRequest;
    }

    /**
     * the request will go on and on until it gets response
     * @param netErrorListener the call back listener when net error happens,null is allowed
     * @return The  request used to request data from web server
     */

    protected Request tryBestToRequest(int httpMethod, String url, Map<String,Object> params,
                                       TypeToken targetType,
                                       Context context,
                                       OnJobFinishListener listener,
                                       Response.ErrorListener netErrorListener) {
        tryToReceiveNetworkState(context);
        if(mTryBestQueue == null) {
            mTryBestQueue = getRequestQueue(context.getApplicationContext());
        }


        if(netErrorListener == null)
            netErrorListener = getDefaultErrorListener(listener);
        Request jsonObjectRequest = getRequest(httpMethod, url, params,
                                               targetType, listener,
                                               netErrorListener
                                              );
        RetryPolicy policy = new DefaultRetryPolicy(mTimeOut, Integer.MAX_VALUE,
                                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        //if the network is not available we just cache the request instead of add to the queue;
        if(!NetworkUtils.isNetworkEnable(context)) {
            suspendTheRequest(jsonObjectRequest);
            return jsonObjectRequest;
        }
        mTryBestQueue.add(jsonObjectRequest);
        VolleyLog.d("sent request to url:" + jsonObjectRequest.getUrl());
        return jsonObjectRequest;
    }

    private void tryToReceiveNetworkState(Context context) {
        if(mIsRegistered) {
            return;
        }
        NetworkStateReceiver mReceiver = new NetworkStateReceiver();
        context.registerReceiver(mReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mIsRegistered = true;
    }

    private void suspendTheRequest(Request request) {
        if(mSuspendedRequest == null) {
            mSuspendedRequest = new ArrayList<>(1);
        }
        mSuspendedRequest.add(request);
    }

    private static void restartSuspendedRequest() {
        if(mSuspendedRequest == null || mSuspendedRequest.isEmpty())
            return;
        for(Request request : mSuspendedRequest) {
            mTryBestQueue.add(request);
        }
        mSuspendedRequest.clear();
    }

    private String getParamsJsonStr(Object params) {
        String object = null;
        if(params != null) {
            if(params instanceof JSONObject || params instanceof String) {
                object = params.toString();
                return object;
            }
            String jsonString = JsonUtils.toJson(params);
            if(!TextUtils.isEmpty(jsonString)) {
                object = jsonString;
            }
        }
        return object;
    }

    private Request getRequest(int httpMethodName, String url,
                               Map<String,Object> params,
                               final TypeToken targetType,
                               final OnJobFinishListener
                                       listener) {
        Response.ErrorListener errorListener = getErrorListener(listener);
        if (errorListener == null) {
            errorListener = getDefaultErrorListener(listener);
        }
        return getRequest(httpMethodName, url, params, targetType, listener,
                          errorListener);

    }

    protected Response.ErrorListener getErrorListener(final OnJobFinishListener listener) {
        return null;
    }

    private Response.ErrorListener getDefaultErrorListener(
            final OnJobFinishListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(listener != null) {
                    listener.onWebCallFinish(false, error);
                }
                if(!TextUtils.isEmpty(error.getMessage())){
                   VolleyLog.e(VolleyLog.TAG, error.getMessage());
                }

            }
        };
    }

    /**
     * You can override this method to return a custom request you need,if you do this ,it is
     * your responsibility to init the request parameters like the RetryPolicy and shouldcatch etc.
     * @param params         This can be an object ,if it is a String,then you it should be a
     *                       jsonString
     * @return The request will be added to the request queue;
     */
    protected Request getRequest(int httpMethodName, String url,
                                 Map<String,Object> params,
                                 final TypeToken targetType,
                                 final OnJobFinishListener listener,
                                 final Response.ErrorListener errorListener) {
        String jsonParams = getParamsJsonStr(params);

        String requestUrl = url;
        if(httpMethodName == Request.Method.GET && params!=null && !params.isEmpty()){
            requestUrl = assembleGetParams(url,params);

        }
        JsonStringRequest req = new JsonStringRequest(httpMethodName, requestUrl,
                jsonParams,
                getSuccessListener(targetType, listener),
                errorListener);
        req.setExtraHeaders(getHeaders());
        req.setShouldCache(false);
        RetryPolicy retryPolicy = new DefaultRetryPolicy(mTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(retryPolicy);

        return req;

    }
    private String assembleGetParams(String originalUrl,Map<String,Object> params) {
        Set<Map.Entry<String,Object>> entrySet = params.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            if(!originalUrl.contains("?")){
                originalUrl = originalUrl+"?"+entry.getKey()+"="+entry.getValue();
            }else{
                originalUrl=originalUrl+"&"+entry.getKey()+"="+entry.getValue();
            }
        }
        return originalUrl;
    }

    protected Response.Listener<String> getSuccessListener(final TypeToken targetType,
                                                           final OnJobFinishListener listener) {
        return new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                if(listener == null) {
                    return;
                }
                if(response == null) {
                    listener.onWebCallFinish(false,
                                             null);
                } else {
                        VolleyLog.d(VolleyLog.TAG, response);
                    Object result;
                    if(isAutoParseJson()) {
                        result = JsonUtils
                                .fromJson(response,
                                          targetType);
                        if(result == null) {
                            Class newResult = targetType.getRawType();
                            try {
                                result = newResult.newInstance();
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        result = response;
                    }

                    listener.onWebCallFinish(true,
                                             result);
                }

            }
        };
    }

    public void cancelAllRequest() {
        if(mQueue != null) {
            RequestQueue.RequestFilter mRequestFilter = new RequestQueue.RequestFilter() {
                public boolean apply(Request<?> request) {
                    return true;
                }
            };
            mQueue.cancelAll(mRequestFilter);
        }
    }

    public static void tryToResumeRequest(Context context) {
        if(mTryBestQueue == null)
            return;
        if(NetworkUtils.isNetworkEnable(context)) {
            restartSuspendedRequest();
        }

    }


    /**
     * This method should be override if need to set custom headers
     */
    protected Map<String, String> getHeaders() {
        return null;
    }

    /**
     * This method should be override and return true if need to use https mode
     * default value is false;
     */
    protected boolean isHttpsMode() {
        return false;
    }

    /**
     * Override this method if you need to handle the json string yourself
     *
     * @return True means the parent class will parse the json for you
     */
    protected boolean isAutoParseJson() {
        return true;
    }


    /**
     * The certificate should be placed in the folder named "raw"
     *
     * @return https certificate id
     */
    protected int getCertificateId() {
        return 0;
    }

    public interface OnJobFinishListener {
        public void onWebCallFinish(boolean isSuccess, Object data);
    }

}
