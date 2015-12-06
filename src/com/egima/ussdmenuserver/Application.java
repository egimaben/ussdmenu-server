package com.egima.ussdmenuserver;

import java.util.HashMap;
import java.util.Map;

public class Application {
public static UssdTree tree=null;
public static int bufferLimit=4;
public static HashMap<String,String> error=new HashMap<>();
public static String ussdCode="*298#";
public static String CLIENT_URL="http://127.0.0.1:8000/ussd/";
public static String APP_ID="appid";
public static String APP_PASSWORD="password";
public static Map<String,Double> ODDS=new HashMap<>();
public static void loadOdds()
{
	ODDS.put("#2", 3.2);
	ODDS.put("#1", 2.9);
	ODDS.put("#0", 1.6);

}

public static String getError(String code){
	return error.get(code);
}
public static String TEST_PIN="4649";
public static boolean notAppShortCode(String message) {
	return !message.equals(ussdCode);
}
public static double toDbl(Object str){
	double dbl=-1;
	try{
		dbl=Double.parseDouble(str+"");
		return dbl;
	}catch(Exception e){
		return dbl;
	}
}
public static boolean isPrompt(UssdNode node){
	return node instanceof UssdPrompt;
}
public static int toInt(String str){
	int n;
	try{
		n=Integer.parseInt(str);
	}catch(Exception e){
		n=-1;
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

}
