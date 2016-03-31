package com.xiaogu.xgvolleyex;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;


/**
 * Created by Phyllis on 16/3/30.
 */
public class SSLSocketFactoryCreator {
    public static SSLSocketFactory getSSLSocketFactory(Context context,int certificateId) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            TrustManagerFactory managerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            if(certificateId == 0){

                sslContext.init(null,new TrustManager[]{new SSLSocketFactoryCreator.TrustAnyTrustManager()},null);
            }else {
                Certificate certificate = certificateFactory
                        .generateCertificate(context.getResources().openRawResource(certificateId));


                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);
                managerFactory.init(keyStore);
                sslContext.init(null, managerFactory.getTrustManagers(), null);
            }
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                       String authType) throws java.security.cert.CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                       String authType) throws java.security.cert.CertificateException {

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }


    }

}
