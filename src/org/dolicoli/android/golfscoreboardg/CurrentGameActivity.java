package org.dolicoli.android.golfscoreboardg;

import java.util.Locale;

import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.DummySectionFragment;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameActivityPage;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameFragmentContainer;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameHoleResultFragment;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGameSummaryFragment;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask.ImportProgress;
import org.dolicoli.android.golfscoreboardg.tasks.ImportCurrentGameTask.ImportResult;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class CurrentGameActivity extends Activity implements
		OneGameFragmentContainer, ImportCurrentGameTask.TaskListener {

	private static final int TAB_CURRENT_GAME_SUMMARY_FRAGMENT = 0;
	private static final int TAB_CURRENT_GAME_HOLE_RESULT_FRAGMENT = 1;

	private static final int TAB_COUNT = TAB_CURRENT_GAME_HOLE_RESULT_FRAGMENT + 1;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.activity_currentgame_title);

		setContentView(R.layout.activity_current_game);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

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
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case R.id.ImportThisGame:
			importCurentGame();
			return true;
		case R.id.NetShareReceiveData:
			importData();
			return true;
		case R.id.Save:
			saveHistory();
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

	@Override
	public void showModifyGameSettingActivity() {
	}

	private void importData() {
		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				CurrentGameActivity.this);
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
				Toast.makeText(CurrentGameActivity.this,
						R.string.activity_main_netshare_import_success,
						Toast.LENGTH_LONG).show();
				reload(false);
			}
		} else {
			Toast.makeText(CurrentGameActivity.this,
					R.string.activity_main_netshare_import_fail,
					Toast.LENGTH_LONG).show();
		}
		hideProgressDialog();
	}

	@Override
	public void reload(final boolean clean) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mSectionsPagerAdapter == null)
					return;

				int count = mSectionsPagerAdapter.getCount();
				OneGameActivityPage fragment = null;
				for (int i = 0; i < count; i++) {
					fragment = (OneGameActivityPage) mSectionsPagerAdapter
							.getItem(i);
					if (fragment != null)
						fragment.reload(clean);
				}
			}
		});
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
		final Activity activity = this;

		progressDialog = new ProgressDialog(activity);
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

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case TAB_CURRENT_GAME_SUMMARY_FRAGMENT:
				return currentGameSummaryFragment;
			case TAB_CURRENT_GAME_HOLE_RESULT_FRAGMENT:
				return currentGameHoleResultFragment;
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
			case TAB_CURRENT_GAME_HOLE_RESULT_FRAGMENT:
				return getString(
						R.string.activity_main_fragment_current_game_hole_result)
						.toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}
}
