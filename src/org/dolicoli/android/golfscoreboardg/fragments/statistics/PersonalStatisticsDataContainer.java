package org.dolicoli.android.golfscoreboardg.fragments.statistics;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.GameAndResult;

public interface PersonalStatisticsDataContainer {
	String getPlayerName();

	int getPlayerImageResourceId();

	ArrayList<GameAndResult> getGameAndResults();
}
