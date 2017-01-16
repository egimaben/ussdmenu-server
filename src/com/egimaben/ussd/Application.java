package com.egimaben.ussd;

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
	public static final String MSISDN="-msisdn-";
	public static final int USSD_CHAR_LIMIT = 160;
	public static final String SEP = System.getProperty("line.separator");
	public static String APP_PASSWORD = "password";
	public static HashMap<String, UssdUserSession> userSessions = new HashMap<>();
	public static Map<String, Double> ODDS = new HashMap<>();
	public static boolean ENABLE_EXIT=false;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Application.class);

	public static void log(String str) {
		LOGGER.info(str);
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

	public static String replaceVars(String message, String address) {
		String edited = message;
		log("replacing vars: " + message);
		Map<String, Object> userData = userSessions.get(address)
				.getAllUserData();
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
						val = String.valueOf(userData.get(finalz));

					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, val);
				}
			}
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

	public static String replaceTitleVars(String message, String address) {
		String edited = message;
		Map<String, Object> userData = userSessions.get(address)
				.getAllUserData();

		while (edited.contains("$") || edited.contains("%")) {
			log("recursing substitution of vars");
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
				log("in replacing title vars, got final="+finalz);
				finalz = isCommand(original) ? original.split("_")[1] : finalz;
				log("after checking if is command,it's called: "+finalz);
				if (userData.containsKey(finalz)) {
					Object o = userData.get(finalz);
					log("found it's value in storage: "+o);
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
						val = String.valueOf(o);
//					String title = original.contains("COUNT") ? val
//							: userSessions.get(address).getMyTree()
//									.getNode(val).getTitle();
					String matcher = java.util.regex.Matcher
							.quoteReplacement(original);
					edited = edited.replaceAll(matcher, val);
				}
				if (isFunction(finalz)) {
					if (finalz.startsWith("PERCENT")) {
						finalz = finalz.replaceAll("PERCENT", "")
								.replaceAll("(", "").replaceAll(")", "");
						String[] params = finalz.split(",");
						String percent = calcPercent(params[0], params[1],
								userData);
						String matcher = java.util.regex.Matcher
								.quoteReplacement(original);
						edited = edited.replaceAll(matcher, percent);
					}
				}

			}
		}

		return edited;
	}

	private static boolean isFunction(String str) {
		return str.contains("(") && str.contains(")");
	}

	private static String calcPercent(String operand, String percent,
			Map<String, Object> userData) {
		String strVal = null;
		double op = toDbl(operand);
		double perc = toDbl(percent);
		if (op == -1)
			op = toDbl(userData.get(operand));
		if (perc == -1)
			perc = toDbl(userData.get(percent));
		double finalVal = op * perc;
		strVal = String.valueOf(finalVal);
		return strVal;
	}

	public static boolean isCommand(String nodeName) {
		return nodeName.charAt(0) == '%' && nodeName.contains("_");
	}

	public static String formatCash(double cash) {
		DecimalFormat df = new DecimalFormat("### ### ###.##");
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


}
