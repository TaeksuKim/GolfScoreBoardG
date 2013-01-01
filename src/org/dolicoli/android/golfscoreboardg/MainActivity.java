package org.dolicoli.android.golfscoreboardg;

import java.util.Locale;

import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.ResultDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.DummySectionFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.AttendCountFragment;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameHoleResultFragment;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameSummaryFragment;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask.ImportProgress;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask.ImportResult;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends Activity implements
		ImportCurrentGameTask.TaskListener, OnClickListener {

	private static final int TAB_CURRENT_GAME_SUMMARY_FRAGMENT = 0;
	private static final int TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT = 1;
	private static final int TAB_ATTEND_COUNT_FRAGMENT = 2;

	private static final int TAB_COUNT = TAB_ATTEND_COUNT_FRAGMENT + 1;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		findViewById(R.id.ImportButton).setOnClickListener(this);

		getSupportActionBar().setTitle(R.string.activity_main_title);

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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		reload(true);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.ImportButton:
			importCurentGame();
			return;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Reset:
			showResetDialog();
			return true;
		case R.id.NetShareReceiveData:
			importData();
			return true;
		case R.id.Save:
			saveHistory();
			return true;
		case R.id.Settings:
			showSettingActivity();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static final int REQ_IMPORT = 0x0004;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case REQ_IMPORT:
			if (resultCode == Activity.RESULT_OK) {
				reload(true);
			}
			return;
		}
	}

	private void showResetDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_reset)
				.setMessage(R.string.dialog_are_you_sure_to_reset)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								resetDatabase();
								reload(true);
							}

						}).setNegativeButton(android.R.string.no, null).show();
	}

	private void importData() {
		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				MainActivity.this);
		GameSetting gameSetting = new GameSetting();
		gameSettingWorker.getGameSetting(gameSetting);

		String currentGameId = GameSetting
				.toGameIdFormat(gameSetting.getDate());

		Intent intent = new Intent(this, NetShareClientActivity.class);
		intent.putExtra(NetShareClientActivity.IK_GAME_ID, currentGameId);
		startActivityForResult(intent, REQ_IMPORT);
	}

	private void saveHistory() {
		HistoryGameSettingDatabaseWorker worker = new HistoryGameSettingDatabaseWorker(
				this);
		worker.addCurrentHistory(true);

		reload(false);
	}

	private void showSettingActivity() {
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
	}

	@Override
	public void onImportGameStart() {
		showProgressDialog(R.string.dialog_import_current_game,
				R.string.dialog_import_please_wait);
	}

	@Override
	public void onImportGameProgressUpdate(ImportProgress progress) {
		if (progress == null)
			return;

		int messageId = progress.getMessageId();
		setProgressDialogStatus(R.string.dialog_import_current_game,
				R.string.dialog_import_please_wait, progress.getCurrent(),
				progress.getTotal(), getString(messageId));
	}

	@Override
	public void onImportGameFinished(ImportResult result) {
		if (result.isSuccess()) {
			if (!result.isCancel()) {
				Toast.makeText(MainActivity.this,
						R.string.activity_main_netshare_import_success,
						Toast.LENGTH_LONG).show();
				reload(false);
			}
		} else {
			Toast.makeText(MainActivity.this,
					R.string.activity_main_netshare_import_fail,
					Toast.LENGTH_LONG).show();
		}
		hideProgressDialog();
	}

	private void reload(final boolean clean) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mSectionsPagerAdapter == null)
					return;

				int count = mSectionsPagerAdapter.getCount();
				Reloadable fragment = null;
				for (int i = 0; i < count; i++) {
					fragment = (Reloadable) mSectionsPagerAdapter.getItem(i);
					if (fragment != null)
						fragment.reload(clean);
				}
			}
		});
	}

	private void resetDatabase() {
		ResultDatabaseWorker resultWorker = new ResultDatabaseWorker(this);
		resultWorker.reset();
	}

	private void importCurentGame() {
		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				this);
		GameSetting gameSetting = new GameSetting();
		gameSettingWorker.getGameSetting(gameSetting);

		String currentGameId = GameSetting
				.toGameIdFormat(gameSetting.getDate());

		ImportCurrentGameTask task = new ImportCurrentGameTask(this, this);
		task.execute(currentGameId);
	}

	private void showProgressDialog(int defaultTitleId, int defaultMessageId) {
		if (progressDialog != null && progressDialog.isShowing()) {
			return;
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(defaultTitleId);
		progressDialog.setMessage(getString(defaultMessageId));
		progressDialog.setIndeterminate(true);
		progressDialog.setMax(100);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.show();
	}

	private void setProgressDialogStatus(int defaultTitleId,
			int defaultMessageId, final int current, final int total,
			final String message) {
		if (progressDialog == null) {
			showProgressDialog(defaultTitleId, defaultMessageId);
		} else {
			if (total <= 0) {
				progressDialog.setIndeterminate(true);
			} else {
				progressDialog.setIndeterminate(false);

				progressDialog.setMax(total);
				progressDialog.setProgress(current);
			}
			progressDialog.setMessage(message);
		}
	}

	private void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private class SectionsPagerAdapter extends FragmentPagerAdapter {
		OneGameSummaryFragment currentGameSummaryFragment = new OneGameSummaryFragment();
		OneGameHoleResultFragment currentGameHoleResultFragment = new OneGameHoleResultFragment();
		AttendCountFragment attendCountFragment = new AttendCountFragment();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case TAB_CURRENT_GAME_SUMMARY_FRAGMENT:
				return currentGameSummaryFragment;
			case TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT:
				return currentGameHoleResultFragment;
			case TAB_ATTEND_COUNT_FRAGMENT:
				return attendCountFragment;
			}
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return TAB_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case TAB_CURRENT_GAME_SUMMARY_FRAGMENT:
				return getString(
						R.string.activity_main_fragment_current_game_summary)
						.toUpperCase(Locale.getDefault());
			case TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT:
				return getString(
						R.string.activity_main_fragment_current_game_hole_result)
						.toUpperCase(Locale.getDefault());
			case TAB_ATTEND_COUNT_FRAGMENT:
				return getString(R.string.activity_main_fragment_attend_count)
						.toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}
}
