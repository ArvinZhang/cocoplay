package com.arvin.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.arvin.cocoplay.MainActivity;
import com.arvin.cocoplay.Mp3Service;
import com.arvin.cocoplay.R;
import com.arvin.tools.FileUtils;
import com.arvin.tools.Tools;

/**   
 * @Title: BaseWidgetProvider.java
 * @Package com.arvin.widget
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang   
 * @date 2014年6月19日 下午5:25:35
 * @version V1.0   
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {
    private FileUtils imgUtils;
	
	protected RemoteViews views;
	protected PendingIntent playOrPausePendingIntent;
	protected PendingIntent nextPendingIntent;
	protected PendingIntent previousPendingIntent;
	protected PendingIntent openMainActivityIntent;
	protected Class<?> widgetClass;
	
	protected int albumImgId;
	protected int progressbarId;
	protected int playOrPauseButtonId;
	protected int songNameTextId;
	protected String action;
	protected Intent baseIntent;
	protected static String timeToShow;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		setRemoteViews(context);
		initCommonView();
		action = intent.getAction();
		baseIntent = intent;

		if (action.equals(Mp3Service.INTENT_ACTION_PLAY)) {
			setWhenPlay(context, intent);
		} else if (action.equals(Mp3Service.INTENT_ACTION_PAUSE)) {
			setWhenPause();
		} else if (action.equals(Mp3Service.ACTION_UDPATE_PROGRESS)) {
			setWhenUpdateProgress(intent);
		} else if (action.equals(Mp3Service.INTENT_ACTION_WIDGET_REFREASH)) {
			if (intent.getBooleanExtra("isPlaying", true)) {
				setWhenPlay(context, intent);
			} else {
				setWhenPause();
			}
			setWhenUpdateProgress(intent);
		}
		
		additionOnReceive();

		// 改变完成后，请一定要记住调用updateAppWidget方法，否则改变不会生效
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		setWidgetClass();
		mgr.updateAppWidget(new ComponentName(context, widgetClass), views);
	}
	
	public void setWhenPlay(Context context, Intent intent) {
		
		views.setImageViewResource(playOrPauseButtonId, R.drawable.btn_simple_pause);
		views.setTextViewText(songNameTextId, intent.getStringExtra("song_title"));
		
		String imgName = intent.getStringExtra("album_img");

		Bitmap bmp = null;
		imgUtils = FileUtils.getInstance(context);
		if (!imgName.equals("") && imgUtils.isFileExists(imgName)) {
			bmp = imgUtils.getBitmap(imgName);
			views.setImageViewBitmap(albumImgId, bmp);
		} else {
			views.setImageViewResource(albumImgId, R.drawable.playing_bar_default_avatar);
		}
	}
	
	public void setWhenPause() {
		views.setImageViewResource(playOrPauseButtonId, R.drawable.btn_simple_play);
	}
	
	public void setWhenUpdateProgress(Intent intent) {
		int currentDuration = intent.getIntExtra("progress", 0);
		int max = intent.getIntExtra("maxDuration", 0);
		
		Tools tools = new Tools();
		StringBuilder timeStrBuilder = new StringBuilder();
		timeStrBuilder.append(tools.durationFormat(currentDuration));
		timeStrBuilder.append("/");
		timeStrBuilder.append(tools.durationFormat(max));
		timeToShow = timeStrBuilder.toString();
		
		if (max <= 0) {
			// 如果indeterminate为true的话，相当于调用
			// ProgressBar.setMax, ProgressBar.setProgress和ProgressBar.setIndeterminate，
			// 那么最大值和进度被忽略
			views.setProgressBar(progressbarId, max, currentDuration, true);
		} else {
			views.setProgressBar(progressbarId, max, currentDuration, false);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		setRemoteViews(context);
		
		// 打开MainActivity
		Intent activityIntent = new Intent(context, MainActivity.class);
		openMainActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
		
		// 播放或暂停
		Intent playOrPauseIntent = new Intent(Mp3Service.INTENT_ACTION_PLAY);
		playOrPauseIntent.setClass(context, Mp3Service.class);
		playOrPausePendingIntent = PendingIntent.getService(context, 0, playOrPauseIntent, 0);

		// 下一首
		Intent nextIntent = new Intent(Mp3Service.INTENT_ACTION_NEXT);
		nextIntent.setClass(context, Mp3Service.class);
		nextPendingIntent = PendingIntent.getService(context, 0, nextIntent, 0);
		
		// 上一首
		Intent previousIntent = new Intent(Mp3Service.INTENT_ACTION_PREVIOUS);
		previousIntent.setClass(context, Mp3Service.class);
		previousPendingIntent = PendingIntent.getService(context, 0, previousIntent, 0);
		
		additionOnUpdate(context);
		
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		setWidgetClass();
		mgr.updateAppWidget(new ComponentName(context, widgetClass), views);
	}

	protected abstract void setRemoteViews(Context context);
	protected abstract void initCommonView();
	protected abstract void additionOnReceive();
	protected abstract void additionOnUpdate(Context context);
	protected abstract void setWidgetClass();
	
	protected void updateModeImg(int modeViewId, int mode) {
		switch (mode) {
		case Mp3Service.MODE_LIST_LOOP:
			views.setImageViewResource(modeViewId, R.drawable.ic_player_mode_all);
			break;
		case Mp3Service.MODE_RANDOM:
			views.setImageViewResource(modeViewId, R.drawable.ic_player_mode_random);
			break;
		case Mp3Service.MODE_SEQUENCE:
			views.setImageViewResource(modeViewId, R.drawable.ic_player_mode_sequence);
			break;
		case Mp3Service.MODE_SINGLE_LOOP:
			views.setImageViewResource(modeViewId, R.drawable.ic_player_mode_single);
			break;
		default:
			break;
		}
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		imgUtils = FileUtils.getInstance(context);
		Intent intent = new Intent(Mp3Service.INTENT_ACTION_INITIAL_WIDGET);
		intent.setClass(context, Mp3Service.class);
		context.startService(intent);
	}

	@Override
	public void onDisabled(Context context) {
		// 当widget删除时，停止musicService
		super.onDisabled(context);
//		Intent intent = new Intent(context, Mp3Service.class);
//		context.stopService(intent);
	}
}
