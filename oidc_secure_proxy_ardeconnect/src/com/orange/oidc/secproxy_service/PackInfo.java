package com.orange.oidc.secproxy_service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

public class PackInfo  {

    public static X509Certificate getCertificate(Context ctx, String packageName) {

            X509Certificate c = null;
            PackageManager pm = ctx.getPackageManager();

            int flags = PackageManager.GET_SIGNATURES;

            PackageInfo packageInfo = null;

            try {
                packageInfo = pm.getPackageInfo(packageName, flags);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                return c;
            }
            Signature[] signatures = packageInfo.signatures;

            byte[] cert = signatures[0].toByteArray();

            InputStream input = new ByteArrayInputStream(cert);

            CertificateFactory cf = null;
            try {
                cf = CertificateFactory.getInstance("X509");
            } catch (CertificateException e) {
                e.printStackTrace();
                return c;
            }
            
            try {
                c = (X509Certificate) cf.generateCertificate(input);
            } catch (CertificateException e) {
                e.printStackTrace();
            }

			return c;
    }
}