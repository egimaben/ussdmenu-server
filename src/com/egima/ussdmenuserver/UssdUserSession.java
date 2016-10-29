package com.egima.ussdmenuserver;

import java.util.HashMap;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class stores information about a user's session and helps in navigating
 * the menu tree
 * 
 * @author Egima
 * 
 */
public class UssdUserSession {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdUserSession.class);
	public void log(String str) {
		LOGGER.info(str);
	}
	// current node in the menu tree
	private UssdNode currentNode;
	IStack stack = new IStack();
	// Ussd display can only accomodate
	private int index = 0;
	private UssdTree myTree;
	private HashMap<String, Object> userData = new HashMap<>();

	/**
	 * returns the Menu tree
	 * 
	 * @return
	 */
	public UssdTree getMyTree() {
		return myTree;
	}

	/**
	 * sets menu tree for this session
	 * 
	 * @param myTree
	 */
	public void setMyTree(UssdTree myTree) {
		this.myTree = myTree;
	}

	public boolean hasData(String key) {
		return userData.containsKey(key);
	}

	public boolean hasExtraData(String key) {
		return userData.containsKey(key + "-extras");
	}

	public UssdUserSession() {

	}

	/**
	 * is the current node a promp or a normal selection node
	 * 
	 * @return
	 */
	public boolean isPrompt() {
		return currentNode instanceof UssdPrompt;
	}

	/**
	 * return all the data accumulated over the course of this session from the
	 * user selections
	 * 
	 * @return
	 */
	public HashMap<String, Object> getAllUserData() {
		return userData;
	}

	/**
	 * load a new node based on the user choice and set it as currentnode
	 * 
	 * @param node
	 */
	public void changeNode(String node) {
		setIndex(0);
		String currName = currentNode.getName();
		stack.push(currName);
		UssdNode newNode = getMyTree().getNode(node);
		if (!(newNode instanceof UssdPrompt) && newNode.getMenu().length == 1) {
			newNode = getMyTree().getNode(newNode.getMenu()[0]);

		}
		if (newNode instanceof UssdPrompt) {
			UssdPrompt prompt = (UssdPrompt) newNode;
			if (prompt.isDynamic()) {
				Object data = userData.get(currName);
				if (data instanceof String[]) {
					String[] savedValues = (String[]) data;
					String dynamicChild = savedValues[savedValues.length - 1];
					UssdNode dynamicNode = getMyTree().getNode(dynamicChild);
					prompt.setChildren(dynamicNode.getMenu());
				}
			}
			setCurrentNode(prompt);
		} else
			setCurrentNode(newNode);
	}

	public void loadPrevNode() {

		try {
			UssdNode node = getMyTree().getNode(stack.peek());
			if (node.isMultiSelect()) {
				String name = node.getName();
				String[] data = (String[]) userData.get(name);
				String toRm = data[data.length - 1];
				data = Application.removeElements(data, toRm);
				userData.put(name, data);

			}
			setCurrentNode(getMyTree().getNode(stack.pop()));
		} catch (NoSuchElementException e) {
			return;
		}
		setIndex(0);
	}

	/**
	 * format the current node items for display
	 * 
	 * @return
	 */
	public String fetchDisplay() {	
		return currentNode.toString();
	}

	/**
	 * return the current node
	 * 
	 * @return
	 */
	public UssdNode getCurrentNode() {
		return currentNode;
	}

	/**
	 * set the current node
	 * 
	 * @param currentNode
	 */
	public void setCurrentNode(UssdNode currentNode) {
		this.currentNode = currentNode;
	}

	/**
	 * return the index of the current chunk of display data
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * set the current index for the chunk to display
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * add data to session storage, mostly from user selection
	 * 
	 * @param key
	 * @param value
	 */
	public void addData(String key, Object value) {
		userData.put(key, value);
	}

	public void addExtraData(String key, Object value) {
		userData.put(key + "-extras", value);
	}

	/**
	 * get session data by key
	 * 
	 * @param key
	 * @return
	 */
	public Object getData(String key) {
		return userData.get(key);
	}

	public Object getExtraData(String key) {
		return userData.get(key + "-extras");
	}

	/**
	 * load a session for the current user by setting a menu tree
	 * 
	 * @param myTree2
	 */
	public void initUser(UssdTree myTree2) {
		setMyTree(myTree2);
		setCurrentNode(getMyTree().getNode("root"));
	}

}
