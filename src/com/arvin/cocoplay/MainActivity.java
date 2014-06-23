package com.arvin.cocoplay;

import java.io.File;
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
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import com.arvin.custom.VisualizerView;
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
    private final static int LOAD_IMAGE = 5;
    private final static int BLUE_RADIUS = 25;

	protected int UPDATE_SINGER_IMG = 5;
	
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
    public static VisualizerView waveformView;
    public static RelativeLayout playAndDetail_layout;
    private ImageView detail_switch_lrc_visualizer;
	private Visualizer visualizer;
	public static final String LYRICBASEPATH = Environment.getExternalStorageDirectory().getPath() + "/cocoplayer/lyrics/";
	public static boolean isLrcInit = false;
	private static boolean isImageBlur = true;
	
	private static Bitmap originSingerBitmap;
	private static Bitmap blurSingerBitmap;
	
	private Bitmap defBmp;
	
	private Mp3SerBinder mp3SerBinder;
	private ServiceConnection mp3SerConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mp3SerBinder = (Mp3SerBinder) service;
			showToast("onServiceConnected");
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
		connectToMp3Ser();

//		Log.i(TAG, "mp3SerBinder " + mp3SerBinder.toString());

		mp3Loader = Mp3Loader.getInstance(getContentResolver());
		imgUtils = FileUtils.getInstance(MainActivity.this);
		tools = new Tools();

		ensureOrCreateLyricFolder();
		initViews();

	}
	
	private void ensureOrCreateLyricFolder() {
		//判断SDCard是否存在，并且可以读写
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
		    File lyricFolder = new File(LYRICBASEPATH);
		    
		    Log.i(TAG, LYRICBASEPATH);
		    if (!lyricFolder.exists()) {
		    	lyricFolder.mkdirs();
		    }
		}   
	}
	
	@Override
	protected void onResume() {
		Log.i("MainActivity", "onResume");

		setData(false);
		setViews(false);
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

		int currentMode = 2;
		if (mp3SerBinder != null) {
			currentMode = mp3SerBinder.bindGetCurrentPlayMode();
		}
		updateModeImg(currentMode);
		registerReceiver();
		Intent intent = new Intent(Mp3Service.INTENT_ACTION_INITIAL_WIDGET);
		intent.setClass(MainActivity.this, Mp3Service.class);
		startService(intent);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		unregisterReceiver(progressReceiver);
		super.onPause();
	}

	private void initViews() {
		defBmp = BitmapFactory.decodeResource(getResources(), R.drawable.playing_bar_default_avatar);
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
		
		playAndDetail_layout = (RelativeLayout) findViewById(R.id.playAndDetail_layout);
		detail_lyric_view = (LyricView) findViewById(R.id.detail_lyric_view);
		waveformView = new VisualizerView(MainActivity.this);
		
		detail_singer_img = (ImageView) findViewById(R.id.detail_singer_img);
		originSingerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.playing_bar_default_avatar);
		blurSingerBitmap = Blur.fastblur(MainActivity.this, originSingerBitmap, BLUE_RADIUS);
		detail_singer_img.setImageBitmap(blurSingerBitmap);

		detail_switch_lrc_visualizer = (ImageView) findViewById(R.id.detail_switch_lrc_visualizer);
		detail_switch_lrc_visualizer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (detail_lyric_view.getVisibility() != View.VISIBLE) {
					detail_lyric_view.setVisibility(View.VISIBLE);
					waveformView.setVisibility(View.GONE);
				} else {
					if (visualizer == null) {
						setupVisualizerFxAndUI();
					}
					visualizer.setEnabled(true);
					waveformView.setVisibility(View.VISIBLE);
					detail_lyric_view.setVisibility(View.GONE);
				}
			}
		});
		
		detail_switch_lrc_visualizer.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				switchImageBetweenBlurAndClear();
				return true; // 返回值为true时长按会振动
			}
		});
		
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
		
		play_songInfo_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
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
				detail_mode_img.setImageResource(R.drawable.ic_player_mode_random);
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

				adapter.updateListView(mp3List);	
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
					adapter.updateListView(mp3List);	
					mp3ListView.smoothScrollToPosition(currentPlayingPosition);
				}
			}
		});

		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				setData(true);
				handler.sendEmptyMessage(MP3_REFRESH);
			}
		}, 0);
		
		// 定义seekbar触碰操作，触碰到时暂停，抬起播放
		main_seekBar.setOnSeekBarChangeListener(new MySeekBarListener());
		
	}
	
	private void switchImageBetweenBlurAndClear() {
		if (isImageBlur) {
			detail_singer_img.setImageBitmap(originSingerBitmap);
			isImageBlur = false;
		} else {
			blurSingerBitmap = Blur.fastblur(MainActivity.this, originSingerBitmap, BLUE_RADIUS);
			detail_singer_img.setImageBitmap(blurSingerBitmap);
			isImageBlur = true;
		}
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
	}
	
	private void playPrevious() {
		mp3SerBinder.bindPlayPrevious();
		currentPlayingPosition = mp3SerBinder.bindGetCurrentMp3Position();
		adapter.updateListView(mp3List);	
	}

	private void initPlayingLayout(final Mp3 mp3) {
		if (mp3 != null) {
			
			String fileName = mp3List.get(currentPlayingPosition).getTitle();
			display(R.id.album_img, fileName, false, defBmp);
			display(R.id.detail_singer_img, fileName, true, defBmp);
	
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
	
	private void display(final int recId, final String fileName, final boolean isBlur, final Bitmap defBmp) {
		originSingerBitmap = defBmp;
		
		if (imgUtils.isFileExists(fileName)) {

			Bitmap imgToSet = imgUtils.getBitmap(fileName);
			originSingerBitmap = imgToSet;
			Message msg = new Message();
			msg.what = ACTIVITY_LOAD_ALBUM_IMAGE;
			Bundle bundle = new Bundle();
			bundle.putParcelable("bitmap", imgToSet);
			bundle.putInt("recId", recId);
			bundle.putParcelable("defBmp", defBmp);
    		bundle.putBoolean("isBlur", isBlur);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}  else {
			Message msg = new Message();
			msg.what = ACTIVITY_LOAD_ALBUM_IMAGE;
			Bundle bundle = new Bundle();
			bundle.putParcelable("bitmap", null);
			bundle.putInt("recId", recId);
			bundle.putParcelable("defBmp", defBmp);
    		bundle.putBoolean("isBlur", isBlur);
			msg.setData(bundle);
			handler.sendMessage(msg);
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

	}
	
	private void setViews(boolean refresh) {
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
		adapter.updateListView(mp3List);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MP3_REFRESH:;
					mp3SerBinder.bindRefreshMp3List();
					setViews(true);
					
					refreshableView.finishRefreshing();

					break;
				case PLAYING_POSITION_CHANGE:
					initPlayingLayout(mp3List.get(currentPlayingPosition));
					adapter.updateListView(mp3List);;
					setPlayBtn();
		    		
					break;
				case ACTIVITY_LOAD_ALBUM_IMAGE:
					Bundle bundle = msg.getData();
					setDisplayWithBundle(bundle);
		        	break;
				case LOAD_IMAGE:
					String fileName = mp3List.get(currentPlayingPosition).getTitle();
					display(R.id.album_img, fileName, false, defBmp);
					display(R.id.detail_singer_img, fileName, true, defBmp);
				default:
					break;
			}
		}
	};
	
	public void setDisplayWithBundle(Bundle bundle) {
		Bitmap bmp = (Bitmap) bundle.get("bitmap");
		Bitmap defBmp = (Bitmap) bundle.get("defBmp");
		boolean isBlur = bundle.getBoolean("isBlur");
		int recId = bundle.getInt("recId");
		ImageView imgView = (ImageView) findViewById(recId);
		
		if (bmp != null && isBlur) {
			if (isImageBlur) {
				Bitmap bluredBmp = Blur.fastblur(MainActivity.this, bmp, BLUE_RADIUS);
				imgView.setImageBitmap(bluredBmp);
			} else {
				imgView.setImageBitmap(bmp);
			}
		} else if (bmp != null && !isBlur) {
			imgView.setImageBitmap(bmp);
		} else if (bmp == null && isBlur && defBmp != null) {
			Bitmap bluredBmp = Blur.fastblur(MainActivity.this, defBmp, BLUE_RADIUS);
			imgView.setImageBitmap(bluredBmp);
		} else if (bmp == null && !isBlur && defBmp != null) {
			imgView.setImageBitmap(defBmp);
		}
	} 
	
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

				if (!isLrcInit) {
					showToast("new lrcrunnable");
					new Thread(new LrcRunnable()).start();
				}
				setLrc();
				
				handler.sendEmptyMessage(PLAYING_POSITION_CHANGE);
			} else if (Mp3Service.INTENT_ACTION_PAUSE.equals(action) || Mp3Service.INTENT_ACTION_PLAY.equals(action)) {
				setPlayBtn();
			} else if (Mp3Service.INTENT_ACTION_MODE.equals(action)) {
				int currentMode = intent.getIntExtra("currentPlayMode", 2);
				updateModeImg(currentMode);
			} else if (Mp3Service.INTENT_ACTION_LOAD_IMAGE.equals(action)) {
				handler.sendEmptyMessage(LOAD_IMAGE);
			}
		}
	}
	
	private void updateModeImg(int mode) {
		switch (mode) {
		case Mp3Service.MODE_LIST_LOOP:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_all);
			break;
		case Mp3Service.MODE_RANDOM:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_random);
			break;
		case Mp3Service.MODE_SEQUENCE:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_sequence);
			break;
		case Mp3Service.MODE_SINGLE_LOOP:
			detail_mode_img.setImageResource(R.drawable.ic_player_mode_single);
			break;
		default:
			break;
		}
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
		intentFilter.addAction(Mp3Service.INTENT_ACTION_LOAD_IMAGE);
		registerReceiver(progressReceiver, intentFilter);
	}
	
	private void setupVisualizerFxAndUI() {  
		MainActivity.waveformView = new VisualizerView(this);  
		
		RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(  
                ViewGroup.LayoutParams.MATCH_PARENT,  
                500)); 
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); 
		lp.setMargins(10, 0, 10, 0);
		waveformView.setLayoutParams(lp);
