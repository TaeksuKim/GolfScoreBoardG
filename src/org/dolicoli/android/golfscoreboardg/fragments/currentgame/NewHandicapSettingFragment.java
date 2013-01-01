package org.dolicoli.android.golfscoreboardg.fragments.currentgame;

import java.util.ArrayList;
import java.util.Collections;

import org.dolicoli.android.golfscoreboardg.Constants;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.data.GameAndResult;
import org.dolicoli.android.golfscoreboardg.tasks.GameAndResultTask;
import org.dolicoli.android.golfscoreboardg.tasks.GameAndResultTask.GameAndResultTaskListener;
import org.dolicoli.android.golfscoreboardg.utils.DateRangeUtil;
import org.dolicoli.android.golfscoreboardg.utils.DateRangeUtil.DateRange;
import org.dolicoli.android.golfscoreboardg.utils.PlayerUIUtil;
import org.dolicoli.android.golfscoreboardg.utils.UIUtil;
import org.dolicoli.android.golfscoreboardg.utils.handicaps.Ecco1Calculator;
import org.dolicoli.android.golfscoreboardg.utils.handicaps.HandicapCalculator;
import org.dolicoli.android.golfscoreboardg.utils.handicaps.HandicapCalculator.GameResultItem;
import org.dolicoli.android.golfscoreboardg.utils.handicaps.LaterBetterCalculator;
import org.dolicoli.android.golfscoreboardg.utils.handicaps.SimpleHandicapCalculator;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

