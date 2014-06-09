package com.arvin.custom;

import java.util.Comparator;

import com.arvin.pojo.Mp3;

public class PinyinComparator implements Comparator<Mp3> {

	public int compare(Mp3 o1, Mp3 o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
