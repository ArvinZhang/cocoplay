package com.arvin.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.arvin.pojo.Lyric;

/**
 * @Title: LyricView.java
 * @Package com.arvin.custom
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang
 * @date 2014年6月3日 下午5:07:53
 * @version V1.0
 */
public class LyricView extends View {

	private static TreeMap<Integer, Lyric> lrc_map;
	private float mX; // 屏幕X轴的中点，此值固定，保持歌词在X中间显示
	private float offsetY; // 歌词在Y轴上的偏移量，此值会根据歌词的滚动变小
	private static boolean blLrc = false;
	private float touchY; // 当触摸歌词View时，保存为当前触点的Y轴坐标
	private float touchX;
	private boolean blScrollView = false;
	private int lrcIndex = 0; // 保存歌词TreeMap的下标
	private int SIZEWORD = 0; // 显示歌词文字的大小值
	private int INTERVAL = 20; // 歌词每行的间隔
	TextPaint paint = new TextPaint(); // 画笔，用于画不是高亮的歌词
	TextPaint paintHL = new TextPaint(); // 画笔，用于画高亮的歌词，即当前唱到这句歌词
	private Context context;

	public LyricView(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (blLrc) {
			paintHL.setTextSize(SIZEWORD);
			paint.setTextSize(SIZEWORD);
			Lyric temp = lrc_map.get(lrcIndex);
//			canvas.drawText(temp.getLrc(), mX, offsetY + (SIZEWORD + INTERVAL)
//					* lrcIndex, paintHL);
			
			int screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
			int textZoneWidth = screenWidth - 20;
			
			StaticLayout layout = new StaticLayout(temp.getLrc(), paintHL,  
		            textZoneWidth,  
		            Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);  
		    canvas.translate(10 + Math.abs(textZoneWidth-temp.getLrc().length()*SIZEWORD)/2, 0);
		    layout.draw(canvas);
			
			// 画当前歌词之前的歌词
			for (int i = lrcIndex - 1; i >= 0; i--) {
				temp = lrc_map.get(i);
				if (offsetY + (SIZEWORD + INTERVAL) * i < 0) {
					break;
				}
				canvas.drawText(temp.getLrc(), mX, offsetY
						+ (SIZEWORD + INTERVAL) * i, paint);
			}
			// 画当前歌词之后的歌词
			for (int i = lrcIndex + 1; i < lrc_map.size(); i++) {
				temp = lrc_map.get(i);
				if (offsetY + (SIZEWORD + INTERVAL) * i > 600) {
					break;
				}
				canvas.drawText(temp.getLrc(), mX, offsetY
						+ (SIZEWORD + INTERVAL) * i, paint);
			}
		} else {
			paint.setTextSize(25);
			canvas.drawText("找不到歌词", mX, 310, paint);
		}
		//super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		System.out.println("bllll===" + blScrollView);
		float tt = event.getY();
		if (!blLrc) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			touchY = tt - touchY;
			offsetY = offsetY + touchY;
			break;
		case MotionEvent.ACTION_UP:
			blScrollView = false;
			break;
		}
		touchY = tt;
		return true;
	}

	public void init() {
		lrc_map = new TreeMap<Integer, Lyric>(); // TreeMap中元素的排列是有序的（HashMap中元素的排列顺序是不固定的）
		offsetY = 320;

		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true); // 防止锯齿
		paint.setDither(true); // 防抖动
		paint.setAlpha(180); // 设置透明度（0-255），值越小越透明

		paintHL.setTextAlign(Paint.Align.CENTER);

		paintHL.setColor(Color.RED);
		paintHL.setAntiAlias(true);
		paintHL.setAlpha(255);
	}

	/**
	 * 
	 * @Title: SetTextSize
	 * @Description: 根据歌词里面最长的那句来确定歌词字体的大小
	 * @param 
	 * @return void 
	 * @throws
	 */
	public void setTextSize() {
		if (!blLrc) {
			return;
		}
		int max = lrc_map.get(0).getLrc().length();
		for (int i = 1; i < lrc_map.size(); i++) {
			Lyric lrcStrLength = lrc_map.get(i);
			if (max < lrcStrLength.getLrc().length()) {
				max = lrcStrLength.getLrc().length();
			}
		}
		SIZEWORD = 25;
		Log.i("LyricView", "SIZEWORD = " + SIZEWORD);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mX = w * 0.5f;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public Float setScollSpeed() {
		float speed = 0;
		if (offsetY + (SIZEWORD + INTERVAL) * lrcIndex > 220) {
			speed = ((offsetY + (SIZEWORD + INTERVAL) * lrcIndex - 220) / 20);
		} else if (offsetY + (SIZEWORD + INTERVAL) * lrcIndex < 120) {
			speed = 0;
		}

		return speed;
	}

	public void setCurrentIndex(int time) {
		if (blLrc) {
			int index = 0;
			for (int i = 0; i < lrc_map.size(); i++) {
				Lyric temp = lrc_map.get(i);
				if (temp.getBegintime() < time) {
					++index;
				}
			}
			lrcIndex = index - 1;
			if (lrcIndex < 0) {
				lrcIndex = 0;
			}
		}
	}

	public static void readLRC(String file) {
		TreeMap<Integer, Lyric> lrc_read = new TreeMap<Integer, Lyric>();
		String data = "";
		try {
			File saveFile = new File(file);
			if (!saveFile.isFile()) {
				blLrc = false;
				return;
			}
			blLrc = true;

			FileInputStream stream = new FileInputStream(saveFile); // context.openFileInput(file);

			BufferedReader br = new BufferedReader(new InputStreamReader(stream, "GB2312"));
			int i = 0;
			Pattern pattern = Pattern.compile("\\d{2}");
			while ((data = br.readLine()) != null) {
				data = data.replace("[", "");// 将前面的替换成后面的
				data = data.replace("]", "@");
				String splitdata[] = data.split("@");// 分隔
				if (data.endsWith("@")) {
					for (int k = 0; k < splitdata.length; k++) {
						String str = splitdata[k];

						str = str.replace(":", ".");
						str = str.replace(".", "@");
						String timedata[] = str.split("@");
						Matcher matcher = pattern.matcher(timedata[0]);
						if (timedata.length == 3 && matcher.matches()) {
							int m = Integer.parseInt(timedata[0]); // 分
							int s = Integer.parseInt(timedata[1]); // 秒
							int ms = Integer.parseInt(timedata[2]); // 毫秒
							int currTime = (m * 60 + s) * 1000 + ms * 10;
							Lyric item1 = new Lyric();
							item1.setBegintime(currTime);
							item1.setLrc("");
							lrc_read.put(currTime, item1);
						}
					}
				} else {
					String lrcContenet = splitdata[splitdata.length - 1];

					for (int j = 0; j < splitdata.length - 1; j++) {
						String tmpstr = splitdata[j];

						tmpstr = tmpstr.replace(":", ".");
						tmpstr = tmpstr.replace(".", "@");
						String timedata[] = tmpstr.split("@");
						Matcher matcher = pattern.matcher(timedata[0]);
						if (timedata.length == 3 && matcher.matches()) {
							int m = Integer.parseInt(timedata[0]); // 分
							int s = Integer.parseInt(timedata[1]); // 秒
							int ms = Integer.parseInt(timedata[2]); // 毫秒
							int currTime = (m * 60 + s) * 1000 + ms * 10;
							Lyric item1 = new Lyric();
							item1.setBegintime(currTime);
							item1.setLrc(lrcContenet);
							lrc_read.put(currTime, item1);// 将currTime当标签
															// item1当数据
															// 插入TreeMap里
							i++;
						}
					}
				}
			}
			stream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		/*
		 * 遍历hashmap 计算每句歌词所需要的时间
		 */
		lrc_map.clear();
		data = "";
		Iterator<Integer> iterator = lrc_read.keySet().iterator();
		Lyric oldval = null;
		int i = 0;
		while (iterator.hasNext()) {
			Object ob = iterator.next();

			Lyric val = (Lyric) lrc_read.get(ob);

			if (oldval == null)
				oldval = val;
			else {
				Lyric item1 = new Lyric();
				item1 = oldval;
				item1.setTimeline(val.getBegintime() - oldval.getBegintime());
				lrc_map.put(new Integer(i), item1);
				i++;
				oldval = val;
			}
			if (!iterator.hasNext()) {
				lrc_map.put(new Integer(i), val);
			}

		}

	}

	public static boolean isBlLrc() {
		return blLrc;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	public int getSIZEWORD() {
		return SIZEWORD;
	}

	public void setSIZEWORD(int sIZEWORD) {
		SIZEWORD = sIZEWORD;
	}
}
