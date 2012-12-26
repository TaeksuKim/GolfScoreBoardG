package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.fragments.netshare.NetShareClientFragment;
import org.dolicoli.android.golfscoreboardg.utils.FragmentUtil;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.os.Bundle;
import android.view.WindowManager;

import com.actionbarsherlock.view.MenuItem;

public class NetShareClientActivity extends Activity {

	public static final String IK_GAME_ID = "GAME_ID";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_netshare_client);

		FragmentUtil.replaceFragment(getSupportFragmentManager(), R.id.content,
				new NetShareClientFragment());

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
}
