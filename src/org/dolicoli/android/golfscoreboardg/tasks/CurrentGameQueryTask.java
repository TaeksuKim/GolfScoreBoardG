package org.dolicoli.android.golfscoreboardg.tasks;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.SingleGameResult;
import org.dolicoli.android.golfscoreboardg.data.UsedHandicap;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.ResultDatabaseWorker;

import android.content.Context;
import android.os.AsyncTask;

public class CurrentGameQueryTask extends
		AsyncTask<Void, Void, SingleGameResult> {

	private Context context;
	private TaskListener listener;

	public CurrentGameQueryTask(Context context, TaskListener listener) {
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (listener != null) {
			listener.onCurrentGameQueryStarted();
		}
	}

	@Override
	protected void onPostExecute(SingleGameResult result) {
		super.onPostExecute(result);
		if (listener != null) {
			listener.onCurrentGameQueryFinished(result);
		}
	}

	@Override
	protected SingleGameResult doInBackground(Void... params) {
		if (context == null) {
			return null;
		}

		GameSetting gameSetting = new GameSetting();
		PlayerSetting playerSetting = new PlayerSetting();
		ArrayList<Result> results = new ArrayList<Result>();
		UsedHandicap usedHandicap = null;

		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				context);
		gameSettingWorker.getGameSetting(gameSetting, playerSetting, results);

		ResultDatabaseWorker resultWorker = new ResultDatabaseWorker(context);
		usedHandicap = resultWorker.getUsedHandicaps();

		SingleGameResult result = new SingleGameResult();
		result.setGameSetting(gameSetting);
		result.setPlayerSetting(playerSetting);
		result.setResults(results);
		result.setUsedHandicap(usedHandicap);

		return result;
	}

	public static interface TaskListener {
		void onCurrentGameQueryStarted();

		void onCurrentGameQueryFinished(SingleGameResult gameResult);
	}
}
