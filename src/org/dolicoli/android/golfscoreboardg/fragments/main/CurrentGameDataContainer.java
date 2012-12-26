package org.dolicoli.android.golfscoreboardg.fragments.main;

import org.dolicoli.android.golfscoreboardg.Reloadable;

public interface CurrentGameDataContainer extends Reloadable {
	void modifyGame();

	void addResult();

	void newGame();

	void showResetDialog();

	void showExportDataDialog();

	void importData();

	void saveHistory();

	void showSettingActivity();

	void importThreeMonthData();
}
