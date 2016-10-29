package com.egima.ussdmenuserver;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a subclass of ussd node that can handle events where a user is prompted for
 * custom data input e.g a pin
 * 
 * @author egima
 * 
 */

public abstract class UssdPrompt extends UssdNode {
	private String dynamicParent = null;
	private boolean isDynamic = false;
	

	private Object response = null;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdUserSession.class);
	public void log(String str) {
		LOGGER.info(str);
	}
	/**
	 * get the response from the client
	 * 
	 * @return
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * set response from client, returns true if data validation is successful
	 * 
	 * @param response
	 * @return
	 */
	public boolean setResponse(Object response) {
		if (validate(response)) {
			this.response = response;
			return true;
		}
		return false;
	}

	public UssdPrompt(String title, String name, String parent) {
		super(title, name, parent);
	}

	@Override
	public String toString() {
		if (isMultiSelect()) {
			log("calling toString() of super class UssdNode");
			return super.toString();
		}
		String title = getTitle();
		String disp = Application.replaceVars(title);
		return disp;
	}

	/**
	 * returns the child...numerous prompts can be chained together by using
	 * parent child relationship
	 * 
	 * @return
	 */
	public String getChild() {
		String[] children = getMenu();
		if (children.length == 0)
			return null;
		else
			return children[0];
	}

	/**
	 * a method called to retrieve the validation error to send to client incase
	 * validation fails for client's input data
	 * 
	 * @return
	 */
	public abstract String getValidationError();

	/**
	 * a method called to ascertain the validity of the input data
	 * 
	 * @param data
	 * @return
	 */
	public abstract boolean validate(Object data);

	/**
	 * last message sent to client at the end of the session
	 * 
	 * @param allUserData
	 * @return
	 */
	public String getDynamicParent() {
		return dynamicParent;
	}

	public void setDynamicParent(String dynamicParent) {
		this.dynamicParent = dynamicParent;
	}

	public boolean isDynamic() {
		return isDynamic;
	}

	public void setDynamic(boolean isDynamic) {
		this.isDynamic = isDynamic;
	}

	public String generateSessionEndMessage(HashMap<String, Object> allUserData) {
		return "session ended";
	}

}
