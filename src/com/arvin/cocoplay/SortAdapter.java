package com.arvin.cocoplay;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.arvin.pojo.Mp3;

public class SortAdapter extends BaseAdapter implements SectionIndexer{
	private String TAG = "SortAdapter";
	private List<Mp3> list = null;
	private Context mContext;
	
	public SortAdapter(Context mContext, List<Mp3> list) {
		this.mContext = mContext;
		this.list = list;
	}
	
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<Mp3> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup arg2) {
//		Log.i(TAG, "getView() position=" + position);
		
		ViewHolder viewHolder = null;
		final Mp3 mContent = list.get(position);
		if (view == null) {
			Log.i(TAG, "getView() view = null position=" + position);
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.song_list_item, null);
			viewHolder.songCate_text = (TextView) view.findViewById(R.id.songCate_text);
			viewHolder.itemSongName_text = (TextView) view.findViewById(R.id.itemSongName_text);
			viewHolder.singerAalbum_text = (TextView) view.findViewById(R.id.singerAalbum_text);
			viewHolder.playing_img = (ImageView) view.findViewById(R.id.playing_img);
			//viewHolder.songOp_img = (View) view.findViewById(R.id.songOp_img);
			viewHolder.songOp_layout = (View) view.findViewById(R.id.songOp_layout);
			viewHolder.audio_layout = (View) view.findViewById(R.id.audio_layout);
			viewHolder.setList_layout = (View) view.findViewById(R.id.setList_layout);
			viewHolder.info_layout = (View) view.findViewById(R.id.info_layout);
			viewHolder.delete_layout = (View) view.findViewById(R.id.delete_layout);
			viewHolder.song_num_text = (TextView) view.findViewById(R.id.song_num_text);
			
			view.setTag(viewHolder);	// 给将viewHolder数据添加到View，要用的时候调用getTag()获取，避免调用findViewById
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		int songSum = this.list.size();
		StringBuffer numStrBuf = new StringBuffer();
		int num = position + 1;
		if (songSum < 10) {
			viewHolder.song_num_text.setText(numStrBuf.append(num));
		} else if (songSum >= 10 && songSum < 100) {
			if (num >= 10) {
				viewHolder.song_num_text.setText(numStrBuf.append(num));
			} else {
				numStrBuf.append(0);
				viewHolder.song_num_text.setText(numStrBuf.append(num));
			}
		} else if (songSum >= 100) {
			if (num < 10) {
				numStrBuf.append("00");
				viewHolder.song_num_text.setText(numStrBuf.append(num));
			} else if (num >= 10 && position < 100) {
				numStrBuf.append(0);
				viewHolder.song_num_text.setText(numStrBuf.append(num));
			} else {
				viewHolder.song_num_text.setText(numStrBuf.append(num));
			}
		}
		
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		
		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			viewHolder.songCate_text.setVisibility(View.VISIBLE);
			viewHolder.songCate_text.setText(mContent.getSortLetters());
		}else{
			viewHolder.songCate_text.setVisibility(View.GONE);
		}

		viewHolder.itemSongName_text.setText(this.list.get(position).getTitle());
		
		StringBuffer singerAndAlbum = new StringBuffer();
		singerAndAlbum.append(this.list.get(position).getArtist());
		singerAndAlbum.append(" - ");
		singerAndAlbum.append(this.list.get(position).getAlbum());
		viewHolder.singerAalbum_text.setText(singerAndAlbum);
		
//		viewHolder.songOp_img.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (position != MainActivity.currentPosition) {
//					MainActivity.currentPosition = position;
//				} else {
//					MainActivity.currentPosition = -1;
//				}
//				notifyDataSetChanged();
//				//new MainActivity().handler.sendEmptyMessage(MainActivity.OP_POSITION_CHANGE);
//			}
//		});
		
		if (position != MainActivity.currentPlayingPosition) {
			viewHolder.playing_img.setVisibility(View.INVISIBLE);
		} else {
			viewHolder.playing_img.setVisibility(View.VISIBLE);
		}

		if (position != MainActivity.currentPosition) {
			viewHolder.songOp_layout.setVisibility(View.GONE);
			viewHolder.audio_layout.setClickable(false);
			viewHolder.setList_layout.setClickable(false);
			viewHolder.info_layout.setClickable(false);
			viewHolder.delete_layout.setClickable(false);
		} else {
			viewHolder.songOp_layout.setVisibility(View.VISIBLE);
			viewHolder.audio_layout.setClickable(true);
			viewHolder.setList_layout.setClickable(true);
			viewHolder.info_layout.setClickable(true);
			viewHolder.delete_layout.setClickable(true);
			
			viewHolder.audio_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					MainActivity.currentPosition = -1;
				}
			});
			
			viewHolder.setList_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					MainActivity.currentPosition = -1;
				}
			});
			
			viewHolder.info_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					MainActivity.currentPosition = -1;
				}
			});
			
			viewHolder.delete_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					MainActivity.currentPosition = -1;
				}
			});
		}
		
		return view;

	}
	
	/**
	 * ViewHolder缓存了view中显示数据的子控件，不用每次刷新ListView都调用findViewById方法，可提高控件的响应速度。
	 * @author arvinzhang
	 *
	 */
	final static class ViewHolder {
		TextView songCate_text;
		TextView itemSongName_text;
		TextView singerAalbum_text;
		ImageView playing_img;
		View songOp_img;
		
		View songOp_layout;
		View audio_layout;
		View setList_layout;
		View info_layout;
		View delete_layout;
		
		TextView song_num_text;
	}


	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}