package com.arvin.cocoplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.arvin.cocoplay.Mp3Service.Mp3SerBinder;
import com.arvin.custom.LyricView;
import com.arvin.custom.RefreshableView;
import com.arvin.custom.RefreshableView.PullToRefreshListener;
import com.arvin.custom.SideBar;
import com.arvin.custom.SideBar.OnTouchingLetterChangedListener;
import com.arvin.pojo.Lyric;
import com.arvin.pojo.Mp3;
import com.arvin.tools.Blur;
import com.arvin.tools.FileUtils;
import com.arvin.tools.Tools;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

public class MainActivity extends Activity{
	private String TAG = "MainActivity";
	private final static int OP_POSITION_CHANGE = 1;
	private final static int PLAYING_POSITION_CHANGE = 2;
	private final static int MP3_REFRESH = 3;
    private final static int ACTIVITY_LOAD_ALBUM_IMAGE = 4;
	
	public static int currentPosition = -1;	// 初始化为-1，即初始界面不显示item下面的操作栏
	public static int currentPlayingPosition = -1;
	
	
	private int playedPosition = 0;
	private int currentSeekMax = 0;
	private int mp3Count = -1;
	
	float touchPositionAtFirst = 0;
	float offset = 0;

	private SortAdapter adapter;
	private List<Mp3> mp3List;

	private Mp3Loader mp3Loader;
	private Tools tools;
	private ProgressReceiver progressReceiver;
	private FileUtils imgUtils;
	

	private SideBar sideBar;
	private TextView dialog;
	private ListView mp3ListView;
	private RefreshableView refreshableView;
	private TextView songName_text;
	private TextView singer_text;
	private TextView usedTime_text;
	private TextView totalTime_text;
	private TextView totalMp3_text;
	private ImageView playApause_btn;
	private ImageView next_btn;
	private ImageView album_img;
	private View footerView;
	private SeekBar main_seekBar;
	private View playRandom_layout;
	private TextView playRandom_text;
	private ImageView playRandom_img;
    private SlidingUpPanelLayout slidingLayout;
    private RelativeLayout play_songInfo_layout;
    private RelativeLayout contentView_layout;
    private ImageView detail_singer_img;
    private ImageView detail_back_img;
    private ImageView detail_previous_img;
    private ImageView detail_play_pause_img;
    private ImageView detail_next_img;
    private ImageView detail_mode_img;
    private SeekBar detail_seekBar;
    private TextView detail_time_used;
    private TextView detail_time_total;
    private TextView detail_title_text;
    public static LyricView detail_lyric_view;
	
