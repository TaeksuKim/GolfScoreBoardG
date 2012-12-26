package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.MainActivity.DummySectionFragment;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryGameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.HistoryPlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.db.PlayerSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGamePlayerHoleRecordFragment;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGamePlayerRecordActivityPage;
import org.dolicoli.android.golfscoreboardg.fragments.onegame.OneGamePlayerRecordSummaryFragment;
import org.dolicoli.android.golfscoreboardg.utils.PlayerUIUtil;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;

public class OneGamePlayerRecordActivity extends Activity implements
		OnNavigationListener {

	public static final String IK_CURRENT = "CURRENT";
	public static final String IK_PLAYER_ID = "PLAYER_ID";
	public static final String IK_PLAY_DATE = "PLAYER_DATE";

	private static final int TAB_PLAYER_RECORD_SUMMARY_FRAGMENT = 0;
	private static final int TAB_PLAYER_HOLE_RECORD_FRAGMENT = 1;

	private static final int TAB_COUNT = TAB_PLAYER_HOLE_RECORD_FRAGMENT + 1;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	private ArrayAdapter<Player> navigationAdapter;

	private int tabIndex;
	private String playDate;

	private String playerName;
	private int playerImageResourceId;
	private ImageView playerImageView;
	private boolean currentGame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		currentGame = false;
		Intent intent = getIntent();
		if (intent != null) {
			currentGame = intent.getBooleanExtra(IK_CURRENT, false);
			playDate = intent.getStringExtra(IK_PLAY_DATE);
		}

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		setContentView(R.layout.activity_onegame_player_record);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		playerImageView = (ImageView) findViewById(R.id.PlayerImageView);

		GameSetting gameSetting = new GameSetting();
		PlayerSetting playerSetting = new PlayerSetting();
		if (currentGame) {
			GameSettingDatabaseWorker gameWorker = new GameSettingDatabaseWorker(
					this);
			gameWorker.getGameSetting(gameSetting);

			PlayerSettingDatabaseWorker playerWorker = new PlayerSettingDatabaseWorker(
					this);
			playerWorker.getPlayerSetting(playerSetting);
		} else {
			HistoryGameSettingDatabaseWorker gameWorker = new HistoryGameSettingDatabaseWorker(
					this);
			gameWorker.getGameSetting(playDate, gameSetting);

			HistoryPlayerSettingDatabaseWorker playerWorker = new HistoryPlayerSettingDatabaseWorker(
					this);
			playerWorker.getPlayerSetting(playDate, playerSetting);
		}

		int playerCount = gameSetting.getPlayerCount();
		Player[] players = new Player[playerCount];
		for (int playerId = 0; playerId < playerCount; playerId++) {
			String name = playerSetting.getPlayerName(playerId);
			players[playerId] = new Player(playerId, name);
		}

		navigationAdapter = new ArrayAdapter<Player>(this,
				android.R.layout.simple_list_item_1, players);
		actionBar.setListNavigationCallbacks(navigationAdapter, this);
		actionBar.setTitle("");

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
			setResult(0);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (savedInstanceState == null) {
			tabIndex = 0;

			int initialPlayerId = 0;
			Intent intent = getIntent();
			if (intent != null) {
				initialPlayerId = intent.getIntExtra(IK_PLAYER_ID, 0);
			}

			int count = navigationAdapter.getCount();
			Player item = null;
			for (int i = 0; i < count; i++) {
				item = navigationAdapter.getItem(i);
				if (item.playerId == initialPlayerId) {
					tabIndex = i;
					break;
				}
			}
		} else {
			tabIndex = savedInstanceState.getInt("TAB_INDEX");
		}

		ActionBar actionBar = getSupportActionBar();
		actionBar.setSelectedNavigationItem(tabIndex);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition < 0 || itemPosition > navigationAdapter.getCount() - 1)
			return false;

		Player item = navigationAdapter.getItem(itemPosition);
		if (mSectionsPagerAdapter != null) {
			setPlayerId(item.playerId);
		}

		playerName = PlayerUIUtil.toCanonicalName(item.playerName);
		if (playerImageView != null) {
			playerImageResourceId = PlayerUIUtil.getResourceId(playerName);
			playerImageView.setImageResource(playerImageResourceId);
		}

		return true;
	}

	private void setPlayerId(int playerId) {
		if (mSectionsPagerAdapter == null)
			return;

		for (int i = 0; i < TAB_COUNT; i++) {
			OneGamePlayerRecordActivityPage fragment = (OneGamePlayerRecordActivityPage) mSectionsPagerAdapter
					.getItem(i);
			if (fragment != null) {
				fragment.setPlayerId(currentGame, playerId, playDate);
			}
		}
	}

	private static class Player {
		private int playerId;
		private String playerName;

		public Player(int playerId, String playerName) {
			this.playerId = playerId;
			this.playerName = playerName;
		}

		@Override
		public String toString() {
			return playerName;
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		OneGamePlayerRecordSummaryFragment playerRecordSummaryFragment = new OneGamePlayerRecordSummaryFragment();
		OneGamePlayerHoleRecordFragment playerHoleRecordFragment = new OneGamePlayerHoleRecordFragment();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case TAB_PLAYER_RECORD_SUMMARY_FRAGMENT:
				return playerRecordSummaryFragment;
			case TAB_PLAYER_HOLE_RECORD_FRAGMENT:
				return playerHoleRecordFragment;
			}
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
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
			case TAB_PLAYER_RECORD_SUMMARY_FRAGMENT:
				return getString(R.string.player_record_title_section1)
						.toUpperCase();
			case TAB_PLAYER_HOLE_RECORD_FRAGMENT:
				return getString(R.string.player_record_title_section2)
						.toUpperCase();
			}
			return null;
		}
	}
}
