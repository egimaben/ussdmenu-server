package com.egima.ussdmenuserver;

import java.util.HashMap;

public class UssdUserSession {
	private UssdNode currentNode;
	private int index=0;
	private UssdTree myTree;
	public UssdTree getMyTree() {
		return myTree;
	}
	public void setMyTree(UssdTree myTree) {
		this.myTree = myTree;
	}
	private HashMap<String,Object> userData=new HashMap<>();
public UssdUserSession() {

}
public boolean isPrompt(){
	return currentNode instanceof UssdPrompt;
}
public HashMap<String,Object> getAllUserData(){
	return userData;
}
public void changeNode(String node){
	setIndex(0);
	setCurrentNode(getMyTree().getNode(node));
}
public String fetchDisplay(){
	return currentNode.toString();
}

public UssdNode getCurrentNode() {
	return currentNode;
}
public void setCurrentNode(UssdNode currentNode) {
	this.currentNode = currentNode;
}
public int getIndex() {
	return index;
}
public void setIndex(int index) {
	this.index = index;
}
public void addData(String key,Object value){
	userData.put(key, value);
}
public Object getData(String key){
	return userData.get(key);
}
public void initUser(UssdTree myTree2) {
	setMyTree(myTree2);
	setCurrentNode(getMyTree().getNode("root"));
}
}
