package com.arvin.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.arvin.cocoplay.MainActivity;
import com.arvin.cocoplay.Mp3Service;
import com.arvin.cocoplay.R;
import com.arvin.tools.FileUtils;

/**   
 * @Title: BaseWidgetProvider.java
 * @Package com.arvin.widget
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang   
 * @date 2014年6月19日 下午5:25:35
 * @version V1.0   
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {
	private String TAG = "Mp3WidgetProvider";
    private FileUtils imgUtils;
	
	protected RemoteViews views;
	protected PendingIntent playOrPausePendingIntent;
	protected PendingIntent nextPendingIntent;
	protected PendingIntent previousPendingIntent;
	protected PendingIntent openMainActivityIntent;
	protected Class widgetClass;
	
	protected int albumImgId;
	protected int progressbarId;
	protected int playOrPauseButtonId;
	protected int songNameTextId;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		setRemoteViews(context);
		initCommonView();
		String action = intent.getAction();

		if (action.equals(Mp3Service.INTENT_ACTION_PLAY)) {
			String str = intent.getStringExtra(Mp3Service.INTENT_ACTION_PLAY);
			Log.i(TAG, "INTENT_ACTION_PLAY onReceive - title:" + str);
			
			views.setImageViewResource(playOrPauseButtonId, R.drawable.btn_simple_pause);
			views.setTextViewText(songNameTextId, intent.getStringExtra(Mp3Service.INTENT_ACTION_PLAY));
			
			String imgName = intent.getStringExtra("album_img");

			Bitmap bmp = null;
			imgUtils = FileUtils.getInstance(context);
    		if (!imgName.equals("") && imgUtils.isFileExists(imgName)) {
    			bmp = imgUtils.getBitmap(imgName);
    			Log.i(TAG, "play 从文件中获取" + imgName);
    			views.setImageViewBitmap(albumImgId, bmp);
    		} else {
    			views.setImageViewResource(albumImgId, R.drawable.playing_bar_default_avatar);
    		}
			
		} else if (action.equals(Mp3Service.INTENT_ACTION_PAUSE)) {
			views.setImageViewResource(playOrPauseButtonId, R.drawable.btn_simple_play);
		} else if (action.equals(Mp3Service.ACTION_UDPATE_PROGRESS)) {
			int currentDuration = intent.getIntExtra("progress", 0);
			int max = intent.getIntExtra("maxDuration", 0);
			
			Log.i(TAG, "currentDuration=" + currentDuration + " max=" + max);
			if (max <= 0) {
				// 如果indeterminate为true的话，相当于调用
				// ProgressBar.setMax, ProgressBar.setProgress和ProgressBar.setIndeterminate，
				// 那么最大值和进度被忽略
				views.setProgressBar(progressbarId, max, currentDuration, true);
			} else {
				views.setProgressBar(progressbarId, max, currentDuration, false);
			}
		}
		
		additionOnReceive();

		// 改变完成后，请一定要记住调用updateAppWidget方法，否则改变不会生效
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		setWidgetClass();
		mgr.updateAppWidget(new ComponentName(context, widgetClass), views);
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
		
		additionOnUpdate();
		
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		setWidgetClass();
		mgr.updateAppWidget(new ComponentName(context, widgetClass), views);
	}

	protected abstract void setRemoteViews(Context context);
	protected abstract void initCommonView();
	protected abstract void additionOnReceive();
	protected abstract void additionOnUpdate();
	protected abstract void setWidgetClass();

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
