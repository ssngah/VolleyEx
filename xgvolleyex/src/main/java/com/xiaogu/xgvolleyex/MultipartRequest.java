package com.xiaogu.xgvolleyex;

import android.graphics.Bitmap;

import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.HttpEntity;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Phyllis on 15-5-23.
 */
public class MultipartRequest extends JsonStringRequest {
    private MultipartEntityBuilder mEntityBuilder;
    private HttpEntity             mEntity;
    public MultipartProgressListener multipartProgressListener;
    private long fileLength = 0L;

    public MultipartRequest(int method, String url, HashMap<String, Object> files,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener,MultipartProgressListener progressListener) {
        super(method, url, "", listener, errorListener);
        multipartProgressListener = progressListener;
        buildMultipartEntity(files);
    }

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     * @param files         The File to post with the request,Null is not allowed.
     */

    public MultipartRequest(int method, String url, HashMap<String, Object> files,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, "", listener, errorListener);

        buildMultipartEntity(files);
    }

    /**
     * 注意Map里面的value中，将会使用toString方法来获得值
     *
     * @param params
     */
    private void buildMultipartEntity(HashMap<String, Object> params) {
        mEntityBuilder = MultipartEntityBuilder.create();
        mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentType type = ContentType.create("text/plain", Consts.UTF_8);
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(entry.getValue() instanceof File) {
                addFile((File)entry.getValue(), entry.getKey());
            } else if(entry.getValue() instanceof Bitmap) {
                Bitmap bitmap = (Bitmap)entry.getValue();
                addBitmap(bitmap, entry.getKey());
            } else if(entry.getValue() instanceof List) {
                List list = (List)entry.getValue();
                int i = 0;
                for(Object object : list) {
                    if(object instanceof File) {
//                        FileBody fileBody = new FileBody((File)object);
//                        mEntityBuilder.addPart(entry.getKey() + "[" + String.valueOf(i++) + "]",
//                                               fileBody);
                        addFile((File)object,entry.getKey());
                    } else if(object instanceof Bitmap) {
                        addBitmap((Bitmap)object, entry.getKey());
                    }
                    i++;
                }
            } else {
                mEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), type);
            }
        }
        mEntity = mEntityBuilder.build();
    }

    private void addBitmap(Bitmap bitmap, String key) {
        ContentBody body = new ByteArrayBody(getBitmapBytes(bitmap), key + ".jpg");
        mEntityBuilder.addPart(key, body);
    }

    private void addFile(File file, String key) {
        FileBody fileBody = new FileBody(file);
        mEntityBuilder.addPart(key, fileBody);
    }

    public static byte[] getBitmapBytes(Bitmap bitmap) {
        if(bitmap == null)
            return null;
        int compressSize = 100;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressSize, outputStream);
        bitmap.recycle();
        return outputStream.toByteArray();
    }

    @Override
    public String getBodyContentType() {
        return mEntity.getContentType().getValue();
    }


    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            //mEntity.writeTo(bos);
            mEntity.writeTo(new CountingOutputStream(bos, mEntity.getContentLength(),
                    multipartProgressListener));
            return bos.toByteArray();
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
            return null;
        }

    }

    public static interface MultipartProgressListener {
        void transferred(long transfered, int progress);
    }

    public static class CountingOutputStream extends FilterOutputStream {
        private final MultipartProgressListener progListener;
        private long transferred;
        private long fileLength;

        public CountingOutputStream(final OutputStream out, long fileLength,
                                    final MultipartProgressListener listener) {
            super(out);
            this.fileLength = fileLength;
            this.progListener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            if (progListener != null) {
                this.transferred += len;
                int prog = (int) (transferred * 100 / fileLength);
                this.progListener.transferred(this.transferred, prog);
            }
        }

        public void write(int b) throws IOException {
            out.write(b);
            if (progListener != null) {
                this.transferred++;
                int prog = (int) (transferred * 100 / fileLength);
                this.progListener.transferred(this.transferred, prog);
            }
        }

    }
}
