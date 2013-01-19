package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.PlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.ResultDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.currentgame.AddResultFragment;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.actionbarsherlock.view.MenuItem;

public class AddResultActivity extends Activity implements OnClickListener {

	private AddResultFragment mainFragment;
	private Button confirmButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_current_game_add_result);

		FragmentManager fragmentManager = getSupportFragmentManager();
		mainFragment = (AddResultFragment) fragmentManager
				.findFragmentById(R.id.AddResultFragment);

		confirmButton = (Button) findViewById(R.id.ConfirmButton);
		confirmButton.setOnClickListener(this);
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
			break;
		case R.id.CancelButton:
			setResult(Activity.RESULT_CANCELED);
			finish();
			break;
		}
	}

	public void inputDataChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				confirmButton.setEnabled(mainFragment.isAllFieldValid());
			}
		});
	}

	private void saveResult() {
		int hole = mainFragment.getHoleNumber();
		int parNumber = mainFragment.getParNumber();
		Result result = new Result(hole, parNumber);

		for (int playerId = 0; playerId < Constants.MAX_PLAYER_COUNT; playerId++) {
			result.setScore(playerId, mainFragment.getScore(playerId));
			result.setUsedHandicap(playerId, mainFragment.getHandicap(playerId));
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
