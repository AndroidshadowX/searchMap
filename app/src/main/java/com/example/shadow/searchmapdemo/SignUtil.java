package com.example.shadow.searchmapdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;


/**
 * Created by dddd on 2016/1/5.
 */
public class SignUtil {
    public static String getMD5AppSign(Context mContext) {
        return getMessageDigest("MD5", getSingInfo(mContext));


    }

    public static String getAppSign(Context mContext) {
        return "MD5:" + getMessageDigest("MD5", getSingInfo(mContext)) + "\r\n" + "SHA1:" + getMessageDigest("SHA1", getSingInfo(mContext));


    }


    public static byte[] getSingInfo(Context mContext) {
        try {

            Context context = mContext;
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return sign.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }


    private static String getMessageDigest(String instance, byte[] signature) {
        String sinfo = null;
        try {
            MessageDigest md = MessageDigest.getInstance(instance);

            md.update(signature);

            byte[] digest = md.digest();

            sinfo = toHexString(digest).replace(":", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sinfo == null ? "" : sinfo;
    }

    /**
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {

        StringBuffer buf = new StringBuffer();


        int len = block.length;


        for (int i = 0; i < len; i++) {

            byte2hex(block[i], buf);

            if (i < len - 1) {

                buf.append(":");

            }

        }

        return buf.toString();

    }

    private static void byte2hex(byte b, StringBuffer buf) {

        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8',

                '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        int high = ((b & 0xf0) >> 4);

        int low = (b & 0x0f);

        buf.append(hexChars[high]);

        buf.append(hexChars[low]);

    }
}
