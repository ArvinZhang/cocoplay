package com.arvin.custom;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.arvin.cocoplay.R;
import com.arvin.pojo.Lyric;

/**
 * @Title: LyricView.java
 * @Package com.arvin.custom
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang
 * @date 2014年6月3日 下午5:07:53
 * @version V1.0
 */
public class LyricView extends TextView {
	private float width; // 歌词视图宽度
	private float height; // 歌词视图高度
	private Paint currentPaint; // 当前画笔对象
	private Paint notCurrentPaint; // 非当前画笔对象
	private float textHeight = 25; // 文本高度
	private float notCurrentTextSize = 24; // 文本大小
	private int index = 0; // list集合下标

	private List<Lyric> lrcList = new ArrayList<Lyric>();

	public void setmLrcList(List<Lyric> lrcList) {
		this.lrcList = lrcList;
	}

	public LyricView(Context context) {
		super(context);
		init();
	}

	public LyricView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setFocusable(true); 

		// 高亮部分
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true); // 设置抗锯齿，让文字美观饱满
		currentPaint.setTextAlign(Paint.Align.CENTER);// 设置文本对齐方式

		// 非高亮部分
		notCurrentPaint = new Paint();
		notCurrentPaint.setAntiAlias(true);
		notCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * 绘画歌词
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas == null) {
			return;
		}

		String claert_red = "#990505";
		currentPaint.setColor(Color.parseColor(claert_red));
		notCurrentPaint.setColor(Color.argb(140, 255, 255, 255));

		currentPaint.setTextSize(30);
		currentPaint.setTypeface(Typeface.SERIF);

		notCurrentPaint.setTextSize(notCurrentTextSize);
		notCurrentPaint.setTypeface(Typeface.DEFAULT);

		try {
			setText("");
			canvas.drawText(lrcList.get(index).getLrc(), width / 2, height / 2, currentPaint);

			float tempY = height / 2;
			// 画出本句之前的句子
			for (int i = index - 1; i >= 0; i--) {
				// 向上推移
				tempY = tempY - textHeight;
				canvas.drawText(lrcList.get(i).getLrc(), width / 2, tempY,	notCurrentPaint);
			}
			tempY = height / 2;
			// 画出本句之后的句子
			for (int i = index + 1; i < lrcList.size(); i++) {
				// 往下推移
				tempY = tempY + textHeight;
				canvas.drawText(lrcList.get(i).getLrc(), width / 2, tempY,
						notCurrentPaint);
			}
		} catch (Exception e) {
			setText("...木有歌词文件，赶紧去下载...");
		}
	}

	/**
	 * 当view大小改变的时候调用的方法
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
