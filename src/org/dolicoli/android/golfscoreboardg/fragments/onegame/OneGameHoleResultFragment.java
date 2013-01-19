package org.dolicoli.android.golfscoreboardg.fragments.onegame;

import java.util.ArrayList;
import java.util.Collections;

import org.dolicoli.android.golfscoreboardg.Constants;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.data.OneGame;
import org.dolicoli.android.golfscoreboardg.data.OneHolePlayerScore;
import org.dolicoli.android.golfscoreboardg.data.PlayerScore;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.dolicoli.android.golfscoreboardg.tasks.CurrentGameQueryTask;
import org.dolicoli.android.golfscoreboardg.tasks.HistoryGameSettingWithResultQueryTask;
import org.dolicoli.android.golfscoreboardg.utils.UIUtil;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.util.SparseArray;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class OneGameHoleResultFragment extends ListFragment implements
		OneGameActivityPage, CurrentGameQueryTask.TaskListener,
		HistoryGameSettingWithResultQueryTask.TaskListener {

	private View gameSettingView;
	private TextView currentHoleTextView, finalHoleTextView;
	private TextView sumOfHoleFeeTextView, totalHoleFeeTextView;
	private TextView sumOfTotalFeeTextView, totalTotalFeeTextView;

	private HoleResultListAdapter adapter;

	private int currentHole;
	private OneGame gameResult;

	private int mode;
	private String playDate;
	private int holeNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mode = MODE_CURRENT;
		this.playDate = "";

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
	public void onPause() {
		super.onPause();

		if (mode == MODE_CURRENT) {
			hideActionMode();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (mode == MODE_CURRENT) {
			inflater.inflate(R.menu.current_game, menu);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity activity = getSupportActivity();
		return activity.onOptionsItemSelected(item);
	}

	public void hideActionMode() {
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	@Override
	public void reload(boolean clean) {
		Activity activity = getSupportActivity();

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
		activity.invalidateOptionsMenu();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public void onCurrentGameQueryStarted() {
	}

	@Override
	public void onCurrentGameQueryFinished(OneGame gameResult) {
		this.gameResult = gameResult;
		reloadUI();
	}

	@Override
	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;

		reload(false);
	}

	private void reloadUI() {
		if (getActivity() == null || gameResult == null)
			return;

		int holeCount = gameResult.getHoleCount();
		int playerCount = gameResult.getPlayerCount();
		int sumOfHoleFee = 0;
		int sumOfTotalFee = 0;
		currentHole = gameResult.getCurrentHole();

		SparseArray<HoleResult> holeResultArray = new SparseArray<HoleResult>();

		ArrayList<PlayerScore> playerScoreList = new ArrayList<PlayerScore>();
		for (int playerId = 0; playerId < playerCount; playerId++) {
			PlayerScore playerScore = gameResult.getPlayerScore(playerId);
			playerScoreList.add(playerId, playerScore);
			sumOfHoleFee += playerScore.getAdjustedHoleFee();
			sumOfTotalFee += playerScore.getAdjustedTotalFee();
		}

		int maxHoleNumber = 0;
		for (Result result : gameResult.getResults()) {
			int holeNumber = result.getHoleNumber();
			int parNumber = result.getParNumber();
			if (holeNumber > maxHoleNumber)
				maxHoleNumber = holeNumber;

			HoleResult holeResult = holeResultArray.get(holeNumber);
			if (holeResult == null) {
				holeResult = new HoleResult();
				holeResultArray.put(holeNumber, holeResult);
			}
			holeResult.holeNumber = holeNumber;
			holeResult.playerCount = playerCount;
			holeResult.parNumber = parNumber;

			for (int playerId = 0; playerId < playerCount; playerId++) {
				OneHolePlayerScore holePlayerScore = gameResult
						.getHolePlayerScore(holeNumber, playerId);
				holeResult.add(new RankingItem(holePlayerScore));
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

		if (sumOfHoleFeeTextView != null) {
			UIUtil.setFeeTextView(getActivity(), sumOfHoleFeeTextView,
					sumOfHoleFee);
		}
		if (sumOfTotalFeeTextView != null) {
			UIUtil.setFeeTextView(getActivity(), sumOfTotalFeeTextView,
					sumOfTotalFee);
		}

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
				if (prevRanking == item.getRanking()) {
					item.displayRanking = false;
				} else {
					item.displayRanking = true;
				}
				prevRanking = item.getRanking();
			}
		}
	}

	private static class RankingItem implements Comparable<RankingItem> {

		private OneHolePlayerScore holePlayerScore;
		private boolean displayRanking;

		public RankingItem(OneHolePlayerScore holePlayerScore) {
			this.holePlayerScore = holePlayerScore;
		}

		@Override
		public int compareTo(RankingItem another) {
			return holePlayerScore.compareTo(another.holePlayerScore);
		}

		public int getRanking() {
			return holePlayerScore.getRanking();
		}

		public String getName() {
			return holePlayerScore.getName();
		}

		public int getOriginalScore() {
			return holePlayerScore.getOriginalScore();
		}

		public int getFinalScore() {
			return holePlayerScore.getFinalScore();
		}

		public int getUsedHandicap() {
			return holePlayerScore.getUsedHandicap();
		}

		public int getFee() {
			return holePlayerScore.getFee();
		}
	}

	private static class HoleResultListViewHolder {
		View[] rankingViews;
		TextView holeNumberTextView;
		TextView[] nameTextViews, rankingTextViews, feeTextViews,
				finalScoreTextViews, scoreTextViews, handicapTextViews;
	}

	private static class HoleResultListAdapter extends ArrayAdapter<HoleResult> {

		private HoleResultListViewHolder holder;
		private int textViewResourceId;

		public HoleResultListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);

			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			Context context = getContext();
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
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

			holder.holeNumberTextView.setText(context.getString(
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

				holder.rankingTextViews[i].setText(UIUtil.formatRanking(
						context, item.getRanking()));
				holder.nameTextViews[i].setText(item.getName());

				UIUtil.setScoreTextView(context, holder.scoreTextViews[i],
						item.getOriginalScore());

				if (item.getUsedHandicap() > 0) {
					holder.handicapTextViews[i].setText(UIUtil.formatHandicap(
							context, item.getUsedHandicap() * -1));
					holder.handicapTextViews[i].setVisibility(View.VISIBLE);
					holder.scoreTextViews[i].setVisibility(View.VISIBLE);
				} else {
					holder.handicapTextViews[i]
							.setText(R.string.fragment_onegameholeresult_no_handicap);
					holder.handicapTextViews[i].setVisibility(View.GONE);
					holder.scoreTextViews[i].setVisibility(View.GONE);
				}

				holder.feeTextViews[i].setText(UIUtil.formatFee(context,
						item.getFee()));

				UIUtil.setScoreTextView(context, holder.finalScoreTextViews[i],
						item.getFinalScore());
			}

			for (int i = playerCount; i < Constants.MAX_PLAYER_COUNT; i++) {
				holder.rankingViews[i].setVisibility(View.GONE);
			}

			return v;
		}
	}
}
