package org.dolicoli.android.golfscoreboardg.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.dolicoli.android.golfscoreboardg.data.settings.GameSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.PlayerSetting;
import org.dolicoli.android.golfscoreboardg.data.settings.Result;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

public class GameParser extends ResponseParser {

	private static final String TAG = "GameParser";

	private long tick;
	private GameSetting gameSetting;
	private PlayerSetting playerSetting;
	private ArrayList<Result> results;

	public long getTick() {
		return tick;
	}

	public boolean parse(Context context, String contents)
			throws ResponseException {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			ByteArrayInputStream bis = new ByteArrayInputStream(
					contents.getBytes());
			InputStream stream = bis;

			xpp.setInput(stream, "UTF-8");
			int eventType = xpp.getEventType();
			Result result = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("response")) {
						String resultCode = xpp.getAttributeValue(null,
								"result");
						if (resultCode.equals("ERROR")) {
							String errorMessage = xpp.getAttributeValue(null,
									"message");
							throw new ResponseException(errorMessage);
						}

						String tickString = xpp.getAttributeValue(null, "tick");
						if (tickString != null && !tickString.isEmpty()) {
							try {
								tick = Long.parseLong(tickString);
							} catch (Throwable t) {
							}
						}

						gameSetting = new GameSetting();
						playerSetting = new PlayerSetting();
						results = new ArrayList<Result>();

						String value = xpp.getAttributeValue(null, "gameId");
						value = xpp.getAttributeValue(null, "gameDate");
						gameSetting.setDate(getLong(value));

						value = xpp.getAttributeValue(null, "holeCount");
						gameSetting.setHoleCount(getInt(value));

						value = xpp.getAttributeValue(null, "playerCount");
						gameSetting.setPlayerCount(getInt(value));

					} else if (xpp.getName().equals("fee")) {
						String value = xpp.getAttributeValue(null, "holeFee");
						gameSetting.setGameFee(getInt(value));

						value = xpp.getAttributeValue(null, "extraFee");
						gameSetting.setExtraFee(getInt(value));

						value = xpp.getAttributeValue(null, "rankingFee");
						gameSetting.setRankingFee(getInt(value));

					} else if (xpp.getName().equals("holeFee")) {
						String value = xpp.getAttributeValue(null, "ranking");
						int ranking = getInt(value);
						value = xpp.getAttributeValue(null, "fee");
						int fee = getInt(value);
						gameSetting.setHoleFeeForRanking(ranking, fee);

					} else if (xpp.getName().equals("rankingFee")) {
						String value = xpp.getAttributeValue(null, "ranking");
						int ranking = getInt(value);
						value = xpp.getAttributeValue(null, "fee");
						int fee = getInt(value);
						gameSetting.setRankingFeeForRanking(ranking, fee);

					} else if (xpp.getName().equals("player")) {
						String value = xpp.getAttributeValue(null, "id");
						int playerId = getInt(value);
						String playerName = xpp.getAttributeValue(null, "name");
						playerSetting.setPlayerName(playerId, playerName);

						value = xpp.getAttributeValue(null, "handicap");
						int handicap = getInt(value);
						playerSetting.setHandicap(playerId, handicap);

						try {
							value = xpp.getAttributeValue(null, "extraScore");
							int extraScore = getInt(value);
							playerSetting.setExtraScore(playerId, extraScore);
						} catch (Throwable t) {
						}
					} else if (xpp.getName().equals("results")) {
					} else if (xpp.getName().equals("result")) {

						String value = xpp
								.getAttributeValue(null, "holeNumber");
						int holeNumber = getInt(value);

						value = xpp.getAttributeValue(null, "parCount");
						int parCount = getInt(value);

						result = new Result(holeNumber, parCount);
					} else if (xpp.getName().equals("score")) {
						if (result != null) {
							String value = xpp.getAttributeValue(null,
									"playerId");
							int playerId = getInt(value);

							value = xpp.getAttributeValue(null, "score");
							int score = getInt(value);

							value = xpp.getAttributeValue(null, "usedHandicap");
							int usedHandicap = getInt(value);

							result.setScore(playerId, score);
							result.setUsedHandicap(playerId, usedHandicap);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("result")) {
						if (result != null) {
							results.add(result);
						}
						result = null;
					}
					break;
				case XmlPullParser.TEXT:
					break;
				}
				eventType = xpp.next();
			}
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
			return false;
		}
	}

	public GameSetting getGameSetting() {
		return gameSetting;
	}

	public PlayerSetting getPlayerSetting() {
		return playerSetting;
	}

	public ArrayList<Result> getResults() {
		return results;
	}
}
