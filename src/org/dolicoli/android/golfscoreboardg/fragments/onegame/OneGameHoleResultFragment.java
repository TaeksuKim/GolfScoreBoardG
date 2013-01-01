package org.dolicoli.android.golfscoreboardg.fragments.onegame;

import java.util.ArrayList;
import java.util.Collections;

import org.dolicoli.android.golfscoreboardg.Constants;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.Reloadable;
import org.dolicoli.android.golfscoreboardg.data.SingleGameResult;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.tasks.CurrentGameQueryTask;
import org.dolicoli.android.golfscoreboardg.tasks.HistoryGameSettingWithResultQueryTask;
import org.dolicoli.android.golfscoreboardg.utils.FeeCalculator;
import org.dolicoli.android.golfscoreboardg.utils.UIUtil;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.ListView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class OneGameHoleResultFragment extends ListFragment implements
		Reloadable, OneGameActivityPage, CurrentGameQueryTask.TaskListener,
		HistoryGameSettingWithResultQueryTask.TaskListener {

	private View gameSettingView;
	private TextView currentHoleTextView, finalHoleTextView;
	private TextView sumOfHoleFeeTextView, totalHoleFeeTextView;
	private TextView sumOfTotalFeeTextView, totalTotalFeeTextView;

	private HoleResultListAdapter adapter;

	private int currentHole;
	private SingleGameResult gameResult;

	private int mode;
	private String playDate;
	private int holeNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mode = MODE_CURRENT;
		this.playDate = "";
		this.holeNumber = 0;

		Bundle bundle = null;
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		} else {
			bundle = getArguments();
		}
		if (bundle != null) {
			int mode = bundle.getInt(BK_MODE);
			if (mode == MODE_CURRENT || mode == MODE_HISTORY)
				this.mode = mode;

			String playDate = bundle.getString(BK_PLAY_DATE);
			if (playDate != null && !playDate.equals("")) {
				this.playDate = playDate;
			}

			int holeNumber = bundle.getInt(BK_HOLE_NUMBER);
			if (holeNumber > 0 && holeNumber <= 18)
				this.holeNumber = holeNumber;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.onegame_hole_result_fragment,
				null);

		gameSettingView = view.findViewById(R.id.GameSettingView);
		sumOfHoleFeeTextView = (TextView) view
				.findViewById(R.id.SumOfHoleFeeTextView);
		totalHoleFeeTextView = (TextView) view
				.findViewById(R.id.TotalHoleFeeTextView);

		sumOfTotalFeeTextView = (TextView) view
				.findViewById(R.id.SumOfTotalFeeTextView);
		totalTotalFeeTextView = (TextView) view
				.findViewById(R.id.TotalTotalFeeTextView);

		currentHoleTextView = (TextView) view
				.findViewById(R.id.CurrentHoleTextView);
		finalHoleTextView = (TextView) view
				.findViewById(R.id.FinalHoleTextView);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new HoleResultListAdapter(getActivity(),
				R.layout.onegame_hole_result_list_item);
		adapter.setNotifyOnChange(false);
		setListAdapter(adapter);

		if (mode == MODE_CURRENT) {
			gameSettingView.setVisibility(View.VISIBLE);

			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			setHasOptionsMenu(true);
		} else {
			gameSettingView.setVisibility(View.GONE);
		}
		reload(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (mode == MODE_CURRENT) {
			inflater.inflate(R.menu.current_game, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity activity = getSupportActivity();
		if (activity == null)
			return false;
		return activity.onOptionsItemSelected(item);
	}

	@Override
	public void reload(boolean clean) {
		FragmentActivity activity = getActivity();

		if (activity == null)
			return;

		if (mode == MODE_CURRENT) {
			CurrentGameQueryTask task = new CurrentGameQueryTask(activity, this);
			task.execute();
		} else if (mode == MODE_HISTORY) {
			HistoryGameSettingWithResultQueryTask task = new HistoryGameSettingWithResultQueryTask(
					getActivity(), this);
			task.execute(new HistoryGameSettingWithResultQueryTask.QueryParam(
					playDate, holeNumber));
		}
	}

	@Override
	public void onCurrentGameQueryStarted() {
	}

	@Override
	public void onCurrentGameQueryFinished(SingleGameResult gameResult) {
		this.gameResult = gameResult;
		reloadUI();
	}

	@Override
	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}

	private void reloadUI() {
		if (getActivity() == null || gameResult == null)
			return;

		int holeCount = gameResult.getHoleCount();
		int playerCount = gameResult.getPlayerCount();

		SparseArray<HoleResult> holeResultArray = new SparseArray<HoleResult>();
		ArrayList<PlayerScore> playerScoreList = new ArrayList<PlayerScore>();
		for (int playerId = 0; playerId < playerCount; playerId++) {
			PlayerScore playerScore = new PlayerScore(playerId);
			playerScoreList.add(playerId, playerScore);
		}

		int maxHoleNumber = 0;
		currentHole = 0;
		for (Result result : gameResult.getResults()) {
			if (currentHole < result.getHoleNumber())
				currentHole = result.getHoleNumber();

			int holeNumber = result.getHoleNumber();
			int parNumber = result.getParNumber();
			if (holeNumber > maxHoleNumber)
				maxHoleNumber = holeNumber;
			HoleResult holeResult = holeResultArray.get(holeNumber, null);
			if (holeResult == null) {
				holeResult = new HoleResult();
				holeResultArray.put(holeNumber, holeResult);
			}
			holeResult.holeNumber = holeNumber;
			holeResult.playerCount = playerCount;
			holeResult.parNumber = parNumber;

			int[] fees = FeeCalculator.calculateFee(gameResult, result);

			for (int playerId = 0; playerId < playerCount; playerId++) {
				int holeFee = fees[playerId];

				int ranking = result.getRanking(playerId);
				RankingItem item = new RankingItem(playerId,
						gameResult.getPlayerName(playerId), ranking,
						result.getFinalScore(playerId),
						result.getOriginalScore(playerId),
						result.getUsedHandicap(playerId));

				item.fee = holeFee;

				PlayerScore playerScore = playerScoreList.get(playerId);
				playerScore.originalHoleFee += fees[playerId];
				playerScore.adjustedHoleFee = playerScore.originalHoleFee;

				holeResult.add(item);
			}

			holeResult.sort();
		}

		finalHoleTextView.setText(getString(
				R.string.fragment_onegameholeresult_final_hole_format,
				holeCount));
		if (currentHole < 1) {
			currentHoleTextView
					.setText(R.string.fragment_onegameholeresult_no_hole_number);
		} else {
			currentHoleTextView.setText(getString(
					R.string.fragment_onegameholeresult_current_hole_format,
					currentHole));
		}
		adjustFee(playerScoreList, gameResult,
				currentHole >= gameResult.getHoleCount());

		UIUtil.setFeeTextView(getActivity(), totalHoleFeeTextView,
				gameResult.getHoleFee());
		UIUtil.setFeeTextView(getActivity(), totalTotalFeeTextView,
				gameResult.getTotalFee());

		adapter.clear();
		for (int i = maxHoleNumber; i >= 1; i--) {
			adapter.add(holeResultArray.get(i));
		}
		adapter.notifyDataSetChanged();
	}

	private void adjustFee(ArrayList<PlayerScore> playerScoreList,
			SingleGameResult result, boolean isGameFinished) {
		int sumOfHoleFee = 0;
		int sumOfRankingFee = 0;
		for (PlayerScore playerScore : playerScoreList) {
			sumOfHoleFee += playerScore.originalHoleFee;
			sumOfRankingFee += playerScore.originalRankingFee;
		}

		int rankingFee = result.getRankingFee();
		int holeFee = result.getHoleFee();

		int remainOfHoleFee = 0;
		if (sumOfHoleFee < holeFee && isGameFinished) {
			int additionalHoleFeePerPlayer = FeeCalculator.ceil(holeFee
					- sumOfHoleFee, result.getPlayerCount());

			int sum = 0;
			for (PlayerScore playerScore : playerScoreList) {
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
					- sumOfRankingFee, result.getPlayerCount());

			int sum = 0;
			for (PlayerScore playerScore : playerScoreList) {
				playerScore.adjustedRankingFee += additionalRankingFeePerPlayer;
				sum += playerScore.adjustedRankingFee;
			}
			remainOfRankingFee = sum - rankingFee;
		} else if (sumOfRankingFee > rankingFee) {
			remainOfRankingFee = sumOfRankingFee - rankingFee;
		}

		PlayerScore lastSinglePlayerScore = null;
		for (PlayerScore playerScore : playerScoreList) {
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
		for (PlayerScore playerScore : playerScoreList) {
			sumOfHoleFee += playerScore.adjustedHoleFee;
			sumOfRankingFee += playerScore.adjustedRankingFee;

			playerScore.adjustedTotalFee = playerScore.adjustedHoleFee
					+ playerScore.adjustedRankingFee;
		}

		if (sumOfHoleFeeTextView != null) {
			UIUtil.setFeeTextView(getActivity(), sumOfHoleFeeTextView,
					sumOfHoleFee);
		}

		if (sumOfTotalFeeTextView != null) {
			UIUtil.setFeeTextView(getActivity(), sumOfTotalFeeTextView,
					sumOfHoleFee + sumOfRankingFee);
		}
	}

	private static class HoleResult {
		private int holeNumber;
		private int playerCount;
		private int parNumber;

		private ArrayList<RankingItem> items;

		public HoleResult() {
			items = new ArrayList<RankingItem>();
		}

		public void add(RankingItem rankingItem) {
			items.add(rankingItem);
		}

		public RankingItem get(int i) {
			return items.get(i);
		}

		public void sort() {
			Collections.sort(items);

			int prevRanking = 0;
			for (RankingItem item : items) {
				if (prevRanking == item.ranking) {
					item.displayRanking = false;
				} else {
					item.displayRanking = true;
				}
				prevRanking = item.ranking;
			}
		}
	}

	private static class RankingItem implements Comparable<RankingItem> {

		private boolean displayRanking;
		private int playerId;
		private String name;
		private int ranking;
		private int finalScore;
		private int score;
		private int fee;
		private int handicap;

		public RankingItem(int playerId, String name, int ranking,
				int finalScore, int score, int handicap) {
			this.displayRanking = true;
			this.playerId = playerId;
			this.name = name;
			this.ranking = ranking;
			this.handicap = handicap;
			this.finalScore = finalScore;
			this.score = score;
			this.fee = 0;
		}

		@Override
		public int compareTo(RankingItem another) {
			if (ranking < another.ranking)
				return -1;
			if (ranking > another.ranking)
				return 1;
			if (playerId < another.playerId)
				return -1;
			if (playerId > another.playerId)
				return 1;
			return 0;
		}
	}

	private static class PlayerScore implements Comparable<PlayerScore> {

		private int playerId;
		private int ranking;
		private int sameRankingCount;
		private int score;

		private int originalHoleFee, adjustedHoleFee;
		private int originalRankingFee, adjustedRankingFee;
		private int adjustedTotalFee;

		public PlayerScore(int playerId) {
			this.playerId = playerId;

			this.ranking = 0;
			this.score = 0;

			this.originalHoleFee = 0;
			this.originalRankingFee = 0;
			this.adjustedHoleFee = 0;
			this.adjustedRankingFee = 0;
			this.adjustedTotalFee = 0;
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

	private static class HoleResultListViewHolder {
		View[] rankingViews;
		TextView holeNumberTextView;
		TextView[] nameTextViews, rankingTextViews, feeTextViews,
				finalScoreTextViews, scoreTextViews, handicapTextViews;
	}

	private class HoleResultListAdapter extends ArrayAdapter<HoleResult> {

		private HoleResultListViewHolder holder;
		private int textViewResourceId;

		public HoleResultListAdapter(Context context, int textViewResourceId) {
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
				holder = new HoleResultListViewHolder();

				holder.rankingViews = new View[6];
				holder.rankingViews[0] = v.findViewById(R.id.Ranking1Panel);
				holder.rankingViews[1] = v.findViewById(R.id.Ranking2Panel);
				holder.rankingViews[2] = v.findViewById(R.id.Ranking3Panel);
				holder.rankingViews[3] = v.findViewById(R.id.Ranking4Panel);
				holder.rankingViews[4] = v.findViewById(R.id.Ranking5Panel);
				holder.rankingViews[5] = v.findViewById(R.id.Ranking6Panel);

				holder.holeNumberTextView = (TextView) v
						.findViewById(R.id.HoleNumberTextView);

				holder.rankingTextViews = new TextView[6];
				holder.rankingTextViews[0] = (TextView) v
						.findViewById(R.id.Ranking1TextView);
				holder.rankingTextViews[1] = (TextView) v
						.findViewById(R.id.Ranking2TextView);
				holder.rankingTextViews[2] = (TextView) v
						.findViewById(R.id.Ranking3TextView);
				holder.rankingTextViews[3] = (TextView) v
						.findViewById(R.id.Ranking4TextView);
				holder.rankingTextViews[4] = (TextView) v
						.findViewById(R.id.Ranking5TextView);
				holder.rankingTextViews[5] = (TextView) v
						.findViewById(R.id.Ranking6TextView);

				holder.nameTextViews = new TextView[6];
				holder.nameTextViews[0] = (TextView) v
						.findViewById(R.id.Name1TextView);
				holder.nameTextViews[1] = (TextView) v
						.findViewById(R.id.Name2TextView);
				holder.nameTextViews[2] = (TextView) v
						.findViewById(R.id.Name3TextView);
				holder.nameTextViews[3] = (TextView) v
						.findViewById(R.id.Name4TextView);
				holder.nameTextViews[4] = (TextView) v
						.findViewById(R.id.Name5TextView);
				holder.nameTextViews[5] = (TextView) v
						.findViewById(R.id.Name6TextView);

				holder.feeTextViews = new TextView[6];
				holder.feeTextViews[0] = (TextView) v
						.findViewById(R.id.Fee1TextView);
				holder.feeTextViews[1] = (TextView) v
						.findViewById(R.id.Fee2TextView);
				holder.feeTextViews[2] = (TextView) v
						.findViewById(R.id.Fee3TextView);
				holder.feeTextViews[3] = (TextView) v
						.findViewById(R.id.Fee4TextView);
				holder.feeTextViews[4] = (TextView) v
						.findViewById(R.id.Fee5TextView);
				holder.feeTextViews[5] = (TextView) v
						.findViewById(R.id.Fee6TextView);

				holder.finalScoreTextViews = new TextView[6];
				holder.finalScoreTextViews[0] = (TextView) v
						.findViewById(R.id.FinalScore1TextView);
				holder.finalScoreTextViews[1] = (TextView) v
						.findViewById(R.id.FinalScore2TextView);
				holder.finalScoreTextViews[2] = (TextView) v
						.findViewById(R.id.FinalScore3TextView);
				holder.finalScoreTextViews[3] = (TextView) v
						.findViewById(R.id.FinalScore4TextView);
				holder.finalScoreTextViews[4] = (TextView) v
						.findViewById(R.id.FinalScore5TextView);
				holder.finalScoreTextViews[5] = (TextView) v
						.findViewById(R.id.FinalScore6TextView);

				holder.scoreTextViews = new TextView[6];
				holder.scoreTextViews[0] = (TextView) v
						.findViewById(R.id.Score1TextView);
				holder.scoreTextViews[1] = (TextView) v
						.findViewById(R.id.Score2TextView);
				holder.scoreTextViews[2] = (TextView) v
						.findViewById(R.id.Score3TextView);
				holder.scoreTextViews[3] = (TextView) v
						.findViewById(R.id.Score4TextView);
				holder.scoreTextViews[4] = (TextView) v
						.findViewById(R.id.Score5TextView);
				holder.scoreTextViews[5] = (TextView) v
						.findViewById(R.id.Score6TextView);

				holder.handicapTextViews = new TextView[6];
				holder.handicapTextViews[0] = (TextView) v
						.findViewById(R.id.Handicap1TextView);
				holder.handicapTextViews[1] = (TextView) v
						.findViewById(R.id.Handicap2TextView);
				holder.handicapTextViews[2] = (TextView) v
						.findViewById(R.id.Handicap3TextView);
				holder.handicapTextViews[3] = (TextView) v
						.findViewById(R.id.Handicap4TextView);
				holder.handicapTextViews[4] = (TextView) v
						.findViewById(R.id.Handicap5TextView);
				holder.handicapTextViews[5] = (TextView) v
						.findViewById(R.id.Handicap6TextView);

				v.setTag(holder);
			} else {
				holder = (HoleResultListViewHolder) v.getTag();
			}

			int count = getCount();
			if (count < 1)
				return v;

			if (position < 0 || position > count - 1)
				return v;

			HoleResult holeResult = getItem(position);
			if (holeResult == null)
				return v;

			holder.holeNumberTextView.setText(getString(
					R.string.fragment_onegameholeresult_hole_number_format,
					holeResult.holeNumber, holeResult.parNumber));

			int playerCount = holeResult.playerCount;
			for (int i = 0; i < playerCount; i++) {
				RankingItem item = holeResult.get(i);
				holder.rankingViews[i].setVisibility(View.VISIBLE);

				if (item.displayRanking)
					holder.rankingTextViews[i].setVisibility(View.VISIBLE);
				else
					holder.rankingTextViews[i].setVisibility(View.INVISIBLE);

				Context context = getContext();

				holder.rankingTextViews[i].setText(UIUtil.formatRanking(
						context, item.ranking));
				holder.nameTextViews[i].setText(item.name);

				UIUtil.setScoreTextView(context, holder.scoreTextViews[i],
						item.score);

				if (item.handicap > 0) {
					UIUtil.setHandicapTextView(context,
							holder.handicapTextViews[i], item.handicap);
					holder.handicapTextViews[i].setVisibility(View.VISIBLE);
					holder.scoreTextViews[i].setVisibility(View.VISIBLE);
				} else {
					holder.handicapTextViews[i]
							.setText(R.string.fragment_onegameholeresult_no_handicap);
					holder.handicapTextViews[i].setVisibility(View.GONE);
					holder.scoreTextViews[i].setVisibility(View.GONE);
				}

				holder.feeTextViews[i].setText(UIUtil.formatFee(context,
						item.fee));

				UIUtil.setScoreTextView(context, holder.finalScoreTextViews[i],
						item.finalScore);
			}

			for (int i = playerCount; i < Constants.MAX_PLAYER_COUNT; i++) {
				holder.rankingViews[i].setVisibility(View.GONE);
			}

			return v;
		}
	}
}
