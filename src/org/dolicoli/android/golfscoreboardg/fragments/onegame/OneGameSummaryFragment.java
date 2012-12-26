package org.dolicoli.android.golfscoreboardg.fragments.onegame;

import java.util.ArrayList;
import java.util.Collections;

import org.dolicoli.android.golfscoreboardg.OneGameActivity;
import org.dolicoli.android.golfscoreboardg.OneGamePlayerRecordActivity;
import org.dolicoli.android.golfscoreboardg.PersonalStatisticsActivity;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.data.SingleGameResult;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.tasks.HistoryQueryTask;
import org.dolicoli.android.golfscoreboardg.utils.FeeCalculator;
import org.dolicoli.android.golfscoreboardg.utils.PlayerUIUtil;
import org.dolicoli.android.golfscoreboardg.utils.UIUtil;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.util.SparseArray;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class OneGameSummaryFragment extends ListFragment implements
		OneGameActivityPage, HistoryQueryTask.TaskListener {

	private ScoreListAdapter adapter;

	private int holeCount;
	private int currentHole;

	private String playDate;

	private int holeNumber;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.onegame_summary_fragment, null);

		adapter = new ScoreListAdapter(getActivity(),
				R.layout.onegame_summary_list_item);
		adapter.setNotifyOnChange(false);
		setListAdapter(adapter);

		Intent intent = getActivity().getIntent();
		playDate = intent.getStringExtra(OneGameActivity.IK_PLAY_DATE);

		holeNumber = 0;

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		reload();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		PlayerScore playerScore = adapter.getItem(position);
		int playerId = playerScore.playerId;
		Intent intent = new Intent(getActivity(),
				OneGamePlayerRecordActivity.class);
		intent.putExtra(OneGamePlayerRecordActivity.IK_PLAYER_ID, playerId);
		intent.putExtra(OneGamePlayerRecordActivity.IK_PLAY_DATE, playDate);
		startActivity(intent);
	}

	public boolean isAllGameFinished() {
		return (currentHole >= holeCount);
	}

	@Override
	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
		reload();
	}

	@Override
	public void onGameQueryStarted() {
	}

	@Override
	public void onGameQueryFinished(SingleGameResult gameResult) {
		reloadUI(gameResult);
	}

	public void reload() {
		if (getActivity() == null || playDate == null)
			return;

		HistoryQueryTask task = new HistoryQueryTask(getActivity(), this);
		task.execute(new HistoryQueryTask.QueryParam(playDate, holeNumber));
	}

	private void adjustFee(ArrayList<PlayerScore> list,
			SingleGameResult gameResult, boolean isGameFinished) {
		int sumOfHoleFee = 0;
		int sumOfRankingFee = 0;
		for (PlayerScore playerScore : list) {
			sumOfHoleFee += playerScore.originalHoleFee;
			sumOfRankingFee += playerScore.originalRankingFee;

			playerScore.originalTotalFee = playerScore.originalHoleFee
					+ playerScore.originalRankingFee;
		}

		int rankingFee = gameResult.getRankingFee();
		int holeFee = gameResult.getHoleFee();

		int remainOfHoleFee = 0;
		if (sumOfHoleFee < holeFee && isGameFinished) {
			int additionalHoleFeePerPlayer = FeeCalculator.ceil(holeFee
					- sumOfHoleFee, gameResult.getPlayerCount());

			int sum = 0;
			for (PlayerScore playerScore : list) {
				playerScore.adjustedHoleFee += additionalHoleFeePerPlayer;
				sum += playerScore.adjustedHoleFee;
			}
			remainOfHoleFee = sum - holeFee;
		} else if (sumOfHoleFee > holeFee) {
			remainOfHoleFee = sumOfHoleFee - holeFee;
		}

		int remainOfRankingFee = 0;
		if (sumOfRankingFee < rankingFee) {
			int additionalRankingFeePerPlayer = FeeCalculator.ceil(rankingFee
					- sumOfRankingFee, gameResult.getPlayerCount());

			int sum = 0;
			for (PlayerScore playerScore : list) {
				playerScore.adjustedRankingFee += additionalRankingFeePerPlayer;
				sum += playerScore.adjustedRankingFee;
			}
			remainOfRankingFee = sum - rankingFee;
		} else if (sumOfRankingFee > rankingFee) {
			remainOfRankingFee = sumOfRankingFee - rankingFee;
		}

		PlayerScore lastSinglePlayerScore = null;
		for (PlayerScore playerScore : list) {
			if (playerScore.sameRankingCount <= 1) {
				lastSinglePlayerScore = playerScore;
			}
		}
		if (remainOfHoleFee > 0 || remainOfRankingFee > 0) {
			if (lastSinglePlayerScore != null) {
				lastSinglePlayerScore.adjustedRankingFee -= (remainOfHoleFee + remainOfRankingFee);
			} else {
				// �� ��� ��� �ؾ� �ұ��?
			}
		}

		sumOfHoleFee = 0;
		sumOfRankingFee = 0;
		for (PlayerScore playerScore : list) {
			sumOfHoleFee += playerScore.adjustedHoleFee;
			sumOfRankingFee += playerScore.adjustedRankingFee;

			playerScore.adjustedTotalFee = playerScore.adjustedHoleFee
					+ playerScore.adjustedRankingFee;
		}
	}

	private void calculateRankingFee(ArrayList<PlayerScore> list,
			SingleGameResult gameResult) {
		for (PlayerScore playerScore : list) {
			int sum = 0;
			int sameRankingCount = playerScore.sameRankingCount;

			for (int i = 0; i < sameRankingCount; i++) {
				sum += gameResult.getRankingFeeForRanking(playerScore.ranking
						+ i);
			}
			playerScore.originalRankingFee = FeeCalculator.ceil(sum,
					sameRankingCount);
			playerScore.adjustedRankingFee = playerScore.originalRankingFee;
		}
	}

	private void calculateRanking(ArrayList<PlayerScore> list) {
		PlayerScore prevPlayerScore = null;
		int ranking = 1;
		for (PlayerScore playerScore : list) {
			if (prevPlayerScore != null
					&& prevPlayerScore.score == playerScore.score) {
				playerScore.ranking = prevPlayerScore.ranking;
			} else {
				playerScore.ranking = ranking;
			}
			ranking++;
			prevPlayerScore = playerScore;
		}

		int size = list.size();
		for (int i = 0; i < size; i++) {
			PlayerScore playerScore = list.get(i);
			playerScore.sameRankingCount = 0;
			for (int j = 0; j < size; j++) {
				PlayerScore compare = list.get(j);
				if (compare.ranking == playerScore.ranking) {
					playerScore.sameRankingCount++;
				}
			}
		}
	}

	private void calculateAvgRanking(ArrayList<PlayerScore> list) {
		for (PlayerScore playerScore : list) {
			if (currentHole < 1) {
				playerScore.avgRanking = 0F;
			} else {
				playerScore.avgRanking = ((float) playerScore.holeRankingSum / (float) currentHole);
			}
		}
	}

	private void calculateOverPar(ArrayList<PlayerScore> list) {
		for (PlayerScore playerScore : list) {
			if (currentHole < 1) {
				playerScore.avgOverPar = 0F;
			} else {
				playerScore.avgOverPar = ((float) (playerScore.score + playerScore.extraScore) / (float) currentHole);
			}
		}
	}

	private void reloadUI(SingleGameResult gameResult) {
		Activity activity = getSupportActivity();
		if (activity == null || gameResult == null)
			return;

		holeCount = gameResult.getHoleCount();

		int playerCount = gameResult.getPlayerCount();
		SparseArray<PlayerScore> playerScoreMap = new SparseArray<PlayerScore>();

		ArrayList<PlayerScore> list = new ArrayList<PlayerScore>();
		for (int playerId = 0; playerId < playerCount; playerId++) {
			PlayerScore playerScore = new PlayerScore(playerId,
					gameResult.getPlayerName(playerId),
					gameResult.getHandicap(playerId),
					gameResult.getHandicap(playerId)
							- gameResult.getUsedHandicap(playerId),
					gameResult.getExtraScore(playerId));
			playerScoreMap.put(playerId, playerScore);
			list.add(playerId, playerScore);
		}

		currentHole = 0;
		for (Result result : gameResult.getResults()) {
			if (currentHole < result.getHoleNumber())
				currentHole = result.getHoleNumber();

			int[] fees = FeeCalculator.calculateFee(gameResult, result);

			for (int playerId = 0; playerId < playerCount; playerId++) {
				PlayerScore playerScore = list.get(playerId);

				playerScore.score += result.getFinalScore(playerId);
				playerScore.originalHoleFee += fees[playerId];
				playerScore.adjustedHoleFee = playerScore.originalHoleFee;

				playerScore.addHoleRanking(result.getRanking(playerId));
			}
		}

		for (int playerId = 0; playerId < playerCount; playerId++) {
			PlayerScore playerScore = list.get(playerId);
			playerScore.extraScore = gameResult.getExtraScore(playerId);
			playerScore.originalScore = playerScore.score;
			playerScore.score -= playerScore.extraScore;
		}

		Collections.sort(list);
		calculateRanking(list);
		calculateRankingFee(list, gameResult);
		calculateAvgRanking(list);
		calculateOverPar(list);

		adjustFee(list, gameResult, currentHole >= gameResult.getHoleCount());
		Collections.sort(list); // Sort again after adjustFee has been set

		adapter.clear();
		for (PlayerScore playerScore : list) {
			adapter.add(playerScore);
		}
		adapter.notifyDataSetChanged();
	}

	private static class PlayerScore implements Comparable<PlayerScore> {

		private int playerId;
		private String name;
		private int ranking;
		private int sameRankingCount;
		private int handicap, remainHandicap, usedHandicap;

		private int originalScore;
		private int score;
		private int extraScore;

		private int holeRankingSum;
		private float avgRanking;
		private float avgOverPar;

		private int originalHoleFee, adjustedHoleFee;
		private int originalRankingFee, adjustedRankingFee;
		@SuppressWarnings("unused")
		private int originalTotalFee, adjustedTotalFee;

		public PlayerScore(int playerId, String name, int handicap,
				int remainHandicap, int extraScore) {
			this.playerId = playerId;
			this.name = name;
			this.handicap = handicap;
			this.remainHandicap = remainHandicap;
			this.usedHandicap = handicap - remainHandicap;
			this.extraScore = extraScore;

			this.ranking = 0;
			this.score = 0;

			this.avgRanking = 0.0F;

			this.originalHoleFee = 0;
			this.originalRankingFee = 0;
			this.originalTotalFee = 0;
			this.adjustedHoleFee = 0;
			this.adjustedRankingFee = 0;
			this.adjustedTotalFee = 0;
			this.holeRankingSum = 0;
		}

		public void addHoleRanking(int ranking) {
			this.holeRankingSum += ranking;
		}

		@Override
		public int compareTo(PlayerScore compare) {
			if (ranking < compare.ranking)
				return -1;
			if (ranking > compare.ranking)
				return 1;
			if (score < compare.score)
				return -1;
			if (score > compare.score)
				return 1;
			if (originalScore < compare.originalScore)
				return -1;
			if (originalScore > compare.originalScore)
				return 1;
			if (adjustedTotalFee < compare.adjustedTotalFee)
				return -1;
			if (adjustedTotalFee > compare.adjustedTotalFee)
				return 1;
			if (playerId < compare.playerId)
				return -1;
			if (playerId > compare.playerId)
				return 1;
			return 0;
		}
	}

	private static class ScoreListViewHolder {
		TextView largeNameTextView, largeTotalFeeTextView, largeScoreTextView;
		TextView mediumNameTextView, mediumTotalFeeTextView,
				mediumScoreTextView;
		TextView totalOriginalScoreTextView, totalHandicapTextView,
				totalExtraScoreTextView;
		TextView holeFeeTextView, rankingFeeTextView;
		View handicapView;
		TextView handicapTextView, remainHandicapTextView;
		TextView avgRankingTextView;
		TextView avgOverParTextView;

		ImageView largePlayerImageView, mediumPlayerImageView;
		Button largeShowMoreButton, mediumShowMoreButton;
		Button personalStatisticsButton;

		View largeTagView, mediumTagView, largeView, mediumView, detailView;
	}

	private class ScoreListAdapter extends ArrayAdapter<PlayerScore> implements
			OnClickListener {

		private ScoreListViewHolder holder;
		private int textViewResourceId;

		public ScoreListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
				holder = new ScoreListViewHolder();

				holder.largeTagView = v.findViewById(R.id.LargeTagView);
				holder.largeView = v.findViewById(R.id.LargeView);
				holder.largePlayerImageView = (ImageView) v
						.findViewById(R.id.LargeImage);
				holder.largeNameTextView = (TextView) v
						.findViewById(R.id.LargeNameTextView);
				holder.largeTotalFeeTextView = (TextView) v
						.findViewById(R.id.LargeTotalFeeTextView);
				holder.largeScoreTextView = (TextView) v
						.findViewById(R.id.LargeScoreTextView);
				holder.largeShowMoreButton = (Button) v
						.findViewById(R.id.LargeShowMoreButton);

				holder.mediumTagView = v.findViewById(R.id.MediumTagView);
				holder.mediumView = v.findViewById(R.id.MediumView);
				holder.mediumPlayerImageView = (ImageView) v
						.findViewById(R.id.MediumImage);
				holder.mediumNameTextView = (TextView) v
						.findViewById(R.id.MediumNameTextView);
				holder.mediumTotalFeeTextView = (TextView) v
						.findViewById(R.id.MediumTotalFeeTextView);
				holder.mediumScoreTextView = (TextView) v
						.findViewById(R.id.MediumScoreTextView);
				holder.mediumShowMoreButton = (Button) v
						.findViewById(R.id.MediumShowMoreButton);

				holder.totalOriginalScoreTextView = (TextView) v
						.findViewById(R.id.TotalOriginalScoreTextView);
				holder.totalHandicapTextView = (TextView) v
						.findViewById(R.id.TotalHandicapTextView);
				holder.totalExtraScoreTextView = (TextView) v
						.findViewById(R.id.TotalExtraScoreTextView);

				holder.holeFeeTextView = (TextView) v
						.findViewById(R.id.HoleFeeTextView);
				holder.rankingFeeTextView = (TextView) v
						.findViewById(R.id.RankingFeeTextView);

				holder.avgRankingTextView = (TextView) v
						.findViewById(R.id.AvgRankingTextView);
				holder.avgOverParTextView = (TextView) v
						.findViewById(R.id.AvgOverParTextView);
				holder.handicapView = v.findViewById(R.id.HandicapView);
				holder.handicapTextView = (TextView) v
						.findViewById(R.id.HandicapTextView);
				holder.remainHandicapTextView = (TextView) v
						.findViewById(R.id.RemainHandicapTextView);

				holder.detailView = v.findViewById(R.id.DetailView);

				holder.personalStatisticsButton = (Button) v
						.findViewById(R.id.PersonalStatisticsButton);

				v.setTag(holder);
			} else {
				holder = (ScoreListViewHolder) v.getTag();
			}

			int count = getCount();
			if (count < 1)
				return v;

			if (position < 0 || position > count - 1)
				return v;

			PlayerScore playerScore = getItem(position);
			if (playerScore == null)
				return v;

			Context context = getContext();

			Button showMoreButton;
			ImageView playerImageView;
			TextView nameTextView, totalFeeTextView, scoreTextView;
			View tagView;
			if (playerScore.ranking <= 1) {
				tagView = holder.largeTagView;

				playerImageView = holder.largePlayerImageView;
				showMoreButton = holder.largeShowMoreButton;
				nameTextView = holder.largeNameTextView;
				totalFeeTextView = holder.largeTotalFeeTextView;
				scoreTextView = holder.largeScoreTextView;

				holder.largeView.setVisibility(View.VISIBLE);
				holder.mediumView.setVisibility(View.GONE);
			} else {
				tagView = holder.mediumTagView;

				playerImageView = holder.mediumPlayerImageView;
				showMoreButton = holder.mediumShowMoreButton;
				nameTextView = holder.mediumNameTextView;
				totalFeeTextView = holder.mediumTotalFeeTextView;
				scoreTextView = holder.mediumScoreTextView;

				holder.largeView.setVisibility(View.GONE);
				holder.mediumView.setVisibility(View.VISIBLE);
			}

			tagView.setBackgroundColor(PlayerUIUtil
					.getTagColor(playerScore.name));

			showMoreButton.setOnClickListener(this);
			showMoreButton.setTag(holder.detailView);

			holder.personalStatisticsButton.setOnClickListener(this);
			holder.personalStatisticsButton.setTag(playerScore.name);

			playerImageView.setImageResource(PlayerUIUtil
					.getRoundResourceId(playerScore.name));
			nameTextView.setText(playerScore.name);

			totalFeeTextView.setText(UIUtil.formatFee(context,
					playerScore.adjustedTotalFee));
			holder.holeFeeTextView.setText(UIUtil.formatFee(context,
					playerScore.adjustedHoleFee));
			holder.rankingFeeTextView
					.setText(UIUtil.formatFee(context,
							playerScore.adjustedTotalFee
									- playerScore.adjustedHoleFee));

			UIUtil.setScoreTextView(context, scoreTextView, playerScore.score);

			if (playerScore.handicap > 0) {
				holder.handicapTextView.setText(String
						.valueOf(playerScore.handicap));
				holder.remainHandicapTextView.setText(String
						.valueOf(playerScore.remainHandicap));

				holder.handicapView.setVisibility(View.VISIBLE);
			} else {
				holder.handicapView.setVisibility(View.GONE);
			}

			UIUtil.setScoreTextView(getContext(),
					holder.totalOriginalScoreTextView,
					playerScore.originalScore + playerScore.usedHandicap);
			holder.totalHandicapTextView.setText(String
					.valueOf(playerScore.usedHandicap));
			holder.totalExtraScoreTextView.setText(String
					.valueOf(playerScore.extraScore));

			UIUtil.setAvgScoreTextView(context, holder.avgOverParTextView,
					playerScore.avgOverPar);

			if (playerScore.avgRanking > 0.0) {
				holder.avgRankingTextView.setText(UIUtil.formatAvgRanking(
						context, playerScore.avgRanking));
				holder.avgRankingTextView.setVisibility(View.VISIBLE);
			} else {
				holder.avgRankingTextView.setVisibility(View.INVISIBLE);
			}

			return v;
		}

		@Override
		public void onClick(View v) {
			if (!(v instanceof Button))
				return;

			final int id = v.getId();
			if (id == R.id.PersonalStatisticsButton) {
				String playerName = (String) v.getTag();

				Intent intent = new Intent(getActivity(),
						PersonalStatisticsActivity.class);
				intent.putExtra(PersonalStatisticsActivity.IK_PLAYER_NAME,
						PlayerUIUtil.toCanonicalName(playerName));
				startActivity(intent);
			} else {
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
	}
}