package com.arvin.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.arvin.cocoplay.R;

/**   
 * @Title: MiddleWidgetProvider.java
 * @Package com.arvin.cocoplay
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang   
 * @date 2014年6月19日 下午4:11:22
 * @version V1.0   
 */
public class MiddleWidgetProvider extends BaseWidgetProvider {
	String TAG = this.getClass().getSimpleName();

	@Override
	protected void setRemoteViews(Context context) {
		Log.i(TAG, "setRemoteViews");
		this.views = new RemoteViews(context.getPackageName(), R.layout.widget_middle);
	}

	@Override
	protected void additionOnReceive() {
		// TODO Auto-generated method stub
		
	}
	
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
	protected void additionOnUpdate() {
		views.setOnClickPendingIntent(R.id.widget_middle_op_next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_op_playorpause, playOrPausePendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_op_previous, previousPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_layout, openMainActivityIntent);
	}

	@Override
	protected void initCommonView() {
		albumImgId = R.id.widget_middle_singer_img;
		playOrPauseButtonId = R.id.widget_middle_op_playorpause;
		progressbarId = R.id.widget_middle_progressbar;
		songNameTextId = R.id.widget_middle_song;
	}

	@Override
	protected void setWidgetClass() {
		widgetClass = MiddleWidgetProvider.class;
	}

}
