package com.egima.ussdmenuserver;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that organizes USSD menu items in a tree structure. There is a root
 * node which parents other nodes which can parent others in any depth. Nodes
 * are accessed by their names. They also have titles for display
 * 
 * @author Egima
 * 
 */
public class UssdTree implements Cloneable {
	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	private String notReadyMessage = "Application not ready, try again later";
	private String address=null;

	public String getNotReadyMessage() {
		return notReadyMessage;
	}

	public void setNotReadyMessage(String notReadyMessage) {
		this.notReadyMessage = notReadyMessage;
	}

	HashMap<String, UssdNode> treeMenu = new HashMap<>();
	private boolean ready = false;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdTree.class);

	public void log(String str) {
		LOGGER.info(str);
	}

	@Override
	public UssdTree clone() throws CloneNotSupportedException {
		return (UssdTree) super.clone();
	}

	/**
	 * constructor to create new tree
	 * 
	 * @param treeHeader
	 *            the heading of the menu tree e.g. Welcome to mBank
	 */
	public UssdTree(String treeHeader,String address) {
		this.address=address;
		addNode( new UssdNode(treeHeader, "root", "0"));
	}

	/**
	 * adds an array of nodes to the tree
	 * 
	 * @param nodes
	 */
	public void addNode(UssdNode... nodes) {
		
		for (UssdNode node : nodes) {
			node.setAddress(address);
			treeMenu.put(node.getName(), node);
			String parent = node.getParent();
			if (treeMenu.containsKey(parent))
				treeMenu.get(parent).addChild(node.getName());
		}

	}

	/**
	 * returns a node in the tree
	 * 
	 * @param name
	 * @return
	 */
	public UssdNode getNode(String name) {
		UssdNode node = treeMenu.get(name);
		node.releaseObject();
		return node;
	}

	/**
	 * i still don't understand why i put this method here, but it sure conceals
	 * some bad practice
	 * 
	 * @param whereClause
	 * @param children
	 */
	public void addChildrenWithNegativeCriteria(String whereClause,
			String... children) {
		for (String str : treeMenu.keySet()) {
			UssdNode node = treeMenu.get(str);
			String parent = node.getParent();
			// String[] elims={whereClause,"0","root","account"}
			if (whereClause.equals(parent) || parent.equals("0")
					|| parent.equals("root"))
				continue;
			node.addChildren(children);
		}
	}
}
