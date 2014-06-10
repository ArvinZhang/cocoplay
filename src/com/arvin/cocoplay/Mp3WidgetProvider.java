/**
 * Project Name:cocoplay_20140514_withNum
 * File Name:Mp3WidgetProvider.java
 * Package Name:com.arvin.cocoplay
 * Date:2014年5月15日下午2:32:30
 * Copyright (c) 2014, www.kugou.com All Rights Reserved.
 *
 */

package com.arvin.cocoplay;

import com.arvin.tools.FileUtils;
import com.arvin.tools.Tools;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * ClassName:Mp3WidgetProvider Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2014年5月15日 下午2:32:30
 * 
 * @author arvinzhang
 * @version
 * @see
 */
public class Mp3WidgetProvider extends AppWidgetProvider {
	private String TAG = "Mp3WidgetProvider";
	private static int maxDuration = 0;
    private FileUtils imgUtils;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		String action = intent.getAction();
		RemoteViews views = new RemoteViews(context.getPackageName(),	R.layout.widget_small);

		if (action.equals(Mp3Service.INTENT_ACTION_PLAY)) {
			String str = intent.getStringExtra(Mp3Service.INTENT_ACTION_PLAY);
			Log.i(TAG, "INTENT_ACTION_PLAY onReceive - title:" + str);
			
			views.setImageViewResource(R.id.widget_op_playorpause, R.drawable.btn_simple_pause);
			views.setTextViewText(R.id.widget_songname_text, intent.getStringExtra(Mp3Service.INTENT_ACTION_PLAY));
			
			String imgName = intent.getStringExtra("album_img");

			Bitmap bmp = null;
			imgUtils = FileUtils.getInstance(context);
    		if (!imgName.equals("") && imgUtils.isFileExists(imgName)) {
    			bmp = imgUtils.getBitmap(imgName);
    			Log.i(TAG, "play 从文件中获取" + imgName);
    			views.setImageViewBitmap(R.id.widget_album_img, bmp);
    		} else {
    			views.setImageViewResource(R.id.widget_album_img, R.drawable.playing_bar_default_avatar);
    		}
			
		} else if (action.equals(Mp3Service.INTENT_ACTION_PAUSE)) {
			views.setImageViewResource(R.id.widget_op_playorpause, R.drawable.btn_simple_play);
		} else if (action.equals(Mp3Service.ACTION_UDPATE_PROGRESS)) {
			int currentDuration = intent.getIntExtra("progress", 0);
			int max = intent.getIntExtra("maxDuration", 0);
			
			Log.i(TAG, "currentDuration=" + currentDuration + " max=" + max);
			if (max <= 0) {
				// 如果indeterminate为true的话，相当于调用
				// ProgressBar.setMax, ProgressBar.setProgress和ProgressBar.setIndeterminate，
				// 那么最大值和进度被忽略
				views.setProgressBar(R.id.widget_progressbar, max, currentDuration, true);
			} else {
				views.setProgressBar(R.id.widget_progressbar, max, currentDuration, false);
			}
		}

		// 改变完成后，请一定要记住调用updateAppWidget方法，否则改变不会生效
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		mgr.updateAppWidget(new ComponentName(context, Mp3WidgetProvider.class), views);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// 在这里给widget里的控件添加监听事件
		RemoteViews views = new RemoteViews(context.getPackageName(),	R.layout.widget_small);
		
		// 打开MainActivity
		Intent activityIntent = new Intent(context, MainActivity.class);
		PendingIntent openMainActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
		
		// 播放或暂停
		Intent playOrPauseIntent = new Intent(Mp3Service.INTENT_ACTION_PLAY);
		playOrPauseIntent.setClass(context, Mp3Service.class);
		PendingIntent playOrPausePendingIntent = PendingIntent.getService(context, 0, playOrPauseIntent, 0);

		// 下一首
		Intent nextIntent = new Intent(Mp3Service.INTENT_ACTION_NEXT);
		nextIntent.setClass(context, Mp3Service.class);
		PendingIntent nextPendingIntent = PendingIntent.getService(context, 0, nextIntent, 0);
		
		// 上一首
		Intent previousIntent = new Intent(Mp3Service.INTENT_ACTION_PREVIOUS);
		previousIntent.setClass(context, Mp3Service.class);
		PendingIntent previousPendingIntent = PendingIntent.getService(context, 0, previousIntent, 0);
		
		views.setOnClickPendingIntent(R.id.widget_op_next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_op_playorpause, playOrPausePendingIntent);
		views.setOnClickPendingIntent(R.id.widget_op_previous, previousPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_layout, openMainActivityIntent);
		
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		mgr.updateAppWidget(new ComponentName(context, Mp3WidgetProvider.class), views);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		imgUtils = FileUtils.getInstance(context);
//		Intent intent = new Intent(context, Mp3Service.class);
//		context.startService(intent);
		
	}

	@Override
	public void onDisabled(Context context) {
		// 当widget删除时，停止musicService
		super.onDisabled(context);
//		Intent intent = new Intent(context, Mp3Service.class);
//		context.stopService(intent);
	}
}
