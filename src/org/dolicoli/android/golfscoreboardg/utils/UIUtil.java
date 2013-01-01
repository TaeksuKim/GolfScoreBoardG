package org.dolicoli.android.golfscoreboardg.utils;

import java.text.DecimalFormat;

import org.dolicoli.android.golfscoreboardg.Constants;
import org.dolicoli.android.golfscoreboardg.R;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

public class UIUtil {
	private static boolean primaryTextColorLoaded = false;
	private static int primaryTextColor = -1;

	private static DecimalFormat feeFormat;
	private static DecimalFormat rankingFormat;
	private static DecimalFormat avgRankingFormat;
	private static DecimalFormat avgScoreFormat;
	private static DecimalFormat gameCountFormat;
	private static DecimalFormat rateFormat;
	private static DecimalFormat playerCountFormat;

	public static String formatFee(Context context, int fee) {
		if (context == null)
			return String.valueOf(fee);

		if (feeFormat == null) {
			feeFormat = new DecimalFormat(
					context.getString(R.string.fee_format));
		}

		return feeFormat.format(fee);
	}

	public static String formatFee(Context context, double fee) {
		return formatFee(context, (int) fee);
	}

	public static void setFeeTextView(Context context, TextView textView,
			int fee) {
		if (context == null || textView == null)
			return;

		String text = formatFee(context, fee);

		textView.setText(text);
	}

	public static void setFeeTextView(Context context, TextView textView,
			double fee) {
		setFeeTextView(context, textView, (int) fee);
	}

	public static String formatRanking(Context context, int ranking) {
		if (context == null)
			return String.valueOf(ranking);

		if (rankingFormat == null) {
			rankingFormat = new DecimalFormat(
					context.getString(R.string.ranking_format));
		}

		return rankingFormat.format(ranking);
	}

	public static void setRankingTextView(Context context, TextView textView,
			int ranking) {
		if (context == null || textView == null)
			return;

		String text = formatRanking(context, ranking);

		textView.setText(text);
	}

	public static String formatAvgRanking(Context context, double ranking) {
		if (context == null)
			return String.valueOf(ranking);

		if (avgRankingFormat == null) {
			avgRankingFormat = new DecimalFormat(
					context.getString(R.string.avg_ranking_format));
		}

		return avgRankingFormat.format(ranking);
	}

	public static void setAvgRankingTextView(Context context,
			TextView textView, double ranking) {
		if (context == null || textView == null)
			return;

		String text = formatAvgRanking(context, ranking);

		textView.setText(text);
	}

	public static String formatAvgScore(Context context, double score) {
		if (context == null)
			return String.valueOf(score);

		if (avgScoreFormat == null) {
			avgScoreFormat = new DecimalFormat("0.00");
		}

		String text = avgScoreFormat.format(score);
		if (score <= 0.0) {
			return text;
		}
		return "+" + text;
	}

	public static void setAvgScoreTextView(Context context, TextView textView,
			double score) {
		loadPrimaryTextColor(context);
		setAvgScoreTextView(context, textView, primaryTextColor, score);
	}

	public static void setAvgScoreTextView(Context context, TextView textView,
			int defaultTextColor, double score) {
		if (context == null || textView == null)
			return;

		String text = formatAvgScore(context, score);

		textView.setText(text);
		if (score < 0.0) {
			textView.setTextColor(Constants.UNDER_PAR_TEXT_COLOR);
		} else if (score == 0.0) {
			textView.setTextColor(Constants.EVEN_PAR_TEXT_COLOR);
		} else {
			textView.setTextColor(defaultTextColor);
		}
	}

	public static String formatScore(Context context, int score) {
		if (context == null)
			return String.valueOf(score);

		if (score <= 0) {
			return String.valueOf(score);
		}

		return "+" + String.valueOf(score);
	}

	public static void setScoreTextView(Context context, TextView textView,
			int score) {
		loadPrimaryTextColor(context);
		setScoreTextView(context, textView, primaryTextColor, score);
	}

	public static void setScoreTextView(Context context, TextView textView,
			int defaultTextColor, int score) {
		if (context == null || textView == null)
			return;

		String text = formatScore(context, score);

		textView.setText(text);
		if (score < 0) {
			textView.setTextColor(Constants.UNDER_PAR_TEXT_COLOR);
		} else if (score == 0) {
			textView.setTextColor(Constants.EVEN_PAR_TEXT_COLOR);
		} else {
			textView.setTextColor(defaultTextColor);
		}
	}

	public static String formatHandicap(Context context, int handicap) {
		if (context == null)
			return String.valueOf(handicap);

		if (handicap <= 0) {
			return String.valueOf(handicap);
		}

		return "+" + String.valueOf(handicap);
	}

	public static void setHandicapTextView(Context context, TextView textView,
			int handicap) {
		loadPrimaryTextColor(context);
		setHandicapTextView(context, textView, primaryTextColor, handicap);
	}

	public static void setHandicapTextView(Context context, TextView textView,
			int defaultTextColor, int handicap) {
		if (context == null || textView == null)
			return;

		String text = formatHandicap(context, handicap);
		textView.setText(text);
	}

	public static String formatGameCount(Context context, int count) {
		if (context == null)
			return String.valueOf(count);

		if (gameCountFormat == null) {
			gameCountFormat = new DecimalFormat(
					context.getString(R.string.game_count_format));
		}

		return gameCountFormat.format(count);
	}

	public static void setGameCountTextView(Context context, TextView textView,
			int count) {
		if (context == null || textView == null)
			return;

		String text = formatGameCount(context, count);
		textView.setText(text);
	}

	public static String formatRate(Context context, double rate) {
		if (context == null)
			return String.valueOf(rate);

		if (rateFormat == null) {
			rateFormat = new DecimalFormat(
					context.getString(R.string.rate_format));
		}

		return rateFormat.format(rate);
	}

	public static void setRateTextView(Context context, TextView textView,
			double rate) {
		if (context == null || textView == null)
			return;

		String text = formatRate(context, rate);
		textView.setText(text);
	}

	public static String formatPlayerCount(Context context, int count) {
		if (context == null)
			return String.valueOf(count);

		if (playerCountFormat == null) {
			playerCountFormat = new DecimalFormat(
					context.getString(R.string.player_count_format));
		}

		return playerCountFormat.format(count);
	}

	public static void setPlayerCountTextView(Context context,
			TextView textView, int count) {
		if (context == null || textView == null)
			return;

		String text = formatPlayerCount(context, count);

		textView.setText(text);
	}

	private static void loadPrimaryTextColor(Context context) {
		if (primaryTextColorLoaded)
			return;

		TypedValue tv = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.primaryTextColor, tv, true);
		primaryTextColor = context.getResources().getColor(tv.resourceId);
		primaryTextColorLoaded = true;
	}
}
