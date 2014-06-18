package com.arvin.cocoplay;

import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.arvin.pojo.Mp3;
import com.arvin.tools.FileUtils;
import com.arvin.tools.Tools;

/**
 * OnPreparedListener 通过 prepareAsync()异步的招行prepare()操作
 * 避免UI阻塞
 * @author arvinzhang
 *
 */
public class Mp3Service extends Service{
	public Mp3Service() {}
	
	private String TAG = "Mp3Service"; 
	private MediaPlayer mediaPlayer;

	// Service 需要向外界发送广播的变量
	private int maxDuration = 0;
	private int currentProgress = 0;
	private static int currentMp3Position = -1;
	private int currentPlayMode = 2; // 默认顺序播放
	private boolean isPlaying = false;
	
	private List<Mp3> mp3List;
	
	public final static int MODE_SINGLE_LOOP = 0; // 单曲循环
	public final static int MODE_LIST_LOOP = 1; // 列表循环
	public final static int MODE_SEQUENCE = 2; // 顺序播放
	public final static int MODE_RANDOM = 3; // 随机播放
	
	
	private final int HANDLER_UPDATE_CURRENT_MP3 = 4;
	private final int HANDLER_UPDATE_PROGRESS = 5;
	private final int HANDLER_REFRESH_NOTIFICATION = 6;
	private final int HANDLER_LOAD_IMAGE = 7;
	
	public static final String ACTION_UPDATE_CURRENT_MP3 = "com.arvin.cocoplay.ACTION_UPDATE_CURRENT_MP3";
	public static final String ACTION_UDPATE_PROGRESS = "com.arvin.cocoplay.ACTION_UPDATE_PROGRESS";
	
	public static final String INTENT_ACTION_PLAY = "com.arvin.cocoplay.INTENT_ACTION_PLAY";
	public static final String INTENT_ACTION_NEXT = "com.arvin.cocoplay.INTENT_ACTION_NEXT";
	public static final String INTENT_ACTION_PREVIOUS = "com.arvin.cocoplay.INTENT_ACTION_PREVIOUS";
	public static final String INTENT_ACTION_PAUSE = "com.arvin.cocoplay.INTENT_ACTION_PAUSE";
	public static final String INTENT_ACTION_MODE = "com.arvin.cocoplay.INTENT_ACTION_MODE";
	public static final String INTENT_ACTION_LOAD_IMAGE = "com.arvin.cocoplay.INTENT_ACTION_LOAD_IMAGE";

    private Notification notification;
    private NotificationManager notificationManager;
    private RemoteViews midContentView;
    private RemoteViews contentView;
    
    private int sysVersion = Integer.parseInt(VERSION.SDK);  
    
    private FileUtils imgUtils;
    
	private Binder mp3SerBinder;
	
	@Override
	public IBinder onBind(Intent arg0) {
		mp3SerBinder = new Mp3SerBinder();

		Log.i(TAG, "mp3SerBinder " + mp3SerBinder.toString());
		return mp3SerBinder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		mp3List = Mp3Loader.getInstance(getContentResolver()).getMp3List();
		imgUtils = FileUtils.getInstance(Mp3Service.this);
		
		initMediaPlayer();
		setNotification();
		
		super.onCreate();
	}
	
