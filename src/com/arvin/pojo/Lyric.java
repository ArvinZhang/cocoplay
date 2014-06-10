package com.arvin.pojo;

/**
 * @Title: Lyric.java
 * @Package com.arvin.pojo
 * @Description: TODO(用一句话描述该文件做什么)
 * @author arvinzhang
 * @date 2014年6月3日 下午5:05:51
 * @version V1.0
 */
public class Lyric {
	private int begintime; // 开始时间
	private int endtime; // 结束时间
	private int timeline; // 单句歌词用时
	private String lrc; // 单句歌词

	public int getBegintime() {
		return begintime;
	}

	public void setBegintime(int begintime) {
		this.begintime = begintime;
	}

	public int getEndtime() {
		return endtime;
	}

	public void setEndtime(int endtime) {
		this.endtime = endtime;
	}

	public int getTimeline() {
		return timeline;
	}

	public void setTimeline(int timeline) {
		this.timeline = timeline;
	}

	public String getLrc() {
		return lrc;
	}

	public void setLrc(String lrc) {
		this.lrc = lrc;
	}

}
