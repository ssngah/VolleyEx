package com.xiaogu.xgvolleyex;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Created by Phyllis on 15-4-13.
 */


/**
 * A request for retrieving a json format string response body at a given URL,
 * allowing for an
 *
 */
public class JsonStringRequest extends JsonRequest<String> {
    private Map<String, String> extraHeaders;

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param requestBody   A {@link String} to post with the request. Null is allowed and
     *                      indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonStringRequest(int method, String url, String requestBody,
                             Response.Listener<String> listener,
                             Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener,
              errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

        try {
            String jsonString = new String(response.data,
                                           HttpHeaderParser.parseCharset(response.headers,
                                                                         PROTOCOL_CHARSET));

            return Response.success(jsonString,
                                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
    public void setExtraHeaders(Map<String,String> header){
        extraHeaders = header;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(extraHeaders!=null){
            return extraHeaders;
        }
        return super.getHeaders();
    }
}