	private void initMediaPlayer() {
		Log.i(TAG, "initMediaPlayer");
		
		mediaPlayer = new MediaPlayer();
		// 设置播放的音频流类型
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// 当mediaPlayer加载完成后开始播放
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(TAG, "initMediaPlayer-onPreared");
				mediaPlayer.start();
				//mediaPlayer.seekTo(currentProgress);
				updateProgress();
				Log.v(TAG, "[OnPreparedListener] Start at " + currentMp3Position + " in mode " + currentPlayMode + ", currentDuration : " + currentProgress);
			}
		});

		// 当一首歌曲播放完后，根据当前的播放模式自动切换下一首歌
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (isPlaying) {
					switch (currentPlayMode) {
						case MODE_SINGLE_LOOP:
							mediaPlayer.start();
							break;
						case MODE_LIST_LOOP:
							play((currentMp3Position + 1) % mp3List.size(), 0);
							break;
						case MODE_SEQUENCE:
							if ((currentMp3Position + 1) < mp3List.size()-1) {
								playNext();
							}
							break;
						case MODE_RANDOM:
							play(getRandomPosition(), 0);
							break;
						default:
							break;
					}
				}
			}
		});
	}
	
	/**
	 * 初始化操作
	 *    notification.contentView 和 notification.bigContentView的不同之处：
	 *    notification.contentView是单行的通知
	 *    notification.bigContentView可以显示更多内容的通知
	 *    
	 *    在4.1系统的时候
	 *    在当前的notification不是在通知栏顶部的时候是显示notification.contentView布局
	 *    在当前的notification是在通知栏的顶部的时候显示的是notification.bigContentView布局
	 *    
	 *    在4.0系统的时候
	 *    关于notification的bigContentView是在Jelly Bean 4.1之中加入的,在4.1之前的话bigContentView属性是无效的。
	 *    
	 *    只有在在sdk3.0以上的系统中，通知栏中的按钮点击事件才能响应，以下的做不到。
	 *    要加入按钮点击事件监听的话,必须要做手机的系统判断。再使用RemoteViews的setOnClickPendingIntent的方法
	 *    
	 *    在3.0之前推荐的通知栏写法是
	 *    notification = new Notification(R.drawable.bg_albumdetail_default, "start listener Music", System.currentTimeMillis());
	 *    notification.setLatestEventInfo(this.mActivity, "this is test", "ok!", contentPendingIntent);
	 *    此时notification的logo是实例化Notification中的 "R.drawable.bg_albumdetail_default"
	 *    实际内容是通过setLatestEventInfo(this.mActivity, "this is test", "ok!", contentPendingIntent);
	 */
	private void setNotification(){
		Log.d(TAG, String.valueOf(sysVersion));
       
		// 设置基本的notification
		initNotification();
       
        // if(sysVersion>=15){
       	setContentView();
       	// setBigContentView();
       	// }
       	notifyMusicBox();
	}
   
	/**
	 * 初始化Notification
	 * 此处做Notification的基本设置
	 */
   private void initNotification(){
       // 整个notification的点击事件，点击进入MainActivity
       Intent contentIntent = new Intent(this, MainActivity.class);
       // 以该Intent启动一个Activity，一定要设置 Intent.FLAG_ACTIVITY_NEW_TASK 标记
       contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
       PendingIntent contentPendingIntent = PendingIntent.getActivity(this, R.string.app_name, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
       
       // 获取notification管理的实例
       notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
       
       // 设置notification
       notification = new Notification(R.drawable.alam_playback_icon, getString(R.string.app_name), System.currentTimeMillis());
       if (currentMp3Position >= 0 && mp3List.size() > 0) {
	       notification.setLatestEventInfo(this, 
	       		mp3List.get(currentMp3Position).getTitle(), 
	       		mp3List.get(currentMp3Position).getArtist(),
	       		contentPendingIntent);
       }
       
       // 设置notification在通知栏常驻
       notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
       notification.contentIntent = contentPendingIntent;
   }
   
   /**
    * 
    * @Title: setNotificationBtn
    * @Description: 设置notification根据当前是否正在播放来显示播放或暂停按键
    * @param     设定文件
    * @return void    返回类型
    * @throws
    */
   private void setNotificationBtn() {
   	if (isPlaying) {
   		midContentView.setImageViewResource(R.id.notification_playorpause_img, R.drawable.statusbar_btn_pause);
   	} else {
   		midContentView.setImageViewResource(R.id.notification_playorpause_img, R.drawable.statusbar_btn_play);
   	}
   }
   
   /**
    * 设置contentView
    * 注：只有在在sdk3.0以上的系统中，通知栏中的按钮点击事件才能响应，以下的做不到。
    */
//   private void setSimpleContetView(){
//       //布局文件(小)
//       contentView = new RemoteViews(this.getPackageName(), R.layout.notification_view);
//       contentView.setTextViewText(R.id.tv_music, "Hello World!");
//       notification.contentView = contentView;
//   }
   
   /**
    * 设置BigContentView
    * 在Jelly Bean 中加入
    * 在此可以加入按钮的事件监听
    */
   private void setContentView(){
       //布局文件(大)
       midContentView = new RemoteViews(this.getPackageName(), R.layout.notification);
       final String url = "http://i1217.photobucket.com/albums/dd382/winningprizes/a0f5c39c4b6d4f5b792002a8451898a1.jpg"; 
       if (currentMp3Position >= 0 && mp3List.size() > 0) {
    	   Log.i(TAG, "setContentView - getTitle:" + mp3List.get(currentMp3Position).getTitle() 
    			   + " of song:" + mp3List.get(currentMp3Position).getTitle() 
    			   + " album_id:" + mp3List.get(currentMp3Position).getAlbum_id());
    	   
    	   if (mp3List.get(currentMp3Position).getTitle().toUpperCase().contains("ADELE")) {
    		   StringBuffer imgName = new StringBuffer();
    		   imgName.append(mp3List.get(currentMp3Position).getTitle());
    		   if (imgUtils.isFileExists(imgName.toString())) {
    			   midContentView.setImageViewBitmap(R.id.notification_img, imgUtils.getBitmap(imgName.toString()));
    			   Log.i(TAG, "setContentView 从文件中获取" + imgName);
    		   } else {
    			   Log.i(TAG, "setContentView 开始下载图片" + imgName + "网络路径：" + url);
	    		   new Thread(new Runnable() {  
	                   @Override  
	                   public void run() {  
	                       // TODO Auto-generated method stub  
	                       Bitmap bmp = new Tools().getURLimage(url);  
	                       Message msg = new Message();  
	                       Log.i(TAG, "setContentView - loading image");
	                       msg.what = HANDLER_LOAD_IMAGE;  
	                       msg.obj = bmp;  
	                       handler.sendMessage(msg);  
	                   }  
	               }).start();
    		   }
    	   } else {
    		   midContentView.setImageViewResource(R.id.notification_img, R.drawable.playing_bar_default_avatar);
    	   }
    	   
       	midContentView.setTextViewText(R.id.notification_title_text, mp3List.get(currentMp3Position).getTitle());
       	midContentView.setTextViewText(R.id.notification_singer_text, mp3List.get(currentMp3Position).getArtist());
       }
       
       
//       Intent lastIntent = new Intent(this, MainActivity.class);
//       PendingIntent lastPendingIntent = PendingIntent.getBroadcast(this, 0, lastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//       midContentView.setOnClickPendingIntent(R.id.ib_last, lastPendingIntent);
       
       //播放或暂停
       Intent playOrPauseIntent = new Intent(this, Mp3Service.class);
       playOrPauseIntent.setAction(Mp3Service.INTENT_ACTION_PLAY);
       PendingIntent playOrPausePendingIntent = PendingIntent.getService(this , 1, playOrPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
       midContentView.setOnClickPendingIntent(R.id.notification_playorpause_img, playOrPausePendingIntent);
       
       //下一页
       Intent nextIntent = new Intent(this, Mp3Service.class);
       nextIntent.setAction(Mp3Service.INTENT_ACTION_NEXT);
       PendingIntent nextPendingIntent = PendingIntent.getService(this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
       midContentView.setOnClickPendingIntent(R.id.notification_next_img, nextPendingIntent);
       
       notification.contentView = midContentView;
   }
   
   /**
    * 显示notificationMusicBox
    */
   public void notifyMusicBox() {
       notificationManager.notify(R.string.app_name, notification);
   }
	
	
//	START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
//  随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。
//  如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
//	START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
//	START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
//	START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && mp3List.size() > 0) {
			if (intent.getAction().equals(INTENT_ACTION_PLAY)) {
				if (currentMp3Position < 0) {
					currentMp3Position = 0;
					play(currentMp3Position, 0);
				} else {
					if (!mediaPlayer.isPlaying()) {
						play(currentMp3Position, mediaPlayer.getCurrentPosition());
					} else {
						pause();
					}
				}
			} else if (intent.getAction().equals(INTENT_ACTION_NEXT)) {
				playNext();
			} else if (intent.getAction().equals(INTENT_ACTION_PREVIOUS)) {
				playPrevious();
			}
		}
		
		return START_STICKY;
	}

	/**
	 * 
	 * @Title: cancelNotification
	 * @Description:  取消通知，使用notification不再显示在通知栏
	 * @param     设定文件
	 * @return void    返回类型
	 * @throws
	 */
	private void cancelNotification() {
	    NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    notificationManager.cancel(R.string.app_name);
	}
	
	/**
	 * 
	 * @ClassName: Mp3SerBinder
	 * @Description: 在Activity中绑定此service，通过其中的方法可以获取service的状态值
	 * @author arvinzhang   
	 * @date 2014年5月21日 下午8:08:05
	 *
	 */
	public class Mp3SerBinder extends Binder {
		
		public Mp3SerBinder() {
			Log.i(TAG, "binderService");
		}
		
		public void bindPlay(int currentMp3, int mCurrentDuration) {
			Log.i(TAG, "bindPlay() - currentMp3=" + currentMp3);
			
			if (mp3List.size() > 0 ) {
				play(currentMp3, mCurrentDuration);
			}
		}
		
		public void bindPause() {
			pause();
		}
		
		public void bindSeekPause() {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
		}
		
		public void bindStart() {
			if (mediaPlayer != null) {
				mediaPlayer.start();
			}
		}

		public void bindPlayNext() {
			if (mp3List.size() > 0) {
				playNext();
			}
		}
		
		public void bindPlayPrevious() {
			if (mp3List.size() > 0) {
				playPrevious();
			}
		}
		
		public void bindUpdateMode() {
			updateMode();
		}
		
		public void bindPlayRandom() {
			currentPlayMode = MODE_RANDOM;
			int position = getRandomPosition();
			if (position >= 0) {
				play(position, 0);
			}
		}
		
		public int bindGetCurrentMp3Position() {
			return currentMp3Position;
		}
		
		public int bindGetCurrentProgress() {
			return (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
		}
		
		public boolean bindIsPlaying() {
			return isPlaying;
		}
		
		public int bindGetMaxDuration() {
			return maxDuration;
		}
		
		public int bindGetCurrentPlayMode() {
			return currentPlayMode;
		}
		
		public void bindUpdateProgress(int progress) {
			if (mediaPlayer != null) {
				currentProgress = progress;
				if (isPlaying) {
					mediaPlayer.seekTo(currentProgress);
				} else {
					play(currentMp3Position, currentProgress);
				}
			}
		}

		public void bindRefreshMp3List() {
			mp3List = Mp3Loader.getInstance(getContentResolver()).getMp3List();
		}
		
		public int bindGetAudioSessionId() {
			return mediaPlayer != null ? mediaPlayer.getAudioSessionId() : 0;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLER_UPDATE_CURRENT_MP3:
					updateCurrentMp3();
					break;
				case HANDLER_UPDATE_PROGRESS:
					updateProgress();
					break;
				case HANDLER_REFRESH_NOTIFICATION:
					refreshNotification();
					break;
				case HANDLER_LOAD_IMAGE:
					Bitmap bmp = (Bitmap) msg.obj;
                    Log.i(TAG, "handler - set image");
                    StringBuffer imgName = new StringBuffer();
                    imgName.append(mp3List.get(currentMp3Position).getTitle());
                    try {
						imgUtils.savaBitmap(imgName.toString(), bmp);
						Log.i(TAG, "handler 保存图片" + imgName + "成功");
					} catch (IOException e) {
						Log.i(TAG, "handler 保存图片" + imgName + "失败");
						e.printStackTrace();
					}
		        	midContentView.setImageViewBitmap(R.id.notification_img, bmp);
		        	notifyMusicBox();
		        	
		        	Intent intent = new Intent();
		    		intent.setAction(INTENT_ACTION_LOAD_IMAGE);
		    		sendBroadcast(intent);
					break;
				default:
					break;
			}
		}
	};
	
	private void updateCurrentMp3() {
		if (mediaPlayer != null) {
			maxDuration = (int) mp3List.get(currentMp3Position).getDuration();
		}
		
		Log.i(TAG, "updateCurrentMp3() - maxDuration=" + maxDuration + " title=" + mp3List.get(currentMp3Position).getTitle());
		Intent intent = new Intent();
		intent.setAction(ACTION_UPDATE_CURRENT_MP3);
		intent.putExtra("currentMp3Position", currentMp3Position);
		intent.putExtra("title", mp3List.get(currentMp3Position).getTitle());
		intent.putExtra("maxDuration", maxDuration);
		sendBroadcast(intent);
	}

	private void updateProgress() {
		if (mediaPlayer != null && isPlaying) {
			int progress = mediaPlayer.getCurrentPosition();
			Intent intent = new Intent();
			intent.setAction(ACTION_UDPATE_PROGRESS);
			intent.putExtra("progress", progress);
			intent.putExtra("maxDuration", maxDuration);
			sendBroadcast(intent);
			handler.sendEmptyMessageDelayed(HANDLER_UPDATE_PROGRESS, 1000);	
			//Log.i(TAG, "updateProgress() - progress" + progress);
		}
	}
	
	private void updateMode() {
		currentPlayMode = (currentPlayMode+1) % 4;
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION_MODE);
		intent.putExtra("currentPlayMode", currentPlayMode);
		sendBroadcast(intent);
	}
	
	private void refreshNotification() {
		setContentView();
		setNotificationBtn();
		notifyMusicBox();
	}

	private void play(int currentMp3, int mCurrentDuration) {
		Log.i(TAG, "play() - [paly current=" + currentMp3 + " mCurrentDuration=" + mCurrentDuration + "]");
		isPlaying = true;
		StringBuffer imgName = new StringBuffer();
		if (currentMp3 != currentMp3Position) {
			currentProgress = mCurrentDuration;
			currentMp3Position = currentMp3;
			handler.sendEmptyMessage(HANDLER_UPDATE_CURRENT_MP3);
			
			mediaPlayer.reset();
			try {
				mediaPlayer.setDataSource(mp3List.get(currentMp3).getUrl());
			} catch (Exception e) {
				e.printStackTrace();
			}
			mediaPlayer.prepareAsync();
			
	        if (currentMp3Position >= 0 && mp3List.size() > 0) {
	        	if (mp3List.get(currentMp3Position).getTitle().toUpperCase().contains("ADELE")) {
	        		imgName.append(mp3List.get(currentMp3Position).getTitle());
	        	}
	        }
		} else {
			mediaPlayer.seekTo(mCurrentDuration);
			mediaPlayer.start();
			updateProgress();
		}
		
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION_PLAY);
		intent.putExtra("album_img", imgName.toString());
		intent.putExtra(INTENT_ACTION_PLAY, mp3List.get(currentMp3Position).getTitle());
		sendBroadcast(intent);
		handler.sendEmptyMessage(HANDLER_REFRESH_NOTIFICATION);
	}
	
	private void playNext() {
		switch (currentPlayMode) {
			case MODE_SINGLE_LOOP:
			case MODE_LIST_LOOP:
				if (currentMp3Position+1 == mp3List.size()) {
					play(0, 0);
				} else {
					play(currentMp3Position+1, 0);
				}
				break;
			case MODE_SEQUENCE:
				if (currentMp3Position+1 < mp3List.size()) {
					play(currentMp3Position+1, 0);
				} else {
					Toast.makeText(getApplicationContext(), "已经是最后1首歌了，再后面就没有了哦", Toast.LENGTH_LONG).show();
				}
				break;
			case MODE_RANDOM:
				play(getRandomPosition(), 0);
				break;
			default:
				break;
		}
	}
	
	private void playPrevious() {
		switch (currentPlayMode) {
			case MODE_SINGLE_LOOP:
			case MODE_LIST_LOOP:
				if (currentMp3Position-1 < 0) {
					play(mp3List.size()-1, 0);
				} else {
					play(currentMp3Position-1, 0);
				}
				break;
			case MODE_SEQUENCE:
				if (currentMp3Position-1 >= 0) {
					play(currentMp3Position-1, 0);
				} else if (currentMp3Position == 0) {
					Toast.makeText(getApplicationContext(), "已经是第1首歌，再前面就没有了哦", Toast.LENGTH_LONG).show();
				}
				break;
			case MODE_RANDOM:
				play(getRandomPosition(), 0);
				break;
			default:
				break;
		}
	}

	private void pause() {
		if (mediaPlayer != null) {
			if (isPlaying) {
				mediaPlayer.pause();
				isPlaying = false;
			} else {
				mediaPlayer.start();
				isPlaying = true;
			}
		}
		
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION_PAUSE);
		sendBroadcast(intent);
		handler.sendEmptyMessage(HANDLER_REFRESH_NOTIFICATION);
	}
	

	Handler mHandler = new Handler();
	Runnable mUpdateResults = new Runnable() {
		public void run() {
			MainActivity.detail_lyric_view.invalidate(); // 更新视图
		}
	};
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestory");
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		cancelNotification();
	}
	
	private int getRandomPosition(){
		int mp3Count = mp3List.size();
		int randomPos = -1;
		if (mp3Count > 0) {
			randomPos = (int)(Math.random() * (mp3Count - 1));
		}
		return randomPos;
	}
}
