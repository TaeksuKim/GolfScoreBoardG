package org.dolicoli.android.golfscoreboardg.fragments.preferences;

import org.dolicoli.android.golfscoreboardg.R;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.PreferenceScreen;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Bundle;

public class PrefActivityFromResourceFragment extends PreferenceFragment
		implements OnSharedPreferenceChangeListener {

	private SharedPreferences mainPreference;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		mainPreference = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mainPreference.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		if (mainPreference != null)
			mainPreference.registerOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	}
}