package org.dolicoli.android.golfscoreboardg;

import org.dolicoli.android.golfscoreboardg.data.DateItem;
import org.dolicoli.android.golfscoreboardg.utils.DateRangeUtil;
import org.dolicoli.android.golfscoreboardg.utils.DateRangeUtil.DateRange;

import android.app.Application;

public class GolfScoreBoardApplication extends Application {

	@SuppressWarnings("unused")
	private static final String TAG = "GolfScoreBoardApplication";

	public static final int MODE_RECENT_FIVE_GAMES = 0;
	public static final int MODE_THIS_MONTH = 1;
	public static final int MODE_LAST_MONTH = 2;
	public static final int MODE_LAST_THREE_MONTHS = 3;

	private DateItem[] navigationItems;
	private String webHost;
	private int navigationMode;
	private boolean downloadData;

	@Override
	public void onCreate() {
		super.onCreate();

		loadRangeItems();
		loadCustomProperties();
		navigationMode = MODE_RECENT_FIVE_GAMES;
	}

	public DateItem[] getNavigationItems() {
		return navigationItems;
	}

	public String getWebHost() {
		return webHost;
	}

	public int getNavigationMode() {
		return navigationMode;
	}

	public void setNavigationMode(int mode) {
		this.navigationMode = mode;
	}

	public boolean shouldDownloadData() {
		return downloadData;
	}

	public void dataDownloadFinished() {
		this.downloadData = false;
	}

	private void loadCustomProperties() {
		webHost = getString(R.string.web_host);
	}

	private void loadRangeItems() {
		navigationItems = new DateItem[4];

		DateRange dummyMonthRange = DateRangeUtil.getDateRange(2);
		navigationItems[0] = new DateItem(dummyMonthRange.getFrom(),
				dummyMonthRange.getTo(), "�ֱ� 5 ���");

		DateRange thisMonthRange = DateRangeUtil.getMonthlyDateRange(0);
		navigationItems[1] = new DateItem(thisMonthRange.getFrom(),
				thisMonthRange.getTo(), "�̹� ��");

		DateRange lastMonthRange = DateRangeUtil.getMonthlyDateRange(1);
		navigationItems[2] = new DateItem(lastMonthRange.getFrom(),
				lastMonthRange.getTo(), "���� ��");

		DateRange lastThreeMonthRange = DateRangeUtil.getDateRange(2);
		navigationItems[3] = new DateItem(lastThreeMonthRange.getFrom(),
				lastThreeMonthRange.getTo(), "�ֱ� 3����");
	}
}
