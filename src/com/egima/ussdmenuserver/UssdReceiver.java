package com.egima.ussdmenuserver;

import hms.sdp.ussd.MchoiceUssdException;
import hms.sdp.ussd.MchoiceUssdMessage;
import hms.sdp.ussd.MchoiceUssdTerminateMessage;
import hms.sdp.ussd.client.MchoiceUssdReceiver;
import hms.sdp.ussd.client.MchoiceUssdSender;
import hms.sdp.ussd.impl.UssdAoRequestMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an abstract servlet class to receive client requests, process them and return
 * response
 * 
 * @author Egima
 * 
 */
public abstract class UssdReceiver extends MchoiceUssdReceiver {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdReceiver.class);
	private MchoiceUssdSender ussdSender;

	public UssdReceiver() {

	}

	public abstract String getDelim();

	// client url
	// appid
	// app pasword
	// ussdtree
	// buffer limit
	// app shortcode
	//
	/**
	 * returns the url of the client
	 * 
	 * @return
	 */
	public abstract String getUssdClientUrl();

	/**
	 * returns the appid of the ussd application to process the request
	 * 
	 * @return
	 */
	public abstract String getUssdAppId();

	/**
	 * returns the password of the app
	 * 
	 * @return
	 */
	public abstract String getUssdAppPassword();

	/**
	 * the UssdTree object that serves all the menus to the client
	 * 
	 * @return
	 */
	public abstract UssdTree getUssdTree(String address);

	/**
	 * how many items should be displayed on the screen at once
	 * 
	 * @return
	 */
	public abstract int getBufferLimit();

	/**
	 * what short code is used to access this app
	 * 
	 * @return
	 */
	public abstract String getAppShortCode();

	/**
	 * method that initiates all data for the application
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			String delim = getDelim();
			if (delim != null)
				Application.DELIM = delim;
			String url = getUssdClientUrl();
			if (url != null)
				Application.CLIENT_URL = getUssdClientUrl();
			String appid = getUssdAppId();
			if (appid != null)
				Application.APP_ID = appid;
			String pass = getUssdAppPassword();
			if (pass != null)
				Application.APP_PASSWORD = pass;
			int buffer = getBufferLimit();
			if (buffer > 0)
				Application.bufferLimit = buffer;
			String shortCode = getAppShortCode();
			if (shortCode != null)
				Application.ussdCode = shortCode;
			ussdSender = new MchoiceUssdSender(Application.CLIENT_URL,
					Application.APP_ID, Application.APP_PASSWORD);
			// Application.tree = initBettingMenu();

		} catch (MchoiceUssdException e) {
			e.printStackTrace();
		}
	}

	/**
	 * callback method invoked when a message arrives from client
	 */
	@Override
	public void onMessage(MchoiceUssdMessage arg0) {
		String address = arg0.getAddress();
		String message = arg0.getMessage();
		String convId = arg0.getConversationId();
		log("incoming request, address:" + address + ",message:" + message
				+ ",conversation ID:" + convId);
		Object validationResult = validateRequest(address, message, convId);
		if (validationResult instanceof UssdAoRequestMessage) {
			UssdAoRequestMessage result = (UssdAoRequestMessage) validationResult;
			sendMessage(result);
			return;
		}
		boolean validInput = (boolean) validationResult;
		log(validInput ? "request is valid" : "request is invalid");
		if (validInput) {
			if (isContinuation(address)) {
				UssdAoRequestMessage result = handleContinuingRequests(address,
						message, convId);
				sendMessage(result);
				return;
			} else {
				UssdAoRequestMessage result = initUserSession(address, convId);
				sendMessage(result);
				return;
			}
		}
		// return null;
	}

	/**
	 * add session data to the session storage, probably based on a user
	 * selection
	 * 
	 * @param address
	 *            client address
	 * @param key
	 * @param value
	 */
	public void addUserData(String address, String key, Object value) {
		UssdUserSession sess = Application.userSessions.get(address);
		sess.addData(key, value);
		Application.userData = sess.getAllUserData();

	}

	public void addExtraUserData(String address, String key, Object value) {
		UssdUserSession sess = Application.userSessions.get(address);
		sess.addExtraData(key, value);
	}

	/**
	 * Get a ussdprompt object and fire its callback
	 * 
	 * @param address
	 *            client address
	 * @param message
	 *            data to process in the callback
	 * @param convId
	 *            the conversation id of this session
	 */
	public UssdAoRequestMessage handleLeafNodeEvents(String address,
			Object message, String convId) {
		// get existing session
		UssdUserSession sess = Application.userSessions.get(address);
		// retrieve the current selected node whose data has arrived
		UssdPrompt prompt = (UssdPrompt) sess.getCurrentNode();
		// validate input data
		boolean valid = prompt.setResponse(message);

		if (valid) {
			// now get the response for processing
			Object data = prompt.getResponse();
			// handle case where app specifies this node as multi-select
			if (prompt.isMultiSelect())
				data = createMultiSelectData(address, message, convId);
			// finally add data received under right menu node
			addUserData(address, prompt.getName(), data);
			if (prompt.isDynamic()) {
				Object obj = createMultiSelectExtras(address, message, convId);
				addExtraUserData(address, prompt.getName(), obj);
			}
			/*
			 * the normal behaviour of a prompt is to be a session ender, like
			 * askin for a pin to complete a transaction but sometimes, we have
			 * to ask for a some data before authenticating with pin, for
			 * example cash amount, then pin, so prompt particularly involves
			 * non multiple choice selections but rather user conceived input
			 * 
			 * the other side of a prompt is where we need to have a menu, but
			 * also need to select multiple children
			 * 
			 * here, we first handle a normal prompt item
			 */
			if (prompt.hasChildren() && !prompt.isMultiSelect()) {
				return handleChildBearingNode(prompt.getChild(), address,
						convId);
				/*
				 * then we handle one with menu that needs multiple selection
				 * and go ahead to first confirm that the multiselect child menu
				 * actually exists
				 */
			} else if (prompt.isMultiSelect()
					&& prompt.getMultiSelectChild() != null)
				return handleChildBearingNode(prompt.getMultiSelectChild(),
						address, convId);
			else {
				return handleChildlessNode(address, convId);
			}
		} else {
			// if input is invalid, end session with error
			return endSession(prompt.getValidationError(), address, convId);
		}
	}

	public Object createMultiSelectExtras(String address, Object message,
			String convId) {
		/*
		 * 
		 */
		UssdUserSession sess = Application.userSessions.get(address);
		UssdPrompt node = (UssdPrompt) sess.getCurrentNode();
		Object data = null;
		// split the selections into an array, remember they are integers
		// denoting options
		String[] items = ((String) message).split(Application.DELIM);
		// create another array to store the ids/names of these selections
		UssdNode[] resps = new UssdNode[items.length];
		for (int i = 0; i < resps.length; i++) {
			// get name/id of selected item
			String name = node.getNameFromIndex(Application.toInt(items[i]));
			// get the UssdNode object mapped to this id/name of selected index,
			// remember, this is not for purposes of sending
			// a new menu but rather to get its secondary data incase the node
			// specifies so.
			// ordinarily we save the name of the selected node as value for
			// current node, but sometimes a node specifies
			// that instead of saving its name, we should save a certain piece
			// of data
			UssdNode newNode = sess.getMyTree().getNode(name);
			resps[i] = newNode;
		}
		// assign our array of responses to data object
		data = resps;
		// if this session already contains data in the name of this current
		// node, simply append the new inputs
		if (sess.hasExtraData(node.getName())) {

			Object obj = sess.getExtraData(node.getName());
			if (obj instanceof UssdNode[]) {
				UssdNode[] existingVals = (UssdNode[]) obj;
				List<UssdNode> newList = new ArrayList<>(
						Arrays.asList(existingVals));
				newList.addAll(Arrays.asList(resps));
				data = newList.toArray(new UssdNode[0]);
			}
		}
		return data;
	}

	/**
	 * When multiple options are selected by user as allowed by app, call this
	 * method before persisting data
	 * 
	 * @param address
	 * @param message
	 * @param convId
	 * @return
	 */

	public Object createMultiSelectData(String address, Object message,
			String convId) {
		/*
		 * 
		 */
		UssdUserSession sess = Application.userSessions.get(address);
		UssdNode node = sess.getCurrentNode();
		Object data = null;
		// split the selections into an array, remember they are integers
		// denoting options
		String[] items = ((String) message).split(Application.DELIM);
		// create another array to store the ids/names of these selections
		String[] resps = new String[items.length];
		for (int i = 0; i < resps.length; i++) {
			// get name/id of selected item
			String name = node.getNameFromIndex(Application.toInt(items[i]));
			// get the UssdNode object mapped to this id/name of selected index,
			// remember, this is not for purposes of sending
			// a new menu but rather to get its secondary data incase the node
			// specifies so.
			// ordinarily we save the name of the selected node as value for
			// current node, but sometimes a node specifies
			// that instead of saving its name, we should save a certain piece
			// of data
			UssdNode newNode = sess.getMyTree().getNode(name);
			resps[i] = node.isUsingSecondaryData() ? ""
					+ newNode.getSecondaryData() : name;
		}
		// assign our array of responses to data object
		data = resps;
		// if this session already contains data in the name of this current
		// node, simply append the new inputs
		if (sess.hasData(node.getName())) {

			String[] existingVals = (String[]) sess.getData(node.getName());
			if (node.allowsDuplicate()) {
				List<String> newList = new ArrayList<>(
						Arrays.asList(existingVals));
				newList.addAll(Arrays.asList(resps));
				data = newList.toArray(new String[0]);
			} else {
				LinkedHashSet<String> newSet = new LinkedHashSet<>(
						Arrays.asList(existingVals));
				newSet.addAll(Arrays.asList(resps));
				data = newSet.toArray(new String[0]);
			}
		}
		return data;
	}

	/**
	 * process the selection of a childless node by the user, this usually
	 * terminates a session
	 * 
	 * @param address
	 *            client address
	 * @param convId
	 *            conversation id of the session
	 */
	public UssdAoRequestMessage handleChildlessNode(String address,
			String convId) {
		UssdUserSession sess = Application.userSessions.get(address);
		UssdNode node = sess.getCurrentNode();
		String resp = node.processNodeEndEvent(sess.getAllUserData());
		return endSession(resp, address, convId);
	}

	/**
	 * process the selection of a node by user, usually returns further child
	 * nodes of selected node
	 * 
	 * @param newNode
	 *            child node selected by client, its children will be loaded
	 * @param address
	 *            client address
	 * @param convId
	 *            conversation id of the session
	 */
	public UssdAoRequestMessage handleChildBearingNode(String newNode,
			String address, String convId) {
		Application.userSessions.get(address).changeNode(newNode);
		UssdUserSession sess = Application.userSessions.get(address);
		String display = sess.fetchDisplay();
		return sendMessage(display, address, convId, false);

	}

	/**
	 * this method is called everytime a non-session-end message is received
	 * from the client
	 * 
	 * @param address
	 *            client address
	 * @param message
	 *            data from client
	 * @param convId
	 *            conversation id of the session
	 */
	public UssdAoRequestMessage handleContinuingRequests(String address,
			String message, String convId) {
		UssdUserSession sess = Application.userSessions.get(address);
		if (sess.isPrompt()) {
			return handleLeafNodeEvents(address, message, convId);
			// return;
		} else {
			String currentName = sess.getCurrentNode().getName();
			String selectedNode = sess.getCurrentNode().getNameFromIndex(
					Application.toInt(message));

			if (Application.isCommand(selectedNode)) {
				String[] tokens = selectedNode.split("_");
				if (tokens[0].equals("%REV")) {
					String token = tokens[1];
					selectedNode = token.substring(0, token.indexOf("@"));
					return handleChildBearingNode(selectedNode, address, convId);
				}

			} else {
				addUserData(address, currentName, selectedNode);
				if (sess.getCurrentNode().hasChildren()) {
					return handleChildBearingNode(selectedNode, address, convId);
				} else {

					return handleChildlessNode(address, convId);
				}
			}
			return null;
		}

	}

	/**
	 * start a new ussd session for a client, after this operation, the client
	 * will receive the first menu
	 * 
	 * @param address
	 * @param convId
	 */
	public UssdAoRequestMessage initUserSession(String address, String convId) {
		UssdUserSession sess = new UssdUserSession();
		UssdTree myTree;
		myTree = getUssdTree(address);
		if (myTree.isReady()) {
			sess.initUser(myTree);
			Application.userSessions.put(address, sess);
			return sendMessage(sess.fetchDisplay(), address, convId, false);
		} else
			return endSession(myTree.getNotReadyMessage(), address, convId);
	}

	/**
	 * check if this is a new session or its a continuing session
	 * 
	 * @param addr
	 * @return
	 */
	public boolean isContinuation(String addr) {
		return Application.userSessions.containsKey(addr);
	}

	/**
	 * perform basic validation on message received from client, some rules
	 * include: a first session message must be a short code(specifically the
	 * correct one assigned to the app) and the rest must be integers. apart
	 * from node end events
	 * 
	 * @param address
	 * @param message
	 * @param convId
	 * @return
	 */
	private Object validateRequest(String address, String message, String convId) {
		// if new request must be shortcode
		// if new, is it appshortcode
		// if continuing, must not be shortcode
		// if 0, exit gracefully
		// if 00, return more
		// if continuing and
		if (!isContinuation(address) && !Application.isUssdCode(message)) {

			return endSession("Invalid shortcode", address, convId);
		}
		if (!isContinuation(address) && notAppShortCode(message)) {
			return endSession("Unknown application:" + message, address, convId);
		}
		if (message.equals("0")) {
			return endSession("Thank you,good bye", address, convId);
		}
		// pagination command, fetch the next page
		if (message.equals("00")) {
			return sendMessage(Application.userSessions.get(address)
					.fetchDisplay(), address, convId, false);
		}
		// back command
		if (message.equals("#")) {
			Application.userSessions.get(address).loadPrevNode();
			return sendMessage(Application.userSessions.get(address)
					.fetchDisplay(), address, convId, false);
		}
		if (isContinuation(address)) {
			if (Application.isUssdCode(message)) {
				return endSession("expecting integer not shortcode", address,
						convId);
			}
			if (!isControlSignal(message)
					&& !Application.userSessions.get(address).isPrompt()
					&& !Application.userSessions.get(address).getCurrentNode()
							.isMultiSelect()) {
				int in = toInt(message);
				if (in < 0
						|| in > Application.userSessions.get(address)
								.getCurrentNode().getMenu().length) {
					return endSession("Invalid integer input", address, convId);
				}
			}
			if (Application.userSessions.get(address).getCurrentNode()
					.isMultiSelect()
					&& !validMultiSelect(message)) {
				return endSession(
						"invalid multiple input, separate inputs using "
								+ Application.DELIM, address, convId);
			}
		}

		return true;
	}

	public boolean notAppShortCode(String message) {
		return !message.equals(getAppShortCode());
	}

	public boolean validMultiSelect(String msg) {
		String[] selections = msg.split(Application.DELIM);
		for (String str : selections) {
			if (Application.toInt(str) < 0)
				return false;
		}
		return true;
	}

	/**
	 * returns true if the message received is a navigation command
	 * 
	 * @param message
	 * @return
	 */
	private boolean isControlSignal(String message) {

		return message.equals("0") || message.equals("00");
	}

	/**
	 * terminates the current session, particularly when the user selectes the
	 * exit option
	 * 
	 * @param msg
	 * @param address
	 * @param convId
	 */
	public UssdAoRequestMessage endSession(String msg, String address,
			String convId) {
		if (Application.userSessions.containsKey(address))
			Application.userSessions.remove(address);
		UssdAoRequestMessage resp = new UssdAoRequestMessage();
		resp.setAddress(address);
		resp.setConversationId(convId);
		resp.setMessage(msg);
		resp.setSessionTermination(true);
		return resp;
		// sendMessage(msg, address, convId, true);
	}

	public int toInt(String str) {
		return Integer.parseInt(str);
	}

	/**
	 * sends a message from server to the client
	 * 
	 * @param message
	 * @param address
	 * @param convId
	 * @param terminationSignal
	 */
	public UssdAoRequestMessage sendMessage(String message, String address,
			String convId, boolean terminationSignal) {
		// try {
		UssdAoRequestMessage resp = new UssdAoRequestMessage();
		resp.setAddress(address);
		resp.setConversationId(convId);
		resp.setMessage(message == null ? "Invalid input, try again" : message);
		resp.setSessionTermination(false);
		return resp;

		// ussdSender.sendMessage(message == null ? "Invalid input, try again"
		// : message, address, convId, terminationSignal);
		// } catch (MchoiceUssdException e) {
		// e.printStackTrace();
		// }
	}

	public void sendMessage(UssdAoRequestMessage result) {
		try {

			ussdSender.sendMessage(result.getMessage(), result.getAddress(),
					result.getConversationId(), result.getSessionTermination());
		} catch (MchoiceUssdException e) {
			e.printStackTrace();
		}
	}

	/**
	 * invoked after the session is terminated
	 */
	@Override
	public void onSessionTerminate(MchoiceUssdTerminateMessage arg0) {
		String address = arg0.getAddress();
		String convId = arg0.getConversationId();
		// log("terminatio message received");
		// log("Address of request " + address);
		// log("Conversation id" + convId);
		// log("Correlation id " + arg0.getCorrelationId());
		// log("Termination version " + arg0.getVersion());
		endSession(
				"Dear customer, the session ended, you took too long to respond...please start another session",
				address, convId);
	}

	public void log(String str) {
		LOGGER.info(str);
	}
}
