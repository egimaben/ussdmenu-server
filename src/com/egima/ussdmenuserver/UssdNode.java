package com.egima.ussdmenuserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UssdNode {
	private String name;
	private String title;
	private String parent;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UssdNode.class);
	public void log(String str){
		LOGGER.info(">>>>>>"+str);
	}
	private List<String> children=new ArrayList<>();
	public UssdNode(String title, String name, String parent) {
		setName(name);
		setTitle(title);
		setParent(parent);
	}
	public UssdNode() {
	}
	public void addChild(String childName){
		children.add(childName);
	}
	public String getName() {
		return name;
	}
	public String getNameFromIndex(int index){
		return children.get(index-1);
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	public String[] getMenu() {
		return children.toArray(new String[0]);
	}
	private int index=0;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String processNodeEndEvent(HashMap<String,Object> userData){
		return "Thank you, good bye";
	}
	public boolean hasChildren(){
		return getMenu().length>0;
	}
	public void releaseObject(){
		setIndex(0);
	}
	@Override
	public String toString() {
		String objectString=getTitle();
		String[] items=getMenu();
		if(items.length==0)
			return null;
		int bufferLimit=getBufferLimit();
		for(int i=index;i<bufferLimit;i++){
			String item=items[i];
			int num=i+1;
			objectString+="<br>"+num+"."+Application.tree.getNode(item).getTitle();
					}
		index=bufferLimit;
		boolean lastMenu=index==getMenu().length;
		objectString+="<br>0.Exit<br>";
		if(!lastMenu)
		objectString+="00.More";
		return objectString;
	}
	private int getBufferLimit(){
		int len=getMenu().length;
		int margin=len-index;
		if(margin<Application.bufferLimit)
			return index+margin;
		else return (index+Application.bufferLimit);
	}
	public void addChildren(String...children){
		for(String child:children)
			addChild(child);
		
	}

}
