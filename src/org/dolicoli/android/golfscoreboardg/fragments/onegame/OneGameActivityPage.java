package org.dolicoli.android.golfscoreboardg.fragments.onegame;

import org.dolicoli.android.golfscoreboardg.utils.Reloadable;

public interface OneGameActivityPage extends Reloadable {

	public static final String BK_MODE = "Mode";
	public static final String BK_PLAY_DATE = "PlayDate";
	public static final String BK_HOLE_NUMBER = "HoleNumber";

	public static final int MODE_CURRENT = 1;
	public static final int MODE_HISTORY = 2;

	void setHoleNumber(int holeNumber);

}
