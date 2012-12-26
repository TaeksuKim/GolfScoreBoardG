package org.dolicoli.android.golfscoreboardg.tasks;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.SingleGameResult;
import org.dolicoli.android.golfscoreboardg.data.UsedHandicap;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryPlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryResultDatabaseWorker;

import android.content.Context;
import android.os.AsyncTask;

public class HistoryQueryTask extends
		AsyncTask<HistoryQueryTask.QueryParam, Void, SingleGameResult> {

	public static int WHOLE_HOLE = -1;

	private Context context;
	private TaskListener listener;

	public HistoryQueryTask(Context context, TaskListener listener) {
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
	protected SingleGameResult doInBackground(QueryParam... params) {
		if (context == null || params == null || params.length < 1) {
			return null;
		}

		String playDate = params[0].playDate;
		int holeNumber = params[0].holeNumber;

		GameSetting gameSetting = new GameSetting();
		PlayerSetting playerSetting = new PlayerSetting();
		ArrayList<Result> results = null;
		UsedHandicap usedHandicap = null;

		HistoryGameSettingDatabaseWorker gameSettingWorker = new HistoryGameSettingDatabaseWorker(
				context);
		gameSettingWorker.getGameSetting(playDate, gameSetting);

		HistoryPlayerSettingDatabaseWorker playerSettingWorker = new HistoryPlayerSettingDatabaseWorker(
				context);
		playerSettingWorker.getPlayerSetting(playDate, playerSetting);

		HistoryResultDatabaseWorker resultWorker = new HistoryResultDatabaseWorker(
				context);
		if (holeNumber > WHOLE_HOLE) {
			results = resultWorker.getResults(playDate, holeNumber);
		} else {
			results = resultWorker.getResults(playDate);
		}

		usedHandicap = resultWorker.getUsedHandicaps(playDate);

		SingleGameResult result = new SingleGameResult();
		result.setGameSetting(gameSetting);
		result.setPlayerSetting(playerSetting);
		result.setResults(results);
		result.setUsedHandicap(usedHandicap);

		return result;
	}

	public static class QueryParam {
		private String playDate;
		private int holeNumber;

		public QueryParam(String playDate) {
			this(playDate, WHOLE_HOLE);
		}

		public QueryParam(String playDate, int holeNumber) {
			this.playDate = playDate;
			this.holeNumber = holeNumber;
		}
	}

	public static interface TaskListener {
		void onGameQueryStarted();

		void onGameQueryFinished(SingleGameResult gameResult);
	}
}
