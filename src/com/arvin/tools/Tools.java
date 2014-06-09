/**
 * Project Name:cocoplay_20140509
 * File Name:Tools.java
 * Package Name:com.arvin.cocoplay
 * Date:2014年5月9日下午7:26:39
 * Copyright (c) 2014, www.kugou.com All Rights Reserved.
 *
*/

package com.arvin.tools;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * ClassName:Tools
 * Function: TODO ADD FUNCTION.
 * Reason:	 TODO ADD REASON.
 * Date:     2014年5月9日 下午7:26:39
 * @author   arvinzhang
 * @version  
 * @see 	 
 */
public class Tools {
	/**
	 * 将传入的文件时间长度格式化为符合人们习惯的形式
	 * 
	 * @param duration
	 * @return
	 */
	public String durationFormat(long duration) {
		String format = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		String secStr = "";
		String minStr = "";
		String hStr = "";

		second = (int) (duration / 1000 % 60);
		minute = (int) (duration / 1000 / 60 % 60);
		hour = (int) (duration / 1000 / 60 / 60);
		if (second / 10 == 0) {
			secStr = "0" + second;
		} else {
			secStr = String.valueOf(second);
		}

		if (minute / 10 == 0) {
			minStr = "0" + minute;
		} else {
			minStr = String.valueOf(second);
		}

		if (hour == 0) {
			format = minStr + ":" + secStr;
		} else if (hour / 10 == 0) {
			hStr = "0" + hour;
			format = hStr + ":" + minStr + ":" + secStr;
		} else {
			format = hour + ":" + minStr + ":" + secStr;
		}
		return format;
	}

	//加载图片  
    public Bitmap getURLimage(String url) {  
        Bitmap bmp = null;  
        try {  
            URL myurl = new URL(url);  
            // 获得连接  
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();  
            conn.setConnectTimeout(6000);//设置超时  
            conn.setDoInput(true);  
            conn.setUseCaches(false);//不缓存  
            conn.connect();  
            InputStream is = conn.getInputStream();//获得图片的数据流  
            bmp = BitmapFactory.decodeStream(is);  
            is.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return bmp;  
    }  
}