//		waveformView.setBackgroundColor(Color.parseColor("#44000000"));
		playAndDetail_layout.addView(waveformView);  
  
        final int maxCR = Visualizer.getMaxCaptureRate();  
          
        visualizer = new Visualizer(mp3SerBinder.bindGetAudioSessionId());  
        visualizer.setCaptureSize(256);  
		visualizer.setDataCaptureListener(
			new Visualizer.OnDataCaptureListener() {
				public void onWaveFormDataCapture(Visualizer visualizer,
						byte[] bytes, int samplingRate) {
					waveformView.updateVisualizer(bytes);
				}

				public void onFftDataCapture(Visualizer visualizer,
						byte[] fft, int samplingRate) {
					waveformView.updateVisualizer(fft);
				}
			}, maxCR / 2, false, true);
	}
	
	public void setLrc() {
		String lrc = getCurrentMp3LyricPath();
		
		Log.i(TAG, "in setLrc lrc=" + lrc);
		detail_lyric_view.readLRC(lrc);
		detail_lyric_view.setTextSize();
		detail_lyric_view.setOffsetY(350);
	}	
	
	private String getCurrentMp3LyricPath() {
		if (mp3SerBinder != null && currentPlayingPosition >= 0) {
			StringBuilder lyricPath = new StringBuilder();
	        lyricPath.append(LYRICBASEPATH);
	        lyricPath.append(mp3List.get(currentPlayingPosition).getTitle());
	        lyricPath.append(".lrc");
	        return lyricPath.toString();
		} else {
			return "";
		}
	}
	
	class LrcRunnable implements Runnable {
		public void run() {
			boolean isLrcShow = true;
			isLrcInit = true;
			while (isLrcShow) {
				try {
					Thread.sleep(100);
					if (mp3SerBinder != null && mp3SerBinder.bindIsPlaying() && slidingLayout.isExpanded()) {
						detail_lyric_view.setOffsetY(detail_lyric_view.getOffsetY() - detail_lyric_view.setScollSpeed());
						detail_lyric_view.setCurrentIndex(mp3SerBinder.bindGetCurrentProgress());
						mHandler.post(mUpdateResults);
					} else if (slidingLayout.isExpanded()) {
						isLrcShow = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	Handler mHandler = new Handler();
	Runnable mUpdateResults = new Runnable() {
		public void run() {
			detail_lyric_view.invalidate(); // 更新视图
		}
	};
	
	
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
        	if (slidingLayout.isExpanded()) {
        		slidingLayout.collapsePane();
        	} else {
        		moveTaskToBack(true); //设置该activity永不过期，即不执行onDestroy()
        		onStop();
        	}
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
	protected void onRestart() {
		Log.i(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}
	
}