package org.dolicoli.android.golfscoreboardg;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.PlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.ResultDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.main.CurrentGameDataContainer;
import org.dolicoli.android.golfscoreboardg.fragments.main.CurrentGameHoleResultFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.CurrentGameSummaryFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.OldGamesSummaryFragment;
import org.dolicoli.android.golfscoreboardg.net.GameUploader;
import org.dolicoli.android.golfscoreboardg.tasks.GameReceiveTask;
import org.dolicoli.android.golfscoreboardg.tasks.GameReceiveTask.GameReceiveTaskListener;
import org.dolicoli.android.golfscoreboardg.tasks.GameReceiveTask.ReceiveProgress;
import org.dolicoli.android.golfscoreboardg.tasks.GameReceiveTask.ReceiveResult;
import org.dolicoli.android.golfscoreboardg.tasks.ThreeMonthsGameReceiveTask;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MainActivity extends Activity implements CurrentGameDataContainer,
		GameReceiveTaskListener, OnClickListener, OnPageChangeListener {

	private static final int TAB_CURRENT_GAME_SUMMARY_FRAGMENT = 0;
	private static final int TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT = 1;
	private static final int TAB_OLD_GAMES_SUMMARY_FRAGMENT = 2;

	private static final int TAB_COUNT = TAB_OLD_GAMES_SUMMARY_FRAGMENT + 1;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private Button addResultButton;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		addResultButton = (Button) findViewById(R.id.AddResultButton);
		addResultButton.setOnClickListener(this);
		findViewById(R.id.ImportButton).setOnClickListener(this);

		getSupportActionBar()
				.setTitle(R.string.activity_main_title_score_board);

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

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		CurrentGameSummaryFragment currentGameSummaryFragment = new CurrentGameSummaryFragment();
		CurrentGameHoleResultFragment currentGameHoleResultFragment = new CurrentGameHoleResultFragment();
		OldGamesSummaryFragment oldGamesSummaryFragment = new OldGamesSummaryFragment();

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
			case TAB_OLD_GAMES_SUMMARY_FRAGMENT:
				return oldGamesSummaryFragment;
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

		@SuppressLint("DefaultLocale")
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case TAB_CURRENT_GAME_SUMMARY_FRAGMENT:
				return getString(R.string.title_section1).toUpperCase();
			case TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT:
				return getString(R.string.title_section2).toUpperCase();
			case TAB_OLD_GAMES_SUMMARY_FRAGMENT:
				return getString(R.string.title_section3).toUpperCase();
			}
			return null;
		}
	}

	public static class DummySectionFragment extends Fragment {
		public DummySectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			Bundle args = getArguments();
			textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
			return textView;
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.AddResultButton:
			addResult();
			return;
		case R.id.ImportButton:
			importThisGame();
			return;
		}
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffest,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (position != TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT) {
			CurrentGameHoleResultFragment fragment = (CurrentGameHoleResultFragment) mSectionsPagerAdapter
					.getItem(TAB_CURRENG_GAME_HOLE_RESULT_FRAGMENT);
			if (fragment == null || !fragment.isAdded()) {
				return;
			}

			fragment.hideActionMode();
		}
	}

	private static final int REQ_ADD_RESULT = 0x0001;
	private static final int REQ_NEW_GAME = 0x0002;
	private static final int REQ_MODIFY_GAME = 0x0003;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case REQ_NEW_GAME:
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				boolean autoUpload = preferences.getBoolean(
						getString(R.string.preference_auto_upload_key), true);
				if (autoUpload) {
					exportData();
				}
			}
			reload();
			return;
		case REQ_MODIFY_GAME:
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				boolean autoUpload = preferences.getBoolean(
						getString(R.string.preference_auto_upload_key), true);
				if (autoUpload) {
					exportData();
				}
			}
			reload();
			return;
		case REQ_ADD_RESULT:
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				boolean autoUpload = preferences.getBoolean(
						getString(R.string.preference_auto_upload_key), true);
				if (autoUpload) {
					exportData();
				}
			}
			reload();
			return;
		}
	}

	public void setUIStatus() {
		if (addResultButton == null)
			return;

		CurrentGameSummaryFragment feeFragment = (CurrentGameSummaryFragment) mSectionsPagerAdapter
				.getItem(0);
		if (feeFragment == null) {
			addResultButton.setEnabled(false);
		} else if (feeFragment.isAllGameFinished()) {
			addResultButton.setEnabled(false);
		} else {
			addResultButton.setEnabled(true);
		}
	}

	@Override
	public void showResetDialog() {
		new AlertDialog.Builder(this)
				// .setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.activity_main_dialog_reset)
				.setMessage(R.string.activity_main_dialog_are_you_sure_to_reset)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								resetDatabase();
								reload();
							}

						}).setNegativeButton(android.R.string.no, null).show();
	}

	@Override
	public void addResult() {
		CurrentGameSummaryFragment currentScoreFragment = (CurrentGameSummaryFragment) mSectionsPagerAdapter
				.getItem(TAB_CURRENT_GAME_SUMMARY_FRAGMENT);
		if (currentScoreFragment == null) {
			return;
		}

		if (currentScoreFragment.isAllGameFinished()) {
			return;
		}

		Intent intent = new Intent(this, CurrentGameAddResultActivity.class);
		startActivityForResult(intent, REQ_ADD_RESULT);
	}

	@Override
	public void newGame() {
		Intent intent = new Intent(this,
				CurrentGameNewGameSettingActivity.class);
		startActivityForResult(intent, REQ_NEW_GAME);
	}

	@Override
	public void modifyGame() {
		Intent intent = new Intent(this,
				CurrentGameModifyGameSettingActivity.class);
		startActivityForResult(intent, REQ_MODIFY_GAME);
	}

	@Override
	public void showExportDataDialog() {
		new AlertDialog.Builder(this)
				// .setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.activity_main_dialog_netshare_export)
				.setMessage(
						R.string.activity_main_dialog_are_you_sure_to_netshare_upload)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								exportData();
							}

						}).setNegativeButton(android.R.string.no, null).show();
	}

	@Override
	public void importData() {
		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				MainActivity.this);
		GameSetting gameSetting = new GameSetting();
		gameSettingWorker.getGameSetting(gameSetting);

		String currentGameId = GameSetting
				.toGameIdFormat(gameSetting.getDate());

		Intent intent = new Intent(this, NetShareClientActivity.class);
		intent.putExtra(NetShareClientActivity.IK_GAME_ID, currentGameId);
		startActivity(intent);
	}

	@Override
	public void saveHistory() {
		HistoryGameSettingDatabaseWorker worker = new HistoryGameSettingDatabaseWorker(
				this);
		worker.addCurrentHistory(true);

		reload();
	}

	@Override
	public void reload() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setUIStatus();

				if (mSectionsPagerAdapter == null)
					return;

				int count = mSectionsPagerAdapter.getCount();
				Reloadable fragment = null;
				for (int i = 0; i < count; i++) {
					fragment = (Reloadable) mSectionsPagerAdapter.getItem(i);
					if (fragment != null)
						fragment.reload();
				}
			}
		});
	}

	@Override
	public void showSettingActivity() {
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
	}

	@Override
	public void importThreeMonthData() {
		new AlertDialog.Builder(this)
				.setTitle("3���� �ڷ� ��������")
				.setMessage("�ֱ� 3���� �ڷḦ �������ðڽ��ϱ�?\n���� ���� �� �� �ֽ��ϴ�.")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ThreeMonthsGameReceiveTask task = new ThreeMonthsGameReceiveTask(
										MainActivity.this, MainActivity.this);
								task.execute("");
							}

						}).setNegativeButton(android.R.string.no, null).show();
	}

	private void exportData() {
		UploadDataTask task = new UploadDataTask();
		task.execute();
	}

	private void resetDatabase() {
		ResultDatabaseWorker resultWorker = new ResultDatabaseWorker(this);
		resultWorker.reset();
	}

	private void importThisGame() {
		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				MainActivity.this);
		GameSetting gameSetting = new GameSetting();
		gameSettingWorker.getGameSetting(gameSetting);

		String currentGameId = GameSetting
				.toGameIdFormat(gameSetting.getDate());

		GameReceiveTask task = new GameReceiveTask(this, this);
		task.execute(currentGameId);
	}

	private class UploadDataTask extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			GolfScoreBoardApplication application = (GolfScoreBoardApplication) getApplication();
			String host = application.getWebHost();

			GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
					MainActivity.this);
			GameSetting gameSetting = new GameSetting();
			gameSettingWorker.getGameSetting(gameSetting);

			PlayerSettingDatabaseWorker playerSettingWorker = new PlayerSettingDatabaseWorker(
					MainActivity.this);
			PlayerSetting playerSetting = new PlayerSetting();
			playerSettingWorker.getPlayerSetting(playerSetting);

			ResultDatabaseWorker resultWorker = new ResultDatabaseWorker(
					MainActivity.this);
			ArrayList<Result> results = resultWorker.getResults();

			return GameUploader.upload(host, gameSetting, playerSetting,
					results);
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			if (progress == null || progress.length < 1)
				return;

			setProgressDialogStatus(progress[0]);
		}

		@Override
		protected void onPreExecute() {
			showProgressDialog();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(MainActivity.this,
						R.string.activity_main_netshare_upload_success,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MainActivity.this,
						R.string.activity_main_netshare_upload_fail,
						Toast.LENGTH_LONG).show();
			}
			hideProgressDialog();
		}
	}

	private void showProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			return;
		}
		final Activity activity = this;
		progressDialog = ProgressDialog.show(activity,
				getString(R.string.activity_main_dialog_netshare_please_wait),
				getString(R.string.activity_main_dialog_netshare_preparing),
				true, false);
	}

	private void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private void setProgressDialogStatus(final String message) {
		if (progressDialog == null) {
			showProgressDialog();
		} else {
			progressDialog.setMessage(message);
		}
	}

	@Override
	public void onReceiveStart() {
		showProgressDialog();
	}

	@Override
	public void onReceiveProgressUpdate(ReceiveProgress value) {
		if (value == null)
			return;

		int messageId = value.getMessageId();
		if (messageId > 0) {
			setProgressDialogStatus(getString(messageId));
		} else {
			setProgressDialogStatus(value.getMessage());
		}
	}

	@Override
	public void onReceiveFinished(ReceiveResult result) {
		if (result.isSuccess()) {
			if (!result.isCancel()) {
				Toast.makeText(MainActivity.this,
						R.string.activity_main_netshare_import_success,
						Toast.LENGTH_LONG).show();
				reload();
			}
		} else {
			Toast.makeText(MainActivity.this,
					R.string.activity_main_netshare_import_fail,
					Toast.LENGTH_LONG).show();
		}
		hideProgressDialog();
	}

}
