package com.arvin.widget;

import com.arvin.cocoplay.Mp3Service;
import com.arvin.cocoplay.R;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**   
 * @Title: BigWidgetProvider.java
 * @Package com.arvin.widget
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang   
 * @date 2014年6月20日 上午10:39:02
 * @version V1.0   
 */
public class BigWidgetProvider extends BaseWidgetProvider {

	@Override
	protected void setRemoteViews(Context context) {
		views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
	}

	@Override
	protected void initCommonView() {
		albumImgId = R.id.widget_big_singer_img;
		playOrPauseButtonId = R.id.widget_big_op_playorpause;
		progressbarId = R.id.widget_big_progressbar;
		songNameTextId = R.id.widget_big_song;
	}

	@Override
	protected void additionOnReceive() {
		if (action.equals(Mp3Service.INTENT_ACTION_MODE)) {
			int currentMode = baseIntent.getIntExtra("currentPlayMode", 2);
			updateModeImg(R.id.widget_big_mode, currentMode);
		}
		views.setTextViewText(R.id.widget_big_time, timeToShow);
	}

	@Override
	protected void additionOnUpdate(Context context) {
		views.setOnClickPendingIntent(R.id.widget_big_op_next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_big_op_playorpause, playOrPausePendingIntent);
		views.setOnClickPendingIntent(R.id.widget_big_op_previous, previousPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_big_layout, openMainActivityIntent);
		
		Intent intent = new Intent(Mp3Service.INTENT_ACTION_CHANGE_SERVICE_MODE);
		intent.setClass(context, Mp3Service.class);
		PendingIntent modeSwitchIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.widget_big_mode, modeSwitchIntent);
	}

	@Override
	protected void setWidgetClass() {
		widgetClass = BigWidgetProvider.class;
	}

}
