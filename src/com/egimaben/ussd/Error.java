package com.egimaben.ussd;

public class Error {
	public static String USER_EXIT_MESSAGE="Thank you,good bye";
	public static String INVALID_SHORCODE_ERROR="Invalid shortcode";
	public static String UNKNOWN_APPLICATION_ERROR="Unknown application";
	public static String NON_INTEGER_INPUT_ON_MENUITEM_ERROR="Invalid integer input";
	public static String MIDSESSION_SHORTCODE_INPUT="Expecting integer not shortcode";
	public static String INVALID_MULTIPLE_INPUT_ERROR="invalid multiple input, separate inputs using "
			+ Application.DELIM;
	public static String KILL_MESSAGE="You don't have access to this application";
	public static String MISSING_NODES_MESSAGE="NO DATA AVAILABLE, TRY AGAIN LATER";
	public static String NODE_END_EVENT_MESSAGE="Thank you, good bye";
	public static String SESSION_END_MESSAGE="Session ended";
	public static String INPUT_CHANGE_REQUEST="Invalid input, try again";
	public static String APP_NOT_READY_MESSAGE="Application not ready, try again later";
	public static String EXIT_NOT_ENABLED_MESSAGE="Invalid input 0";
}
