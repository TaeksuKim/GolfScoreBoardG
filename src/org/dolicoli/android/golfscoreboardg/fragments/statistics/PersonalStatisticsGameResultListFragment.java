package org.dolicoli.android.golfscoreboardg.fragments.statistics;

import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.Reloadable;
import org.dolicoli.android.golfscoreboardg.data.GameAndResult;
import org.dolicoli.android.golfscoreboardg.data.PlayerScore;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.utils.UIUtil;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;

public class PersonalStatisticsGameResultListFragment extends ListFragment
		implements Reloadable {

	private PersonalStatisticsDataContainer dataContainer;

	private GameAndResultListAdapter adapter;

	private ArrayList<GameAndResult> gameAndResults;
	private String playerName;

	public void setDataContainer(PersonalStatisticsDataContainer container) {
		this.dataContainer = container;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.personal_statistics_game_result_list_fragment, null);

		adapter = new GameAndResultListAdapter(getActivity(),
				R.layout.personal_statistics_game_result_list_item);
		adapter.setNotifyOnChange(false);
		setListAdapter(adapter);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (dataContainer != null) {
			gameAndResults = dataContainer.getGameAndResults();
			playerName = dataContainer.getPlayerName();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		reload(false);
	}

	@Override
	public void reload(boolean clean) {
		if (dataContainer == null)
			return;

		adapter.clear();
		for (GameAndResult gameAndResult : gameAndResults) {
			if (!gameAndResult.containsPlayerScore(playerName))
				continue;

			adapter.add(gameAndResult);
		}
		adapter.notifyDataSetChanged();
	}

	private static class GameAndResultListViewHolder {
		TextView dateTextView, rankingTextView, playerCountTextView,
				scoreTextView, feeTextView;
	}

	private class GameAndResultListAdapter extends ArrayAdapter<GameAndResult> {

		private GameAndResultListViewHolder holder;
		private int textViewResourceId;

		public GameAndResultListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);

			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FragmentActivity activity = getActivity();

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) activity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
				holder = new GameAndResultListViewHolder();

				holder.dateTextView = (TextView) v
						.findViewById(R.id.DateTextView);
				holder.rankingTextView = (TextView) v
						.findViewById(R.id.RankingTextView);
				holder.playerCountTextView = (TextView) v
						.findViewById(R.id.PlayerCountTextView);
				holder.scoreTextView = (TextView) v
						.findViewById(R.id.ScoreTextView);
				holder.feeTextView = (TextView) v
						.findViewById(R.id.FeeTextView);

				v.setTag(holder);
			} else {
				holder = (GameAndResultListViewHolder) v.getTag();
			}

			int count = getCount();
			if (count < 1)
				return v;

			if (position < 0 || position > count - 1)
				return v;

			GameAndResult gameAndResult = getItem(position);
			if (gameAndResult == null)
				return v;

			GameSetting gameSetting = gameAndResult.getGameSetting();
			String dateString = DateUtils.formatDateTime(activity, gameSetting
					.getDate().getTime(), DateUtils.FORMAT_SHOW_DATE);
			holder.dateTextView.setText(dateString);

			PlayerScore playerScore = gameAndResult.getPlayerScore(playerName);

			UIUtil.setRankingTextView(activity, holder.rankingTextView,
					playerScore.getRanking());
			UIUtil.setPlayerCountTextView(activity, holder.playerCountTextView,
					gameSetting.getPlayerCount());

			int score = playerScore.getOriginalScore();
			UIUtil.setScoreTextView(activity, holder.scoreTextView, score);

			UIUtil.setFeeTextView(activity, holder.feeTextView,
					playerScore.getAdjustedTotalFee());

			return v;
		}
	}
}
