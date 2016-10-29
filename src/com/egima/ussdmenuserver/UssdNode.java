package com.egima.ussdmenuserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * each item in a ussd menu is an instance of this class
 * 
 * @author new
 * 
 */
public class UssdNode {
	private String name;
	private String title;
	private String parent;
	private String address;
	private String childrenAlias = null;
	private String titlePrefix = null;
	private Object secondaryData;
	private int myBufferLimit = 0;
	private boolean allowDuplicate;
	private String id;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	private HashMap<String,Object> extraData=new HashMap<>();
	public void addExtraData(String key,Object value){
		extraData.put(key, value);
	}
	public Object getExtraData(String key){
		return extraData.get(key);
	}

	public boolean allowsDuplicate() {
		return allowDuplicate;
	}

	public void setAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
	}

	public int getMyBufferLimit() {
		return myBufferLimit;
	}

	public void setMyBufferLimit(int myBufferLimit) {
		this.myBufferLimit = myBufferLimit;
	}

	private boolean usingSecondaryData = false;

	public Object getSecondaryData() {
		return secondaryData;
	}

	public void setSecondaryData(Object secondaryData) {
		this.secondaryData = secondaryData;
	}

	public boolean isUsingSecondaryData() {
		return usingSecondaryData;
	}

	public void setUsingSecondaryData(boolean usingSecondaryData) {
		this.usingSecondaryData = usingSecondaryData;
	}

	public String getTitlePrefix() {
		return titlePrefix == null ? "" : titlePrefix + "-";
	}

	public void setTitlePrefix(String titlePrefix) {
		this.titlePrefix = titlePrefix;
	}

	private String titleSuffix = null;

	public String getTitleSuffix() {
		return titleSuffix == null ? "" : "-" + titleSuffix;
	}

	public void setTitleSuffix(String titleSuffix) {
		this.titleSuffix = titleSuffix;
	}

	public String getChildrenAlias() {
		return childrenAlias;
	}

	public void setChildrenAlias(String childrenAlias) {
		this.childrenAlias = childrenAlias;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	private int index = 0;
	private List<String> children = new ArrayList<>();
	private String multiSelectChild = null;

	public String getMultiSelectChild() {
		return multiSelectChild;
	}

	public void setMultiSelectChild(String multiSelectChild) {
		this.multiSelectChild = multiSelectChild;
	}

	private boolean multiSelect = false;

	/**
	 * returns true if multiple selection can be made for this node
	 * 
	 * @return
	 */
	public boolean isMultiSelect() {
		return multiSelect;
	}

	/**
	 * determines if this node accepts multiple selections delimited by the
	 * DELIM global property, which if not provided defaults to a comma
	 * 
	 * @param multiSelect
	 */
	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdNode.class);

	public void log(String str) {
		LOGGER.info( str);
	}

	/**
	 * constructor
	 * 
	 * @param title
	 *            text displayed at the top of this item's child items
	 * @param name
	 *            a unique reference to this item
	 * @param parent
	 *            the name of the item under which this item should appear in
	 *            the menu tree
	 */
	public UssdNode(String title, String name, String parent) {
		setName(name);
		setTitle(title);
		setParent(parent);
	}

	public UssdNode() {
	}

	/**
	 * add a child item to this menu item by name
	 * 
	 * @param childName
	 */
	public void addChild(String childName) {
		children.add(childName);
	}

	/**
	 * return the name used to reference this item
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the child of this node from a given index
	 * 
	 * @param index
	 *            index at which the child is located
	 * @return
	 */
	public String getNameFromIndex(int index) {
		return children.get(index - 1);
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 
	 * @return
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * 
	 * @param parent
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * get an array of the children of this node
	 * 
	 * @return
	 */
	public String[] getMenu() {
		return children.toArray(new String[0]);
	}

	/**
	 * get the index for the current chunk of data from this node
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * set a new index for the next chunk of data to fetch from this node
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * this method is called at the end of the session
	 * 
	 * @param userData
	 * @return
	 */
	public String processNodeEndEvent(HashMap<String, Object> userData) {
		return "Thank you, good bye";
	}

	/**
	 * return true if this node has any descendants
	 * 
	 * @return
	 */
	public boolean hasChildren() {
		return getMenu().length > 0;
	}

	public void addChildren(UssdNode... nodes) {
		for (UssdNode node : nodes)
			addChild(node.getName());
	}

	public void setChildren(UssdNode... nodes) {
		children.clear();
		addChildren(nodes);
	}

	public void setChildren(String... nodes) {
		children.clear();
		for (String node : nodes)
			addChild(node);
	}

	/**
	 * set the index of this node to 0, so that navigation through its children
	 * starts from the first chunk
	 */
	public void releaseObject() {
		setIndex(0);
	}

	@Override
	public String toString() {
		String objectString = null;

		String[] items = getMenu();
		int bufferLimit = (items.length == 0) ? 1 : getBufferLimit() + 1;

		do {
			bufferLimit -= 1;
			objectString = recurseMenu(items, bufferLimit);
		} while (objectString.length() > Application.USSD_CHAR_LIMIT);
		index = bufferLimit;
		return objectString;
	}

	public String recurseMenu(String[] items, int bufferLimit) {
		String objectString = getTitlePrefix() + getTitle() + getTitleSuffix();
		boolean lastMenu = false;
		if (items.length > 0) {
			for (int i = index; i < bufferLimit; i++) {
				String item = items[i];
				int num = i + 1;
				log("getting node for address: "+getAddress());
				UssdNode node=Application.userSessions.get(getAddress()).getMyTree().getNode(item);
				log("node got");
				String title=node.getTitle();
				objectString += Application.SEP + num + "."
						+ title;
			}
		}
		else objectString+=Application.SEP+Application.SEP+"NO DATA AVAILABLE, TRY AGAIN LATER"+Application.SEP;
		lastMenu = bufferLimit == items.length;
		objectString += Application.SEP + "0.Exit";
		if (!getParent().equals("0")) {
			objectString += Application.SEP + "#.Back";
			if (!lastMenu)
				objectString += getChildrenAlias() == null ? Application.SEP
						+ "00.More(" + (items.length - bufferLimit) + ")items"
						: Application.SEP + "00.More(" + (items.length - index)
								+ ")" + getChildrenAlias();

		}
		String vars = Application.replaceTitleVars(objectString,getAddress());
		return vars;
	}

	/**
	 * return how many items can be displayed on the ussd screen at once
	 */
	private int getBufferLimit() {
		// what is the length of the menu
		int len = getMenu().length;
		/*
		 * index is the cursor position in the menu, margin is how many items
		 * remain till last menu item if margin is same as menu length, that
		 * means we are serving our first menu page
		 */
		int margin = len - index;
		/*
		 * 
		 */
		int limit = getMyBufferLimit() > 0 ? getMyBufferLimit()
				: Application.bufferLimit;
		if (margin < limit)
			return index + margin;
		else
			return (index + limit);
	}

	/**
	 * add children to this node
	 * 
	 * @param children
	 */
	public void addChildren(String... children) {
		for (String child : children)
			addChild(child);

	}

}
