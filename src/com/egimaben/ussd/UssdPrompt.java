package com.egimaben.ussd;

import java.util.HashMap;
import java.util.Map;

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
			// first copy this response to the local variable
			this.response = response;
			// get client's address
			String address = getAddress();
			// use address to get his session object
			UssdUserSession sess = Application.userSessions.get(address);
			// defensively copy session data into a new map
			Map<String, Object> userData = new HashMap<>(sess.getAllUserData());
			// give API client existing user data and current response so he can
			// create his k,v pairs
			Map<String, Object> newValues = updateDataState(userData, response);
			// if he did something, then add only new data to the user's session
			if (newValues != null)
				for (String key : newValues.keySet())
					if (!userData.containsKey(key))
						sess.addData(key, newValues.get(key));
			// update defensive copies
			userData = new HashMap<>(sess.getAllUserData());
			response = this.response;
			// give API client freedom to create dynamic nodes that take effect
			// after this prompt exits
			UssdNode[] dynamicNodes = getDynamicNodes(userData, response);
			if (dynamicNodes != null)
				log("got some dynamic nodes:" + dynamicNodes.length);
			UssdTree tree = sess.getMyTree();
			tree.addNode(dynamicNodes);
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
		String disp = Application.replaceVars(title, getAddress());
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
	 * a method called to allow for predetermined modifications to user input
	 * and to add named values that capture state information that will be
	 * referenced in a future menu. Any value added in the returned map will be
	 * available in a future call using the %@ reference notation.<br />
	 * The <code>userData</code> argument avails all already stored inputs to
	 * the programmer incase they are needed to compute new values for storage.
	 * It's recommended that the programmer create a new map to return and only
	 * use the <code>userData</code> as a reference for computing new values.<br />
	 * This is strictly for performance reasons as the returned map will be
	 * checked key for key to get any new keys. Only data that was previously
	 * absent in the existing storage will be saved, already existing keys will
	 * not be updated. So it is best practice to create a new map and only add
	 * to it new keys.
	 * 
	 * @param userData
	 *            A map of all data saved in the session upto this point.
	 * @param currentInput
	 *            The data that has been entered by the user in response to the
	 *            current prompt. at this point, it has not yet been added to
	 *            the storage, the programmer need not to worry about when it
	 *            will be added and instead only use it for computing extra
	 *            data.
	 * @return
	 */
	public abstract Map<String, Object> updateDataState(
			Map<String, Object> userData, Object currentInput);

	/**
	 * method called to allow for creating of dynamic nodes which are only valid
	 * within the context of the user input provided for the current prompt.
	 * They are dynamically added. However there is no rule to check whether
	 * the new nodes are children of an already rendered node or the current one.
	 * This API is designed to create new children for only the current prompt/node.
	 * If the parent is set to an earlier rendered menu, the behavior is undefined. 
	 * @param userData 
	 * 				  A copy of all data saved in the session up to this point. 
	 * @param currentInput
	 *   		  The data that has been entered by the user in response to the
	 *            current prompt. at this point, it has not yet been added to
	 *            the storage, the programmer need not to worry about when it
	 *            will be added and instead only use it for computing extra
	 *            nodes.
	 * 
	 * @return an array of ussd nodes to be added supposedly as children to the current
	 *         node/prompt
	 */

	public abstract UssdNode[] getDynamicNodes(Map<String, Object> userData,
			Object currentInput);

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

	/**
	 * last message sent to client at the end of the session
	 * 
	 * @param allUserData
	 * @return
	 */

	public String generateSessionEndMessage(HashMap<String, Object> allUserData) {
		return "session ended";
	}

}
