/**
 * Project Name:cocoplay_20140514_withNum
 * File Name:Mp3WidgetProvider.java
 * Package Name:com.arvin.cocoplay
 * Date:2014年5月15日下午2:32:30
 * Copyright (c) 2014, www.kugou.com All Rights Reserved.
 *
 */

package com.arvin.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.arvin.cocoplay.R;

/**
 * ClassName:Mp3WidgetProvider Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2014年5月15日 下午2:32:30
 * 
 * @author arvinzhang
 * @version
 * @see
 */
public class SmallWidgetProvider extends BaseWidgetProvider {
	String TAG = this.getClass().getSimpleName();

	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		super.onReceive(context, intent);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(TAG, "onUpdate");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	protected void setRemoteViews(Context context) {
		Log.i(TAG, "setRemoteViews");
		this.views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
	}

	@Override
	protected void additionOnReceive() {
	}

	@Override
	protected void additionOnUpdate(Context context) {
		views.setOnClickPendingIntent(R.id.widget_op_next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_op_playorpause, playOrPausePendingIntent);
		views.setOnClickPendingIntent(R.id.widget_op_previous, previousPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_layout, openMainActivityIntent);
	}

	@Override
	protected void initCommonView() {
		albumImgId = R.id.widget_album_img;
		playOrPauseButtonId = R.id.widget_op_playorpause;
		progressbarId = R.id.widget_progressbar;
		songNameTextId = R.id.widget_songname_text;
	}

	@Override
	protected void setWidgetClass() {
		widgetClass = SmallWidgetProvider.class;	
	}
	
}
