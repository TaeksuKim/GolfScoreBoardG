package org.dolicoli.android.golfscoreboardg.fragments.history;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.dolicoli.android.golfscoreboardg.PersonalStatisticsActivity;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.data.GameAndResult;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.utils.PlayerUIUtil;
import org.dolicoli.android.golfscoreboardg.utils.TagProgressBar;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PlayerRankingFragment extends ListFragment implements
		HistoryDataFragment {

	private PlayerInfoListAdapter adapter;

	private HistoryDataContainer dataContainer;

	public void setDataContainer(HistoryDataContainer container) {
		this.dataContainer = container;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.history_player_ranking_fragment,
				null);

		adapter = new PlayerInfoListAdapter(getActivity(),
				R.layout.history_player_ranking_list_item);
		adapter.setNotifyOnChange(false);
		setListAdapter(adapter);

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		PlayerInfo playerInfo = adapter.getItem(position);
		if (playerInfo == null)
			return;

		String playerName = playerInfo.name;
		Intent intent = new Intent(getActivity(),
				PersonalStatisticsActivity.class);
		intent.putExtra(PersonalStatisticsActivity.IK_PLAYER_NAME, playerName);
		startActivity(intent);
	}

	@Override
	public void reload(int index) {
		if (getActivity() == null || dataContainer == null)
			return;

		ArrayList<GameAndResult> results = null;
		switch (index) {
		case HistoryDataContainer.INDEX_THIS_MONTH:
			results = dataContainer.getThisMonthGameAndResults();
			break;
		case HistoryDataContainer.INDEX_LAST_MONTH:
			results = dataContainer.getLastMonthGameAndResults();
			break;
		case HistoryDataContainer.INDEX_RECENT_FIVE_GAMES:
		case HistoryDataContainer.INDEX_LAST_THREE_MONTH:
			results = dataContainer.getAllGameAndResults();
			break;
		}
		if (results == null) {
			adapter.clear();
			adapter.notifyDataSetChanged();
		} else {
			HashMap<String, PlayerInfo> playerInfoMap = new HashMap<String, PlayerInfo>();
			if (index == HistoryDataContainer.INDEX_RECENT_FIVE_GAMES) {
				HashMap<String, Integer> playerGameCountMap = new HashMap<String, Integer>();
				for (GameAndResult gameAndResult : results) {
					int playerCount = gameAndResult.getGameSetting()
							.getPlayerCount();
					PlayerSetting playerSettings = gameAndResult
							.getPlayerSetting();
					for (int playerId = 0; playerId < playerCount; playerId++) {
						String playerName = playerSettings
								.getPlayerName(playerId);
						playerName = PlayerUIUtil
								.toCanonicalName(playerName);
						if (playerName.startsWith("Player"))
							continue;

						if (!playerGameCountMap.containsKey(playerName)) {
							playerGameCountMap.put(playerName, 0);
						}

						int playerGameCount = playerGameCountMap
								.get(playerName);
						if (playerGameCount >= 5)
							continue;

						if (!playerInfoMap.containsKey(playerName)) {
							playerInfoMap.put(playerName, new PlayerInfo(
									playerName));
						}
						PlayerInfo playerInfo = playerInfoMap.get(playerName);
						playerInfo.increaseScore(gameAndResult.getPlayerScore(
								playerName).getOriginalScore());

						playerGameCountMap.put(playerName, playerGameCount + 1);
					}
				}
			} else {
				for (GameAndResult gameAndResult : results) {
					int playerCount = gameAndResult.getGameSetting()
							.getPlayerCount();
					PlayerSetting playerSettings = gameAndResult
							.getPlayerSetting();
					for (int playerId = 0; playerId < playerCount; playerId++) {
						String playerName = playerSettings
								.getPlayerName(playerId);
						playerName = PlayerUIUtil
								.toCanonicalName(playerName);
						if (playerName.startsWith("Player"))
							continue;

						if (!playerInfoMap.containsKey(playerName)) {
							playerInfoMap.put(playerName, new PlayerInfo(
									playerName));
						}
						PlayerInfo playerInfo = playerInfoMap.get(playerName);
						playerInfo.increaseScore(gameAndResult.getPlayerScore(
								playerName).getOriginalScore());
					}
				}
			}

			Collection<PlayerInfo> values = playerInfoMap.values();
			PlayerInfo[] playerInfos = new PlayerInfo[values.size()];
			values.toArray(playerInfos);
			for (PlayerInfo playerInfo : playerInfos) {
				playerInfo.calculate();
			}
			Arrays.sort(playerInfos);

			adapter.clear();
			for (PlayerInfo info : playerInfos) {
				adapter.add(info);
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void hideActionMode() {
	}

	private static class ScoreListViewHolder {
		View tagView;
		TagProgressBar attendCountProgressBar;
		ImageView playerImageView;
		TextView playerNameTextView, attendCountTextView;
	}

	private class PlayerInfoListAdapter extends ArrayAdapter<PlayerInfo>
			implements OnClickListener {

		private ScoreListViewHolder holder;
		private int textViewResourceId;

		public PlayerInfoListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);

			this.textViewResourceId = textViewResourceId;
			TypedValue tv = new TypedValue();
			context.getTheme().resolveAttribute(R.attr.primaryTextColor, tv,
					true);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
				holder = new ScoreListViewHolder();

				holder.tagView = v.findViewById(R.id.PlayerTagView);
				holder.attendCountProgressBar = (TagProgressBar) v
						.findViewById(R.id.AttendCountProgressBar);
				holder.playerImageView = (ImageView) v
						.findViewById(R.id.PlayerImage);
				holder.playerNameTextView = (TextView) v
						.findViewById(R.id.PlayerNameTextView);
				holder.attendCountTextView = (TextView) v
						.findViewById(R.id.AttendCountTextView);

				v.setTag(holder);
			} else {
				holder = (ScoreListViewHolder) v.getTag();
			}

			int count = getCount();
			if (count < 1)
				return v;

			if (position < 0 || position > count - 1)
				return v;

			PlayerInfo playerInfo = getItem(position);
			if (playerInfo == null)
				return v;

			holder.attendCountProgressBar.setMax(100);
			double rate = (140.0 - ((double) playerInfo.avgOfScore - 16.0) * 5.0) / 280.0 + 0.3;
			if (rate < 0.0) {
				rate = 0.0;
			}
			holder.attendCountProgressBar.setPos((int) (rate * 100.0));

			holder.tagView.setBackgroundColor(PlayerUIUtil
					.getTagColor(playerInfo.name));
			holder.attendCountProgressBar.setColor(PlayerUIUtil
					.getTagColor(playerInfo.name));
			holder.playerImageView.setImageResource(PlayerUIUtil
					.getRoundResourceId(playerInfo.name));
			holder.playerNameTextView.setText(playerInfo.name);

			DecimalFormat overParScoreCountFormat = new DecimalFormat("+0.00");
			DecimalFormat underParScoreCountFormat = new DecimalFormat("0.00");

			String attendText = getString(
					R.string.fragment_personal_statistics_attend_count_format,
					playerInfo.attend);
			if (playerInfo.avgOfScore > 0) {
				holder.attendCountTextView.setText(overParScoreCountFormat
						.format(playerInfo.avgOfScore)
						+ " ("
						+ attendText
						+ ")");
			} else {
				holder.attendCountTextView.setText(underParScoreCountFormat
						.format(playerInfo.avgOfScore)
						+ " ("
						+ attendText
						+ ")");
			}

			return v;
		}

		@Override
		public void onClick(View v) {
			if (!(v instanceof Button))
				return;

			Button button = (Button) v;
			View more = (View) button.getTag();
			if (more.getVisibility() == View.VISIBLE) {
				more.setVisibility(View.GONE);
				button.setBackgroundResource(R.drawable.ic_expand);
			} else {
				more.setVisibility(View.VISIBLE);
				button.setBackgroundResource(R.drawable.ic_collapse);
			}
		}
	}

	private static class PlayerInfo implements Comparable<PlayerInfo> {
		private int attend = 0;
		private int sumOfScore = 0;
		private float avgOfScore = 0;
		private String name;

		private PlayerInfo(String name) {
			this.name = name;
			this.sumOfScore = 0;
			this.attend = 0;
		}

		public void increaseScore(int score) {
			this.sumOfScore += score;
			this.attend++;
		}

		public void calculate() {
			if (attend < 1) {
				avgOfScore = 0F;
			} else {
				avgOfScore = (float) sumOfScore / (float) attend;
			}
		}

		@Override
		public int compareTo(PlayerInfo compare) {
			if (attend >= 5 && compare.attend < 5)
				return -1;
			if (attend < 5 && compare.attend >= 5)
				return 1;
			if (avgOfScore < compare.avgOfScore)
				return -1;
			if (avgOfScore > compare.avgOfScore)
				return 1;
			if (attend > compare.attend)
				return -1;
			if (attend < compare.attend)
				return 1;
			return name.compareTo(compare.name);
		}
	}
}