	private Mp3SerBinder mp3SerBinder;
	private ServiceConnection mp3SerConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mp3SerBinder = (Mp3SerBinder) service;
		}
	};
	
	private void connectToMp3Ser() {
		Intent intent = new Intent(MainActivity.this, Mp3Service.class);
		bindService(intent, mp3SerConn, BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mp3Loader = Mp3Loader.getInstance(getContentResolver());
		imgUtils = FileUtils.getInstance(MainActivity.this);
		tools = new Tools();
		connectToMp3Ser();
		
		initViews();
	}

	
	@Override
	protected void onResume() {
		Log.i("MainActivity", "onResume");

		setData(false);
		if (mp3List.size() > 0) {
			if (mp3SerBinder != null) {
			currentPlayingPosition = mp3SerBinder.bindGetCurrentMp3Position();
			}
			
			if (currentPlayingPosition >= 0) {
				initPlayingLayout(mp3List.get(currentPlayingPosition));
			} else {
				initPlayingLayout(null);
			}
		} else {
			initPlayingLayout(null);
			totalMp3_text.setText("共0首歌曲");
		}
		
		main_seekBar.setMax(currentSeekMax);
		main_seekBar.setProgress(playedPosition);
		registerReceiver();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		unregisterReceiver(progressReceiver);
		super.onPause();
	}

	private void initViews() {
		songName_text = (TextView) findViewById(R.id.songName_text);
		singer_text = (TextView) findViewById(R.id.singer_text);
		usedTime_text = (TextView) findViewById(R.id.usedTime_text);
		totalTime_text = (TextView) findViewById(R.id.totalTime_text);
		playApause_btn = (ImageView) findViewById(R.id.playApause_btn);
		next_btn = (ImageView) findViewById(R.id.next_btn);
		album_img = (ImageView) findViewById(R.id.album_img);
		sideBar = (SideBar) findViewById(R.id.sidebar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		footerView = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_footer, null, false);
		mp3ListView = (ListView) findViewById(R.id.songs_list);
		totalMp3_text = (TextView)footerView.findViewById(R.id.totalMp3_text);
		mp3ListView.addFooterView(footerView, null, false);
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		main_seekBar = (SeekBar) findViewById(R.id.main_seekBar);
		
		playRandom_layout = (View) findViewById(R.id.playRandom_layout);
		slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		play_songInfo_layout = (RelativeLayout) findViewById(R.id.play_songInfo_layout);
		contentView_layout = (RelativeLayout) findViewById(R.id.contentView_layout);
		
		detail_lyric_view = (LyricView) findViewById(R.id.detail_lyric_view);
		
	    detail_back_img = (ImageView) findViewById(R.id.detail_back_img);
	    detail_back_img.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		slidingLayout.collapsePane();
	    	}
	    });
	    detail_previous_img = (ImageView) findViewById(R.id.detail_previous_img);
	    detail_play_pause_img = (ImageView) findViewById(R.id.detail_play_pause_img);
	    detail_next_img = (ImageView) findViewById(R.id.detail_next_img);
	    detail_mode_img = (ImageView) findViewById(R.id.detail_mode_img);
	    detail_mode_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mp3SerBinder.bindUpdateMode();
			}
		});
	    detail_seekBar = (SeekBar) findViewById(R.id.detail_seekBar);
	    detail_seekBar.setOnSeekBarChangeListener(new MySeekBarListener());
	    detail_time_used = (TextView) findViewById(R.id.detail_time_used);
	    detail_time_total = (TextView) findViewById(R.id.detail_time_total);
	    detail_title_text = (TextView) findViewById(R.id.detail_title_text);
		
		detail_singer_img = (ImageView) findViewById(R.id.detail_singer_img);
		Bitmap sentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.adele);
		Bitmap singerMap = new Blur().fastblur(MainActivity.this, sentBitmap, 50);
		detail_singer_img.setImageBitmap(singerMap);
		
		play_songInfo_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Toast.makeText(MainActivity.this, "click", 3000).show();
				slidingLayout.expandPane();
			}
		});
		final int screenHeight = MainActivity.this.getWindowManager().getDefaultDisplay().getHeight();
		play_songInfo_layout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					touchPositionAtFirst = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int screenHeight = MainActivity.this.getWindowManager().getDefaultDisplay().getHeight();
					float currentYPos = event.getRawY();
					Log.i(TAG, "move at first=" + touchPositionAtFirst);
					offset = touchPositionAtFirst - currentYPos;
					touchPositionAtFirst = currentYPos;
					
					Log.i(TAG, "y=" + event.getRawY() + "offset=" + offset);
					if (currentYPos-150 >= 0) {
						slidingLayout.smoothSlideTo((currentYPos-150)/screenHeight, 0);
					}
					//slidingLayout.setPanelHeight(Math.round(screenHeight - currentYPos + 50));
					break;
				case MotionEvent.ACTION_UP:
					if (offset > 0) {
						slidingLayout.expandPane();
					} else {
						slidingLayout.collapsePane();
					}
					slidingLayout.setPanelHeight(0);
					break;
				}
				return false;
			}
		});
		
		slidingLayout.setPanelSlideListener(new PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				Log.i(TAG, "onPanelSlide, offset " + slideOffset);
//				detail_title_text.setVisibility(View.VISIBLE);
//				playLayout.setVisibility(View.INVISIBLE);
				if (slideOffset == 0) {
					Log.i(TAG, "setVisibility=GONE");
					contentView_layout.setVisibility(View.GONE);
				} else if (contentView_layout.getVisibility() == View.GONE) {
					Log.i(TAG, "setVisibility=VISIBLE");
					contentView_layout.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPanelExpanded(View panel) {
				Log.i(TAG, "onPanelExpanded");
				contentView_layout.setVisibility(View.GONE);
			}

			@Override
			public void onPanelCollapsed(View panel) {
				Log.i(TAG, "onPanelCollapsed");
				slidingLayout.setPanelHeight(0);
			}

			@Override
			public void onPanelAnchored(View panel) {
				Log.i(TAG, "onPanelAnchored");

			}
		});
		
