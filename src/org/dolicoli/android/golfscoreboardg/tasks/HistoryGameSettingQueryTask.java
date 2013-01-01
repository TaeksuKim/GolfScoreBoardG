package org.dolicoli.android.golfscoreboardg.tasks;

import org.dolicoli.android.golfscoreboardg.data.SingleGameResult;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;

import android.content.Context;
import android.os.AsyncTask;

public class HistoryGameSettingQueryTask extends AsyncTask<String, Void, SingleGameResult> {

	private Context context;
	private TaskListener listener;

	public HistoryGameSettingQueryTask(Context context, TaskListener listener) {
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (listener != null) {
			listener.onGameQueryStarted();
		}
	}

	@Override
	protected void onPostExecute(SingleGameResult result) {
		super.onPostExecute(result);
		if (listener != null) {
			listener.onGameQueryFinished(result);
		}
	}

	@Override
	protected SingleGameResult doInBackground(String... params) {
		if (context == null || params == null || params.length < 1) {
			return null;
		}

		String playDate = params[0];

		GameSetting gameSetting = new GameSetting();
		PlayerSetting playerSetting = new PlayerSetting();

		HistoryGameSettingDatabaseWorker historyGameWorker = new HistoryGameSettingDatabaseWorker(
				context);
		historyGameWorker.getGameSetting(playDate, gameSetting, playerSetting);

		SingleGameResult result = new SingleGameResult();
		result.setGameSetting(gameSetting);
		result.setPlayerSetting(playerSetting);

		return result;
	}

	public static interface TaskListener {
		void onGameQueryStarted();

		void onGameQueryFinished(SingleGameResult gameResult);
	}
}
