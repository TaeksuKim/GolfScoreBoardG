package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.PlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.ResultDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.currentgame.ModifyResultFragment;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.actionbarsherlock.view.MenuItem;

public class ModifyResultActivity extends Activity implements OnClickListener {

	public static final String IK_HOLE_NUMBER = ModifyResultFragment.IK_HOLE_NUMBER;

	private ModifyResultFragment modifyResultFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_current_game_modify_result);

		FragmentManager fragmentManager = getSupportFragmentManager();
		modifyResultFragment = (ModifyResultFragment) fragmentManager
				.findFragmentById(R.id.ModifyResultFragment);

		findViewById(R.id.ConfirmButton).setOnClickListener(this);
		findViewById(R.id.CancelButton).setOnClickListener(this);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean alwaysTurnOnScreen = preferences.getBoolean(
				getString(R.string.preference_always_turn_on_key), true);
		if (alwaysTurnOnScreen) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.ConfirmButton:
			saveResult();
			setResult(Activity.RESULT_OK);
			finish();
			return;
		case R.id.CancelButton:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return;
		}
	}

	private void saveResult() {
		int hole = modifyResultFragment.getHoleNumber();
		int parNumber = modifyResultFragment.getParNumber();
		Result result = new Result(hole, parNumber);

		for (int playerId = 0; playerId < Constants.MAX_PLAYER_COUNT; playerId++) {
			result.setScore(playerId, modifyResultFragment.getScore(playerId));
			result.setUsedHandicap(playerId,
					modifyResultFragment.getHandicap(playerId));
		}

		ResultDatabaseWorker resultWorker = new ResultDatabaseWorker(this);
		resultWorker.updateResult(result);

		PlayerSettingDatabaseWorker playerWorker = new PlayerSettingDatabaseWorker(
				this);
		playerWorker.updateUsedHandicap(result);

		HistoryGameSettingDatabaseWorker historyWorker = new HistoryGameSettingDatabaseWorker(
				this);
		historyWorker.addCurrentHistory(false);
	}
}
