package com.egima.ussdmenuserver;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
	public static int bufferLimit = 6;
	public static HashMap<String, String> error = new HashMap<>();
	public static String ussdCode = "*298#";
	public static String CLIENT_URL = "http://127.0.0.1:8000/ussd/";
	public static String APP_ID = "appid";
	public static String DELIM = ",";
	public static final int USSD_CHAR_LIMIT = 160;
	public static final String SEP = System.getProperty("line.separator");
	public static HashMap<String, Object> userData = new HashMap<>();
	public static String APP_PASSWORD = "password";
	public static HashMap<String, UssdUserSession> userSessions = new HashMap<>();;
	public static Map<String, Double> ODDS = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Application.class);

	public void log(String str) {
		LOGGER.info( str);
	}

	public static String getError(String code) {
		return error.get(code);
	}

	public static String TEST_PIN = "4649";

	public static boolean notAppShortCode(String message) {
		return !message.equals(ussdCode);
	}

	public static double toDbl(Object str) {
		double dbl = -1;
		try {
			dbl = Double.parseDouble(str + "");
			return dbl;
		} catch (Exception e) {
			return dbl;
		}
	}

	public static boolean isPrompt(UssdNode node) {
		return node instanceof UssdPrompt;
	}

	public static int toInt(String str) {
		int n;
		try {
			n = Integer.parseInt(str);
		} catch (Exception e) {
			n = -1;
		}
		return n;
	}

	public static boolean isUssdCode(String message) {
		String firstChar = message.substring(0, 1);
		String lastChar = message.substring(message.length() - 1);
		if (firstChar.equals("*") && lastChar.equals("#"))
			return true;
		return false;
	}

	public static String replaceVars(String message) {
		String edited = message;
		int count = 0;
		while (edited.contains("$") || edited.contains("%")) {
			count++;
			if (edited.contains("$")) {
				String original = "";
				int index = edited.indexOf("$");
				int endIndex = edited.indexOf("@", index);
				original = edited.substring(index, endIndex + 1);
				String finalz = original.substring(1, original.indexOf("@"));
				if (userData.containsKey(finalz)) {
					Object o = userData.get(finalz);
					double sum = 0.0;
					if (o instanceof String[]) {
						String[] arr = (String[]) o;
						sum = toDbl(arr[arr.length - 1]);
					} else
						sum = toDbl("" + o);
					String sumz = formatCash(sum);
					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, sumz);
				}
			}
			if (edited.contains("%")) {

				int index = edited.indexOf("%");
				int endIndex = edited.indexOf("@", index);
				String original = edited.substring(index, endIndex + 1);
				int finIndex = original.indexOf("@");
				String finalz = original.substring(1, finIndex);
				finalz = isCommand(original) ? original.split("_")[1] : finalz;
				if (finalz.contains("@"))
					finalz = finalz.substring(0, finalz.indexOf("@"));
				if (userData.containsKey(finalz)) {
					Object o = userData.get(finalz);
					String val = null;
					if (o instanceof String[]) {
						String[] arr = (String[]) o;
						if (original.contains("COUNT"))
							val = arr.length + "";

						else if (original.contains("SUM"))
							val = "" + sum(arr);
						else
							val = arr[arr.length - 1];
					} else
						val = (String) userData.get(finalz);

					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, val);
				}
			}
			if (count == 10)
				break;
		}
		return edited;

	}

	public static double sum(Object[] tokens) {
		double sum = 0.0;
		for (Object o : tokens) {
			double d = Double.parseDouble("" + o);
			sum += d;
		}
		return sum;
	}

	public static String replaceTitleVars(String message,String address) {
		String edited = message;
		while (edited.contains("$") || edited.contains("%")) {
			if (edited.contains("$")) {
				String original = "";
				int index = edited.indexOf("$");
				int endIndex = edited.indexOf("@", index);

				original = edited.substring(index, endIndex + 1);
				String finalz = original.substring(1, original.indexOf("@"));
				if (userData.containsKey(finalz)) {
					Object o = userData.get(finalz);
					double sum = 0.0;
					if (o instanceof String[]) {
						String[] arr = (String[]) o;
						sum = toDbl(arr[arr.length - 1]);
					} else
						sum = toDbl("" + o);
					String sumz = formatCash(sum);
					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, sumz);
				}
			}
			if (edited.contains("%")) {
				int index = edited.indexOf("%");
				int endIndex = edited.indexOf("@", index);
				String original = edited.substring(index, endIndex + 1);
				int finIndex = original.indexOf("@");
				String finalz = original.substring(1, finIndex);
				finalz = isCommand(original) ? original.split("_")[1] : finalz;
				if (userData.containsKey(finalz)) {
					Object o = userData.get(finalz);
					String val = null;
					if (o instanceof String[]) {
						String[] arr = (String[]) o;
						if (original.contains("COUNT"))
							val = arr.length + "";

						else if (original.contains("SUM"))
							val = "" + sum(arr);
						else
							val = arr[arr.length - 1];
					} else
						val = (String) userData.get(finalz);
					String title = original.contains("COUNT") ? val : userSessions.get(address).getMyTree()
							.getNode(val).getTitle();
					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, title);
				}

			}
		}

		return edited;
	}

	public static boolean isCommand(String nodeName) {
		return nodeName.charAt(0) == '%' && nodeName.contains("_");
	}

	public static String formatCash(double cash) {
		DecimalFormat df = new DecimalFormat("###,###,###.##");
		df.setRoundingMode(RoundingMode.CEILING);
		return df.format(cash);
	}

	public static String[] removeElements(String[] input, String deleteMe) {
		List<String> result = new LinkedList<>();

		for (String item : input)
			if (!deleteMe.equals(item))
				result.add(item);
		String[] arr = new String[input.length - 1];
		arr = result.toArray(new String[1]);
		return arr;
	}

	public static void main(String[] args) {
		String str = "Enter Amount No. of Games:%COUNT_leagues@ Total ration:%SUM_odds@";
		str = replaceVars(str);
		System.out.println("final=" + str);
	}

}
