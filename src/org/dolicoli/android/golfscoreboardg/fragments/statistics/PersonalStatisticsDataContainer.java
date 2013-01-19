package org.dolicoli.android.golfscoreboardg.fragments.statistics;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.OneGame;

public interface PersonalStatisticsDataContainer {
	String getPlayerName();

	int getPlayerImageResourceId();

	ArrayList<OneGame> getGameAndResults();
}