public class NewHandicapSettingFragment extends Fragment implements
		GameAndResultTaskListener, OnItemSelectedListener {

	private ProgressBar progressBar;
	private Spinner handicapCalculatorSpinner;
	private View[] playerViews;

	private TextView[] playerNameTextViews;
	private TextView[] playerGameCountTexts;
	private TextView[] playerAvgScoreTexts;
	private TextView[] playerRecommendHandicapTextViews;
	private Spinner[] playerHandicapSpinners, playerExtraScoreSpinners;

	private int playerCount;
	private String[] playerNames, canonicalPlayerNames;
	private ArrayList<GameAndResult> gameAndResults;
	private HandicapCalculator[] calculators;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Activity activity = getSupportActivity();
		View view = inflater.inflate(
				R.layout.current_game_new_handicap_setting_fragment, null);

		progressBar = (ProgressBar) view.findViewById(R.id.ProgressBar);

		calculators = new HandicapCalculator[] {
				new SimpleHandicapCalculator(), new Ecco1Calculator(),
				new LaterBetterCalculator() };
		String[] calculatorNames = new String[calculators.length];
		for (int i = 0; i < calculators.length; i++) {
			calculatorNames[i] = calculators[i].getName(activity);
		}

		final SpinnerAdapter handicapCalculatorSpinnerAdapter = new ArrayAdapter<String>(
				activity, R.layout.simple_spinner_dropdown_item,
				calculatorNames);
		handicapCalculatorSpinner = (Spinner) view
				.findViewById(R.id.HandicapCalculatorSpinner);
		handicapCalculatorSpinner.setAdapter(handicapCalculatorSpinnerAdapter);
		handicapCalculatorSpinner.setOnItemSelectedListener(this);

		playerViews = new View[Constants.MAX_PLAYER_COUNT];
		playerViews[0] = view.findViewById(R.id.Player1View);
		playerViews[1] = view.findViewById(R.id.Player2View);
		playerViews[2] = view.findViewById(R.id.Player3View);
		playerViews[3] = view.findViewById(R.id.Player4View);
		playerViews[4] = view.findViewById(R.id.Player5View);
		playerViews[5] = view.findViewById(R.id.Player6View);

		final SpinnerAdapter handicapAdapter = new ArrayAdapter<String>(
				activity, R.layout.simple_spinner_dropdown_item,
				new String[] { "-", "-1", "-2", "-3", "-4", "-5", "-6", "-7",
						"-8", "-9", "-10", "-11", "-12", "-13", "-14", "-15",
						"-16", "-17", "-18", "-19", "-20", "-21", "-22", "-23",
						"-24", "-25", "-26", "-27", "-28", "-29", "-30", "-31",
						"-32", "-33", "-34", "-35", "-36", "-37", "-38", "-39",
						"-40", "-41", "-42", "-43", "-44", "-45", "-46", "-47",
						"-48", "-49", "-50", "-51", "-52", "-53", "-54", "-55",
						"-56", "-57", "-58", "-59", "-60", "-61", "-62", "-63",
						"-64", "-65", "-66", "-67", "-68", "-69", "-70", "-71",
						"-72" });

		Spinner player1HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player1HandicapSpinner);
		player1HandicapSpinner.setAdapter(handicapAdapter);
		Spinner player2HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player2HandicapSpinner);
		player2HandicapSpinner.setAdapter(handicapAdapter);
		Spinner player3HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player3HandicapSpinner);
		player3HandicapSpinner.setAdapter(handicapAdapter);
		Spinner player4HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player4HandicapSpinner);
		player4HandicapSpinner.setAdapter(handicapAdapter);
		Spinner player5HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player5HandicapSpinner);
		player5HandicapSpinner.setAdapter(handicapAdapter);
		Spinner player6HandicapSpinner = (Spinner) view
				.findViewById(R.id.Player6HandicapSpinner);
		player6HandicapSpinner.setAdapter(handicapAdapter);

		playerHandicapSpinners = new Spinner[Constants.MAX_PLAYER_COUNT];
		playerHandicapSpinners[0] = player1HandicapSpinner;
		playerHandicapSpinners[1] = player2HandicapSpinner;
		playerHandicapSpinners[2] = player3HandicapSpinner;
		playerHandicapSpinners[3] = player4HandicapSpinner;
		playerHandicapSpinners[4] = player5HandicapSpinner;
		playerHandicapSpinners[5] = player6HandicapSpinner;

		Spinner player1ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player1ExtraScoreSpinner);
		player1ExtraScoreSpinner.setAdapter(handicapAdapter);
		Spinner player2ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player2ExtraScoreSpinner);
		player2ExtraScoreSpinner.setAdapter(handicapAdapter);
		Spinner player3ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player3ExtraScoreSpinner);
		player3ExtraScoreSpinner.setAdapter(handicapAdapter);
		Spinner player4ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player4ExtraScoreSpinner);
		player4ExtraScoreSpinner.setAdapter(handicapAdapter);
		Spinner player5ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player5ExtraScoreSpinner);
		player5ExtraScoreSpinner.setAdapter(handicapAdapter);
		Spinner player6ExtraScoreSpinner = (Spinner) view
				.findViewById(R.id.Player6ExtraScoreSpinner);
		player6ExtraScoreSpinner.setAdapter(handicapAdapter);

		playerExtraScoreSpinners = new Spinner[Constants.MAX_PLAYER_COUNT];
		playerExtraScoreSpinners[0] = player1ExtraScoreSpinner;
		playerExtraScoreSpinners[1] = player2ExtraScoreSpinner;
		playerExtraScoreSpinners[2] = player3ExtraScoreSpinner;
		playerExtraScoreSpinners[3] = player4ExtraScoreSpinner;
		playerExtraScoreSpinners[4] = player5ExtraScoreSpinner;
		playerExtraScoreSpinners[5] = player6ExtraScoreSpinner;

		TextView player1NameTextView = (TextView) view
				.findViewById(R.id.Player1NameTextView);
		TextView player2NameTextView = (TextView) view
				.findViewById(R.id.Player2NameTextView);
		TextView player3NameTextView = (TextView) view
				.findViewById(R.id.Player3NameTextView);
		TextView player4NameTextView = (TextView) view
				.findViewById(R.id.Player4NameTextView);
		TextView player5NameTextView = (TextView) view
				.findViewById(R.id.Player5NameTextView);
		TextView player6NameTextView = (TextView) view
				.findViewById(R.id.Player6NameTextView);

		playerNameTextViews = new TextView[Constants.MAX_PLAYER_COUNT];
		playerNameTextViews[0] = player1NameTextView;
		playerNameTextViews[1] = player2NameTextView;
		playerNameTextViews[2] = player3NameTextView;
		playerNameTextViews[3] = player4NameTextView;
		playerNameTextViews[4] = player5NameTextView;
		playerNameTextViews[5] = player6NameTextView;

		TextView player1GameCountTextView = (TextView) view
				.findViewById(R.id.Player1GameCountTextView);
		TextView player2GameCountTextView = (TextView) view
				.findViewById(R.id.Player2GameCountTextView);
		TextView player3GameCountTextView = (TextView) view
				.findViewById(R.id.Player3GameCountTextView);
		TextView player4GameCountTextView = (TextView) view
				.findViewById(R.id.Player4GameCountTextView);
		TextView player5GameCountTextView = (TextView) view
				.findViewById(R.id.Player5GameCountTextView);
		TextView player6GameCountTextView = (TextView) view
				.findViewById(R.id.Player6GameCountTextView);

		playerGameCountTexts = new TextView[Constants.MAX_PLAYER_COUNT];
		playerGameCountTexts[0] = player1GameCountTextView;
		playerGameCountTexts[1] = player2GameCountTextView;
		playerGameCountTexts[2] = player3GameCountTextView;
		playerGameCountTexts[3] = player4GameCountTextView;
		playerGameCountTexts[4] = player5GameCountTextView;
		playerGameCountTexts[5] = player6GameCountTextView;

		TextView player1AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player1AvgScoreTextView);
		TextView player2AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player2AvgScoreTextView);
		TextView player3AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player3AvgScoreTextView);
		TextView player4AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player4AvgScoreTextView);
		TextView player5AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player5AvgScoreTextView);
		TextView player6AvgScoreTextView = (TextView) view
				.findViewById(R.id.Player6AvgScoreTextView);

		playerAvgScoreTexts = new TextView[Constants.MAX_PLAYER_COUNT];
		playerAvgScoreTexts[0] = player1AvgScoreTextView;
		playerAvgScoreTexts[1] = player2AvgScoreTextView;
		playerAvgScoreTexts[2] = player3AvgScoreTextView;
		playerAvgScoreTexts[3] = player4AvgScoreTextView;
		playerAvgScoreTexts[4] = player5AvgScoreTextView;
		playerAvgScoreTexts[5] = player6AvgScoreTextView;

		playerRecommendHandicapTextViews = new TextView[Constants.MAX_PLAYER_COUNT];
		playerRecommendHandicapTextViews[0] = (TextView) view
				.findViewById(R.id.Player1RecommendHandicapTextView);
		playerRecommendHandicapTextViews[1] = (TextView) view
				.findViewById(R.id.Player2RecommendHandicapTextView);
		playerRecommendHandicapTextViews[2] = (TextView) view
				.findViewById(R.id.Player3RecommendHandicapTextView);
		playerRecommendHandicapTextViews[3] = (TextView) view
				.findViewById(R.id.Player4RecommendHandicapTextView);
		playerRecommendHandicapTextViews[4] = (TextView) view
				.findViewById(R.id.Player5RecommendHandicapTextView);
		playerRecommendHandicapTextViews[5] = (TextView) view
				.findViewById(R.id.Player6RecommendHandicapTextView);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		gameAndResults = new ArrayList<GameAndResult>();

		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);

			for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
				playerGameCountTexts[i].setText("");
				playerAvgScoreTexts[i].setText("");
				playerHandicapSpinners[i].setSelection(0);
				playerExtraScoreSpinners[i].setSelection(0);
			}
		}
	}

	public void setInitialValues(String[] names) {
		this.playerCount = names.length;
		this.playerNames = new String[playerCount];
		this.canonicalPlayerNames = new String[playerCount];

		for (int i = 0; i < playerCount; i++) {
			playerNames[i] = names[i];
			canonicalPlayerNames[i] = PlayerUIUtil.toCanonicalName(names[i]);
		}

		for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
			if (i < playerCount) {
				playerViews[i].setVisibility(View.VISIBLE);
				playerNameTextViews[i].setText(playerNames[i]);
			} else {
				playerViews[i].setVisibility(View.GONE);
			}
		}

		DateRange dateRange = DateRangeUtil.getDateRange(3);
		GameAndResultTask task = new GameAndResultTask(getSupportActivity(),
				this);
		task.execute(dateRange);
	}

	public int getHandicap(int playerId) {
		if (playerId < Constants.MIN_PLAYER_ID
				|| playerId > Constants.MAX_PLAYER_ID) {
			return 0;
		}
		return playerHandicapSpinners[playerId].getSelectedItemPosition();
	}

	public int getExtraScore(int playerId) {
		if (playerId < Constants.MIN_PLAYER_ID
				|| playerId > Constants.MAX_PLAYER_ID) {
			return 0;
		}
		return playerExtraScoreSpinners[playerId].getSelectedItemPosition();
	}

	public boolean isAllFieldValid() {
		return true;
	}

	@Override
	public void onGameAndResultStarted() {
		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onGameAndResultFinished(GameAndResult[] results) {
		gameAndResults.clear();
		for (GameAndResult result : results) {
			gameAndResults.add(result);
		}
		Collections.sort(gameAndResults);

		calculateHandicap();

		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		calculateHandicap();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void calculateHandicap() {
		Activity activity = getSupportActivity();
		HandicapCalculator calculator = calculators[handicapCalculatorSpinner
				.getSelectedItemPosition()];

		ArrayList<GameResultItem> items = new ArrayList<GameResultItem>();
		items.addAll(gameAndResults);
		calculator.calculate(canonicalPlayerNames, items);

		for (int i = 0; i < playerCount; i++) {
			String playerName = canonicalPlayerNames[i];
			int handicap = calculator.getHandicap(playerName);
			if (handicap < 0)
				handicap = 0;

			playerHandicapSpinners[i].setSelection(handicap);
			playerExtraScoreSpinners[i].setSelection(0);
			playerRecommendHandicapTextViews[i].setText(String
					.valueOf(handicap));

			int gameCount = calculator.getGameCount(playerName);
			playerGameCountTexts[i].setText(gameCount + "����");

			float avgScore = calculator.getAvgScore(playerName);
			if (gameCount > 0) {
				playerAvgScoreTexts[i].setText(UIUtil.formatAvgScore(activity,
						avgScore));
			} else {
				playerAvgScoreTexts[i].setText("-");
			}
		}
	}
}
