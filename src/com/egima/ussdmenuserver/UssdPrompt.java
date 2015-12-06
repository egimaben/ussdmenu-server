package com.egima.ussdmenuserver;

import java.util.HashMap;

public abstract class UssdPrompt extends UssdNode {
	private Object response=null;
	public abstract String getValidationError();
	
	public Object getResponse() {
		return response;
	}
	public boolean setResponse(Object response) {
		if(validate(response)){
			this.response=response;
			return true;
		}
		return false;
	}
	public UssdPrompt(String title, String name, String parent) {
		super(title, name, parent);
	}
	@Override
	public String toString(){
		return getTitle();
	}
	public String getChild(){
		String[] children=getMenu();
		if(children.length==0)
			return null;
		else return children[0];
	}
	
	public abstract boolean validate(Object data);
	public String generateSessionEndMessage(HashMap<String, Object> allUserData) {
		// TODO Auto-generated method stub
		return "session ended";
	}
		
	

}
