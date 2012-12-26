package org.dolicoli.android.golfscoreboardg.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentUtil {
	public static void replaceFragment(FragmentManager fragmentManager,
			int resId, Fragment fragment) {
		replaceFragment(fragmentManager, resId, fragment, null);
	}

	public static void replaceFragment(FragmentManager fragmentManager,
			int resId, Fragment fragment, String backStackName) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(resId, fragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		if (backStackName != null) {
			ft.addToBackStack(backStackName);
		}
		ft.commit();
	}
}