//		ViewHolder.playRandom_text = (TextView) findViewById(R.id.playRandom_text);
//		ViewHolder.playRandom_img = (ImageView) findViewById(R.id.playRandom_img);
		
		playRandom_layout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mp3SerBinder.bindPlayRandom();
			}
		});
		playRandom_layout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				playRandom_text = (TextView) v.findViewById(R.id.playRandom_text);
				playRandom_img = (ImageView) v.findViewById(R.id.playRandom_img);
				switch (event.getAction()) {
					case KeyEvent.ACTION_DOWN:
						playRandom_text.setTextColor(Color.WHITE);
						playRandom_img.setImageResource(R.drawable.ic_player_mode_random_pressed);
						break;
					case KeyEvent.ACTION_UP:
						playRandom_text.setTextColor(Color.BLACK);
						playRandom_img.setImageResource(R.drawable.ic_player_mode_random_default);
						break;
					default:
						break;
				}
				return false;
			}
		});
		

		OpViewOnClickListener opOnClickListener = new OpViewOnClickListener();
		playApause_btn.setOnClickListener(opOnClickListener);
		next_btn.setOnClickListener(opOnClickListener);
		detail_play_pause_img.setOnClickListener(opOnClickListener);
		detail_next_img.setOnClickListener(opOnClickListener);
		detail_previous_img.setOnClickListener(opOnClickListener);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					mp3ListView.setSelection(position);
				}

			}
		});

		mp3ListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 保存当前正在播放的歌曲位置
				currentPlayingPosition = position;
				
				adapter.notifyDataSetChanged();
				initPlayingLayout(mp3List.get(currentPlayingPosition));
				if (mp3SerBinder.bindIsPlaying() && position == mp3SerBinder.bindGetCurrentMp3Position()) {
					mp3SerBinder.bindPause();
				} else {
					if (position != mp3SerBinder.bindGetCurrentMp3Position()) {
						playedPosition = 0;
					}
					mp3SerBinder.bindPlay(currentPlayingPosition, playedPosition);
				}
				setPlayBtn();
			}
		});

		album_img.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (adapter != null) {
					adapter.notifyDataSetChanged();
					mp3ListView.smoothScrollToPosition(currentPlayingPosition);
				}
			}
		});

		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				if (adapter != null) {
					// doInBackground中不能更新UI，所以放到handler去执行
					handler.sendEmptyMessage(MP3_REFRESH);
				}
			}
		}, 0);
		
		// 定义seekbar触碰操作，触碰到时暂停，抬起播放
		main_seekBar.setOnSeekBarChangeListener(new MySeekBarListener());
	}
	
	private class MySeekBarListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				mp3SerBinder.bindUpdateProgress(progress);
				usedTime_text.setText(tools.durationFormat(progress) + " / ");
				detail_time_used.setText(tools.durationFormat(progress));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mp3SerBinder.bindSeekPause();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mp3SerBinder.bindStart();
		}
	}
	
	private class OpViewOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.detail_play_pause_img:
			case R.id.playApause_btn:
				playOrPause();
				break;
			case R.id.detail_next_img:
			case R.id.next_btn:
				playNext();
				break;
			case R.id.detail_previous_img:
				playPrevious();
			default:
				break;
			}
		}
	}
	
	private void playOrPause() {
		if (currentPlayingPosition >= 0) {
			if (mp3SerBinder.bindIsPlaying()) {
				mp3SerBinder.bindPause();
			} else {
				mp3SerBinder.bindPlay(currentPlayingPosition, playedPosition);
			}
			setPlayBtn();
		}
	}
	
	private void playNext() {
		mp3SerBinder.bindPlayNext();
		currentPlayingPosition = mp3SerBinder.bindGetCurrentMp3Position();
		adapter.notifyDataSetChanged();
	}
	
	private void playPrevious() {
		mp3SerBinder.bindPlayPrevious();
		currentPlayingPosition = mp3SerBinder.bindGetCurrentMp3Position();
		adapter.notifyDataSetChanged();
	}

	private void initPlayingLayout(final Mp3 mp3) {
		if (mp3 != null) {
			final String url = "http://i1217.photobucket.com/albums/dd382/winningprizes/a0f5c39c4b6d4f5b792002a8451898a1.jpg"; 
			if (mp3List.get(currentPlayingPosition).getTitle().toUpperCase().contains("ADELE")) {
	    		   StringBuffer imgName = new StringBuffer();
	    		   imgName.append(mp3List.get(currentPlayingPosition).getAlbum());
	    		   if (imgUtils.isFileExists(imgName.toString())) {
	    			   album_img.setImageBitmap(imgUtils.getBitmap(imgName.toString()));
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
		                       msg.what = ACTIVITY_LOAD_ALBUM_IMAGE;  
		                       msg.obj = bmp;  
		                       handler.sendMessage(msg);  
		                   }  
		               }).start();
	    		   }
	    	   } else {
	    		   album_img.setImageResource(R.drawable.playing_bar_default_avatar);
	    	   }
	
			songName_text.setText(mp3.getTitle());
			singer_text.setText(mp3.getArtist());
			usedTime_text.setText(tools.durationFormat(playedPosition) + " / ");
			totalTime_text.setText(tools.durationFormat(mp3.getDuration()));
			detail_time_used.setText(tools.durationFormat(playedPosition));
			detail_time_total.setText(tools.durationFormat(mp3.getDuration()));
		} else {
			usedTime_text.setText("--:-- / ");
			totalTime_text.setText("--:--");
			detail_time_used.setText("--:--");
			detail_time_total.setText("--:--");
		}
		
	}
	
	private void setPlayBtn() {
		if (mp3SerBinder.bindIsPlaying()) {
			playApause_btn.setImageResource(R.drawable.btn_pause);
			detail_play_pause_img.setImageResource(R.drawable.btn_simple_pause);
		} else {
			playApause_btn.setImageResource(R.drawable.btn_play);
			detail_play_pause_img.setImageResource(R.drawable.btn_simple_play);
		}
	}

	/**
	 * 设置数据 当refresh = false 时从cocoplay.db中的获取 当refresh =
	 * true时从MediaStore.Audio.Media中获取并更新cocoplay.db
	 * 
	 * @param refresh
	 */
	private void setData(boolean refresh) {

		Log.i("MainActivity", "setDate");
		if (!refresh) {
			mp3List = mp3Loader.getMp3List();
		} else {
			mp3List = mp3Loader.refreshMp3List();
		}

		mp3Count = mp3List.size();
		totalMp3_text.setText("共" + mp3Count + "首歌曲");

		if (adapter == null) {
			adapter = new SortAdapter(this, mp3List);
			mp3ListView.setAdapter(adapter);
		} else if (refresh) {
			adapter = null;
			adapter = new SortAdapter(this, mp3List);
			mp3ListView.setAdapter(adapter);
		}else {
			mp3ListView.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case OP_POSITION_CHANGE:
					adapter.notifyDataSetChanged();
					break;
				case MP3_REFRESH:
					setData(true);
					mp3SerBinder.bindRefreshMp3List();
					adapter.notifyDataSetChanged();

					refreshableView.finishRefreshing();
					break;
				case PLAYING_POSITION_CHANGE:
					initPlayingLayout(mp3List.get(currentPlayingPosition));
					adapter.notifyDataSetChanged();
					setPlayBtn();
					break;
				case ACTIVITY_LOAD_ALBUM_IMAGE:
					Bitmap bmp=(Bitmap)msg.obj;
                    Log.i(TAG, "handler - set image");
                    StringBuffer imgName = new StringBuffer();
                    imgName.append(mp3List.get(currentPlayingPosition).getAlbum());
                    try {
						imgUtils.savaBitmap(imgName.toString(), bmp);
						Log.i(TAG, "handler 保存图片" + imgName + "成功");
					} catch (IOException e) {
						Log.i(TAG, "handler 保存图片" + imgName + "失败");
						e.printStackTrace();
					}
		        	album_img.setImageBitmap(bmp);
				default:
					break;
			}
		}
	};
	
	class ProgressReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Mp3Service.ACTION_UDPATE_PROGRESS.equals(action)){
				int progress = intent.getIntExtra("progress", 0);
				if (progress > 0){
					playedPosition = progress;
					main_seekBar.setProgress(progress);
					detail_seekBar.setProgress(progress);
					usedTime_text.setText(tools.durationFormat(progress) + " / ");
					detail_time_used.setText(tools.durationFormat(progress));
				}
			} else if (Mp3Service.ACTION_UPDATE_CURRENT_MP3.equals(action)){
				currentPlayingPosition = intent.getIntExtra("currentMp3Position", 0);
				currentSeekMax = intent.getIntExtra("maxDuration", 0);				
				int max = currentSeekMax;
				Log.v(TAG, "[Main ProgressReciver] Receive duration : " + max);
				main_seekBar.setMax(currentSeekMax);
				detail_seekBar.setMax(currentSeekMax);
				detail_title_text.setText(mp3List.get(currentPlayingPosition).getTitle());
				
				handler.sendEmptyMessage(PLAYING_POSITION_CHANGE);
			} else if (Mp3Service.INTENT_ACTION_PAUSE.equals(action) || Mp3Service.INTENT_ACTION_PLAY.equals(action)) {
				setPlayBtn();
			} else if (Mp3Service.INTENT_ACTION_MODE.equals(action)) {
				int currentMode = mp3SerBinder.bindGetCurrentPlayMode();
				updateModeImg(currentMode);
			}
		}
	}
	
	/**
	 * 
	 * @Title: updateModeImg
	 * @Description: 通过传入的mode值设置对应的播放模式图标
	 * @param @param mode
	 * @return void 
	 * @throws
	 */
	private void updateModeImg(int mode) {
		StringBuilder msg = new StringBuilder();
		switch (mode) {
		case Mp3Service.MODE_LIST_LOOP:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_all);
			msg.append("列表循环");
			break;
		case Mp3Service.MODE_RANDOM:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_random);
			msg.append("随机播放");
			break;
		case Mp3Service.MODE_SEQUENCE:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_sequence);
			msg.append("顺序播放");
			break;
		case Mp3Service.MODE_SINGLE_LOOP:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_single);
			msg.append("单曲循环");
			break;
		default:
			break;
		}
		showToast(msg.toString());
	}
	
	private void showToast(String msg) {
		Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
	}
	
	private void registerReceiver(){
		progressReceiver = new ProgressReceiver();	
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Mp3Service.ACTION_UDPATE_PROGRESS);
		intentFilter.addAction(Mp3Service.ACTION_UPDATE_CURRENT_MP3);
		intentFilter.addAction(Mp3Service.INTENT_ACTION_PAUSE);
		intentFilter.addAction(Mp3Service.INTENT_ACTION_PLAY);
		intentFilter.addAction(Mp3Service.INTENT_ACTION_MODE);
		registerReceiver(progressReceiver, intentFilter);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_logout:
				stopService(new Intent(MainActivity.this, Mp3Service.class));
				finish();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
        	moveTaskToBack(true); //设置该activity永不过期，即不执行onDestroy()
        	onStop();
        }
          
        return false;  
    }  

	@Override
	protected void onDestroy() {
		Log.i("MainActivity", "onDestory");
		unbindService(mp3SerConn);
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}
	
//	@Override
//	public void finish() {
//	    moveTaskToBack(true); //设置该activity永不过期，即不执行onDestroy()
//	}  
}
