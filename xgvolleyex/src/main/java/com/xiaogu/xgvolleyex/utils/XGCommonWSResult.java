package com.xiaogu.xgvolleyex.utils;
import android.text.TextUtils;

/**
 * Created by Phyllis on 15-4-11.
 */
public class XGCommonWSResult<T> {
    /* 调用正常 响应正常 返回的appstate=1 */
    public static final int APPSTATE_RESP_OK = 1;


    /* 无网络连接 */
    public static final int APPSTATE_NO_NETWORK_CONNECTION = -97;

    /* 调用参数为空 */
    public static final int APPSTATE_INVOKE_PARAMS_ERR = -96;

    /* 服务器响应超时 （服务器异常） */
    public static final int APPSTATE_RESP_TIMEOUT = -95;





    /***
     * appState用于指示ws程序的执行状况
     *  -3表示使用该ws的开发者的apiKey不正确
     * -2表示传递给ws接口的传入参数有误（即不符合要求，验证不通过）
     * -1表示ws程序执行失败，程序发生异常或者错误导致
     *  1表示ws程序执行成功，默认为执行正确
     *
     *  -99 表示客户端调用ws接口的程序发生异常
     *  -98 表示网络状况不佳
     */
    private int appState = 1 ;

    public static final String STATE_ERROR="FAILED";
    public static final String STATE_SUCCESS="SUCCESS";
    public static final String STATE_NO_DATA = "NO_DATA";


    private String action;

    private String v;

    private boolean success;
    private String retCode;

    private String msg;

    /***
     * ws接口返回的结果值
     */
    private T data;

    public XGCommonWSResult(){
        super();
    }

    public XGCommonWSResult(T data, String action, String v, String retCode, String msg) {
        super();
        this.data = data;
        this.action = action;
        this.v = v;
        this.retCode = retCode;
        this.msg = msg;
    }

    public boolean isSuccess(){
        if(!TextUtils.isEmpty(retCode)){
            return this.retCode.equals("SUCCESS");
        }else{
            return success;
        }
    }

    public T getResult() {
        return data;
    }

    public void setResult(T data) {
        this.data = data;
    }

    public int getAppState() {
        return appState;
    }

    public void setAppState(int appState) {
        this.appState = appState;
    }

    @Override
    public String toString() {
        return "WsResult [appState=" + appState + ", action=" + action + ", v=" + v + ", retCode="
                + retCode + ", msg=" + msg + ", data=" + data + "]";
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setState(String state){
        this.retCode = state;
    }
    public String getState(){
        return retCode;
    }
}
