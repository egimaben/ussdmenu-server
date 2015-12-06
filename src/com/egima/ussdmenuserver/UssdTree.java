package com.egima.ussdmenuserver;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A class that organises USSD menu items in a tree structure. There is a root node
 * which parents other nodes which can parent others in any depth. Nodes are accessed by
 * their names. They also have titles for display
 * @author Egima
 *
 */
public class UssdTree implements Cloneable{
	HashMap<String,UssdNode> treeMenu=new HashMap<>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdTree.class);
	public void log(String str){
		LOGGER.info(">>>>"+str);
	}
	@Override
	public UssdTree clone() throws CloneNotSupportedException{
		return (UssdTree) super.clone();
	}
public UssdTree(String treeHeader) {
	treeMenu.put("root", new UssdNode(treeHeader, "root", "0"));
}
public void addNode(UssdNode... nodes){
	for(UssdNode node:nodes){
	treeMenu.put(node.getName(), node);
	String parent=node.getParent();
	if(treeMenu.containsKey(parent))
		treeMenu.get(parent).addChild(node.getName());
	}
	
}

public UssdNode getNode(String name){
	UssdNode node=treeMenu.get(name);
	node.releaseObject();
	return node;
}
public void addChildrenWithNegativeCriteria(String whereClause,String...children){
	for(String str:treeMenu.keySet()){
		UssdNode node=treeMenu.get(str);
		String parent=node.getParent();
//		String[] elims={whereClause,"0","root","account"}
		if(whereClause.equals(parent)||parent.equals("0")||parent.equals("root"))
			continue;
		node.addChildren(children);
	}
}
}
