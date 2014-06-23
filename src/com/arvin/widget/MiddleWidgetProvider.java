package com.arvin.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.arvin.cocoplay.Mp3Service;
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
		this.views = new RemoteViews(context.getPackageName(), R.layout.widget_middle);
	}

	@Override
	protected void additionOnReceive() {
		if (action.equals(Mp3Service.INTENT_ACTION_MODE)) {
			int currentMode = baseIntent.getIntExtra("currentPlayMode", 2);
			updateModeImg(R.id.widget_middle_mode, currentMode);
		}
		
		String singer = baseIntent.getStringExtra("singer");
		String album = baseIntent.getStringExtra("album");
		
		if (singer != null && !singer.trim().equals("")) {
			views.setTextViewText(R.id.widget_middle_singer, baseIntent.getStringExtra("singer"));
		}
		if (album != null && !album.trim().equals("")) {
			views.setTextViewText(R.id.widget_middle_album, baseIntent.getStringExtra("album"));
		}
	}

	@Override
	protected void additionOnUpdate(Context context) {
		views.setOnClickPendingIntent(R.id.widget_middle_op_next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_op_playorpause, playOrPausePendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_op_previous, previousPendingIntent);
		views.setOnClickPendingIntent(R.id.widget_middle_layout, openMainActivityIntent);
		
		Intent intent = new Intent(Mp3Service.INTENT_ACTION_CHANGE_SERVICE_MODE);
		intent.setClass(context, Mp3Service.class);
		PendingIntent modeSwitchIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.widget_middle_mode, modeSwitchIntent);
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
