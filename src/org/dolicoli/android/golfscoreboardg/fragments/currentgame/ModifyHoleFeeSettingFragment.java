package org.dolicoli.android.golfscoreboardg.fragments.currentgame;

import java.text.DecimalFormat;

import org.dolicoli.android.golfscoreboardg.Constants;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.db.GameSettingDatabaseWorker;
import org.dolicoli.android.golfscoreboardg.utils.FeeCalculator;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

public class ModifyHoleFeeSettingFragment extends Fragment {

	private int holeCount, playerCount, totalHoleFee;

	private TextView sumTextView, totalHoleFeeTextView;

	private View[] feeTitleTextViews;
	private EditText[] feeEditTexts;
	private TextView[] recommendTextViews;

	private DecimalFormat feeFormat;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.current_game_modify_hole_fee_setting_fragment, null);

		feeFormat = new DecimalFormat(getString(R.string.fee_format));

		sumTextView = (TextView) view.findViewById(R.id.SumTextView);
		totalHoleFeeTextView = (TextView) view
				.findViewById(R.id.TotalHoleFeeTextView);

		View fee1TitleTextView = view.findViewById(R.id.Fee2TitleTextView);
		View fee2TitleTextView = view.findViewById(R.id.Fee2TitleTextView);
		View fee3TitleTextView = view.findViewById(R.id.Fee3TitleTextView);
		View fee4TitleTextView = view.findViewById(R.id.Fee4TitleTextView);
		View fee5TitleTextView = view.findViewById(R.id.Fee5TitleTextView);
		View fee6TitleTextView = view.findViewById(R.id.Fee6TitleTextView);
		feeTitleTextViews = new View[Constants.MAX_PLAYER_COUNT];
		feeTitleTextViews[0] = fee1TitleTextView;
		feeTitleTextViews[1] = fee2TitleTextView;
		feeTitleTextViews[2] = fee3TitleTextView;
		feeTitleTextViews[3] = fee4TitleTextView;
		feeTitleTextViews[4] = fee5TitleTextView;
		feeTitleTextViews[5] = fee6TitleTextView;

		EditText fee1EditText = (EditText) view.findViewById(R.id.Fee1EditText);
		EditText fee2EditText = (EditText) view.findViewById(R.id.Fee2EditText);
		EditText fee3EditText = (EditText) view.findViewById(R.id.Fee3EditText);
		EditText fee4EditText = (EditText) view.findViewById(R.id.Fee4EditText);
		EditText fee5EditText = (EditText) view.findViewById(R.id.Fee5EditText);
		EditText fee6EditText = (EditText) view.findViewById(R.id.Fee6EditText);
		feeEditTexts = new EditText[Constants.MAX_PLAYER_COUNT];
		feeEditTexts[0] = fee1EditText;
		feeEditTexts[1] = fee2EditText;
		feeEditTexts[2] = fee3EditText;
		feeEditTexts[3] = fee4EditText;
		feeEditTexts[4] = fee5EditText;
		feeEditTexts[5] = fee6EditText;

		TextWatcher watcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				feeEditChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
		};
		feeEditTexts[0].addTextChangedListener(watcher);
		feeEditTexts[1].addTextChangedListener(watcher);
		feeEditTexts[2].addTextChangedListener(watcher);
		feeEditTexts[3].addTextChangedListener(watcher);
		feeEditTexts[4].addTextChangedListener(watcher);
		feeEditTexts[5].addTextChangedListener(watcher);

		TextView fee1RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend1TextView);
		TextView fee2RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend2TextView);
		TextView fee3RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend3TextView);
		TextView fee4RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend4TextView);
		TextView fee5RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend5TextView);
		TextView fee6RecommendTextView = (TextView) view
				.findViewById(R.id.Recommend6TextView);
		recommendTextViews = new TextView[Constants.MAX_PLAYER_COUNT];
		recommendTextViews[0] = fee1RecommendTextView;
		recommendTextViews[1] = fee2RecommendTextView;
		recommendTextViews[2] = fee3RecommendTextView;
		recommendTextViews[3] = fee4RecommendTextView;
		recommendTextViews[4] = fee5RecommendTextView;
		recommendTextViews[5] = fee6RecommendTextView;

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		GameSettingDatabaseWorker gameSettingWorker = new GameSettingDatabaseWorker(
				getActivity());

		GameSetting gameSetting = new GameSetting();
		gameSettingWorker.getGameSetting(gameSetting);

		for (int i = 1; i <= Constants.MAX_PLAYER_COUNT; i++) {
			feeEditTexts[i - 1].setText(String.valueOf(gameSetting
					.getHoleFeeForRanking(i)));
		}
	}

	public void setInitialValues(int holeCount, int playerCount,
			int totalHoleFee) {
		this.holeCount = holeCount;
		this.playerCount = playerCount;
		this.totalHoleFee = totalHoleFee;

		totalHoleFeeTextView.setText(feeFormat.format(totalHoleFee));

		for (int i = 2; i < feeTitleTextViews.length; i++) {
			if (i < playerCount) {
				feeEditTexts[i].setVisibility(View.VISIBLE);
				feeTitleTextViews[i].setVisibility(View.VISIBLE);
				recommendTextViews[i].setVisibility(View.VISIBLE);
			} else {
				feeEditTexts[i].setVisibility(View.GONE);
				feeTitleTextViews[i].setVisibility(View.GONE);
				recommendTextViews[i].setVisibility(View.GONE);
			}
		}

		fillRecommendFees();
		fillFee();
	}

	private void fillRecommendFees() {
		if (totalHoleFee <= 0) {
			recommendTextViews[0].setText(feeFormat.format(0));
			recommendTextViews[1].setText(feeFormat.format(0));
			recommendTextViews[2].setText(feeFormat.format(0));
			recommendTextViews[3].setText(feeFormat.format(0));
			recommendTextViews[4].setText(feeFormat.format(0));
			recommendTextViews[5].setText(feeFormat.format(0));
			return;
		}

		int holeFeePerHole = 0;
		int remain = totalHoleFee % holeCount;
		holeFeePerHole = (totalHoleFee - remain) / holeCount;

		int[] recommendFeesForRanking = new int[Constants.MAX_PLAYER_COUNT];

		switch (playerCount) {
		case 2:
			recommendFeesForRanking[0] = 0;
			recommendFeesForRanking[1] = FeeCalculator.toFee(holeFeePerHole, 1);
			recommendFeesForRanking[2] = 0;
			recommendFeesForRanking[3] = 0;
			recommendFeesForRanking[4] = 0;
			recommendFeesForRanking[5] = 0;

			break;

		case 3:
			recommendFeesForRanking[0] = 0;
			recommendFeesForRanking[1] = FeeCalculator.toFee(
					holeFeePerHole * 2, 2 + 3);
			recommendFeesForRanking[2] = FeeCalculator.toFee(
					holeFeePerHole * 3, 2 + 3);
			recommendFeesForRanking[3] = 0;
			recommendFeesForRanking[4] = 0;
			recommendFeesForRanking[5] = 0;
			break;

		case 4:
			recommendFeesForRanking[0] = 0;
			recommendFeesForRanking[1] = FeeCalculator.toFee(
					holeFeePerHole * 2, 2 + 3 + 4);
			recommendFeesForRanking[2] = FeeCalculator.toFee(
					holeFeePerHole * 3, 2 + 3 + 4);
			recommendFeesForRanking[3] = FeeCalculator.toFee(
					holeFeePerHole * 4, 2 + 3 + 4);
			recommendFeesForRanking[4] = 0;
			recommendFeesForRanking[5] = 0;
			break;

		case 5:
			recommendFeesForRanking[0] = 0;
			recommendFeesForRanking[1] = FeeCalculator.toFee(
					holeFeePerHole * 3, 3 + 4 + 5 + 6);
			recommendFeesForRanking[2] = FeeCalculator.toFee(
					holeFeePerHole * 4, 3 + 4 + 5 + 6);
			recommendFeesForRanking[3] = FeeCalculator.toFee(
					holeFeePerHole * 5, 3 + 4 + 5 + 6);
			recommendFeesForRanking[4] = FeeCalculator.toFee(
					holeFeePerHole * 6, 3 + 4 + 5 + 6);
			recommendFeesForRanking[5] = 0;
			break;

		case 6:
			recommendFeesForRanking[0] = 0;
			recommendFeesForRanking[1] = FeeCalculator.toFee(
					holeFeePerHole * 3, 3 + 4 + 5 + 6 + 7);
			recommendFeesForRanking[2] = FeeCalculator.toFee(
					holeFeePerHole * 4, 3 + 4 + 5 + 6 + 7);
			recommendFeesForRanking[3] = FeeCalculator.toFee(
					holeFeePerHole * 5, 3 + 4 + 5 + 6 + 7);
			recommendFeesForRanking[4] = FeeCalculator.toFee(
					holeFeePerHole * 6, 3 + 4 + 5 + 6 + 7);
			recommendFeesForRanking[5] = FeeCalculator.toFee(
					holeFeePerHole * 7, 3 + 4 + 5 + 6 + 7);
			break;

		default:
			recommendTextViews[0].setText(feeFormat.format(0));
			recommendTextViews[1].setText(feeFormat.format(0));
			recommendTextViews[2].setText(feeFormat.format(0));
			recommendTextViews[3].setText(feeFormat.format(0));
			recommendTextViews[4].setText(feeFormat.format(0));
			recommendTextViews[5].setText(feeFormat.format(0));
			return;
		}

		int sum = 0;
		for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
			sum += recommendFeesForRanking[i];
		}
		int addition = FeeCalculator.toFee(holeFeePerHole - sum, 1);
		recommendFeesForRanking[playerCount - 1] += addition;

		for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
			recommendTextViews[i].setText(feeFormat
					.format(recommendFeesForRanking[i]));
		}
	}

	private void fillFee() {
		int fee = 0;
		for (int i = 1; i <= playerCount; i++) {
			fee += getFeeForRanking(i);
		}

		sumTextView.setText(feeFormat.format(fee * holeCount));
	}

	private void feeEditChanged() {
		fillFee();
	}

	public int getFeeForRanking(int ranking) {
		try {
			return Integer.parseInt(feeEditTexts[ranking - 1].getText()
					.toString());
		} catch (Throwable t) {
			return 0;
		}
	}
}
