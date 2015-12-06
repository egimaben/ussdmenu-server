package com.egima.ussdmenuserver;

import hms.sdp.ussd.MchoiceUssdException;
import hms.sdp.ussd.MchoiceUssdMessage;
import hms.sdp.ussd.MchoiceUssdTerminateMessage;
import hms.sdp.ussd.client.MchoiceUssdReceiver;
import hms.sdp.ussd.client.MchoiceUssdSender;

import java.util.HashMap;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UssdReceiver extends MchoiceUssdReceiver {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdReceiver.class);
	private MchoiceUssdSender ussdSender;
	private HashMap<String, UssdUserSession> userSessions = new HashMap<>();;

	public UssdReceiver() {

	}

	// client url
	// appid
	// app pasword
	// ussdtree
	// buffer limit
	// app shortcode
	//

	public abstract String getUssdClientUrl();

	public abstract String getUssdAppId();

	public abstract String getUssdAppPassword();

	public abstract UssdTree getUssdTree();

	public abstract int getBufferLimit();

	public abstract String getAppShortCode();

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			String url = getUssdClientUrl();
			if (url != null)
				Application.CLIENT_URL = getUssdClientUrl();
			String appid = getUssdAppId();
			if (appid != null)
				Application.APP_ID = appid;
			String pass = getUssdAppPassword();
			if (pass != null)
				Application.APP_PASSWORD = pass;
			Application.tree = getUssdTree();
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

	@Override
	public void onMessage(MchoiceUssdMessage arg0) {
		String address = arg0.getAddress();
		String message = arg0.getMessage();
		String convId = arg0.getConversationId();
		boolean validInput = validateRequest(address, message, convId);
		if (validInput) {
			if (isContinuation(address))
				handleContinuingRequests(address, message, convId);
			else
				initUserSession(address, convId);
		}
		//
	}

	public void addUserData(String address, String key, Object value) {
		userSessions.get(address).addData(key, value);
	}

	public void handleLeafNodeEvents(String address, Object message,
			String convId) {
		UssdPrompt prompt = (UssdPrompt) userSessions.get(address)
				.getCurrentNode();
		boolean valid = prompt.setResponse(message);
		if (valid) {
			addUserData(address, prompt.getName(), prompt.getResponse());
			if (prompt.hasChildren())
				handleChildBearingNode(prompt.getChild(), address, convId);
			else
				handleChildlessNode(address, convId);
		} else
			endSession(prompt.getValidationError(), address, convId);
	}

	public void handleChildlessNode(String address, String convId) {
		UssdUserSession sess = userSessions.get(address);
		UssdNode node = sess.getCurrentNode();
		String resp = node.processNodeEndEvent(sess.getAllUserData());
		endSession(resp, address, convId);
	}

	public void handleChildBearingNode(String newNode, String address,
			String convId) {
//		UssdNode newnode = new UssdNode();
//		newnode = Application.tree.getNode(newNode);
		userSessions.get(address).changeNode(newNode);
		String display = userSessions.get(address).fetchDisplay();
		sendMessage(display, address, convId, false);

	}

	public void handleContinuingRequests(String address, String message,
			String convId) {
		UssdUserSession sess = userSessions.get(address);
		if (sess.isPrompt()) {
			handleLeafNodeEvents(address, message, convId);
			return;
		} else {
			String currentName = sess.getCurrentNode().getName();
			String selectedNode = sess.getCurrentNode().getNameFromIndex(
					Application.toInt(message));
			addUserData(address, currentName, selectedNode);
			if (sess.getCurrentNode().hasChildren())
				handleChildBearingNode(selectedNode, address, convId);
			else
				handleChildlessNode(address, convId);
		}

	}

	public void initUserSession(String address, String convId) {
		UssdUserSession sess = new UssdUserSession();
		UssdTree myTree;
		try {
			 myTree = Application.tree.clone();
		} catch (CloneNotSupportedException e) {
			log("Clone not supported caught");
			e.printStackTrace();
			return;
		}
		sess.initUser(myTree);
		userSessions.put(address, sess);
		sendMessage(sess.fetchDisplay(), address, convId, false);
	}

	public boolean isContinuation(String addr) {
		return userSessions.containsKey(addr);
	}

	private boolean validateRequest(String address, String message,
			String convId) {
		// if new request must be shortcode
		// if new, is it appshortcode
		// if continuing, must not be shortcode
		// if 0, exit gracefully
		// if 00, return more
		// if continuing and
		if (!isContinuation(address) && !Application.isUssdCode(message)) {

			endSession("Invalid shortcode", address, convId);
			return false;
		}
		if (!isContinuation(address) && Application.notAppShortCode(message)) {
			endSession("Unknown application", address, convId);
			return false;
		}
		if (message.equals("0")) {
			endSession("Thank you,good bye", address, convId);
			return false;
		}
		if (message.equals("00")) {
			sendMessage(userSessions.get(address).fetchDisplay(), address,
					convId, false);
			return false;
		}
		if (isContinuation(address)) {
			if (Application.isUssdCode(message)) {
				endSession("expecting integer not shortcode", address, convId);
				return false;
			}
			if (!isControlSignal(message)
					&& !userSessions.get(address).isPrompt()) {
				int in = toInt(message);
				if (in < 0
						|| in > userSessions.get(address).getCurrentNode()
								.getMenu().length) {
					endSession("Invalid integer input", address, convId);
					return false;
				}
			}
		}

		return true;
	}

	private boolean isControlSignal(String message) {

		return message.equals("0") || message.equals("00");
	}

	public void endSession(String msg, String address, String convId) {
		userSessions.remove(address);
		sendMessage(msg, address, convId, true);
	}

	public int toInt(String str) {
		return Integer.parseInt(str);
	}

	public void sendMessage(String message, String address, String convId,
			boolean terminationSignal) {
		try {

			ussdSender.sendMessage(message == null ? "Invalid input, try again"
					: message, address, convId, terminationSignal);
		} catch (MchoiceUssdException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSessionTerminate(MchoiceUssdTerminateMessage arg0) {
		log("terminatio message received");
		log("Address of request " + arg0.getAddress());
		log("Conversation id" + arg0.getConversationId());
		log("Correlation id " + arg0.getCorrelationId());
		log("Termination version " + arg0.getVersion());
	}

	public void log(String str) {
		LOGGER.info(">>>>>>>>>" + str);
	}
}
