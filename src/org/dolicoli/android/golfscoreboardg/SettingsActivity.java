package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.fragments.preferences.PrefActivityFromResourceFragment;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content,
						new PrefActivityFromResourceFragment()).commit();
	}
}
