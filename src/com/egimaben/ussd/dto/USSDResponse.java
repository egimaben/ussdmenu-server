package com.egimaben.ussd.dto;

public class USSDResponse {
	private String TransactionId;
	private String TransactionTime;
	private String USSDResponseString;
	private ACTION USSDAction;
	public enum ACTION {END,REQUEST};

	public USSDResponse() {

	}

	public String getTransactionId() {
		return TransactionId;
	}

	public void setTransactionId(String transactionId) {
		TransactionId = transactionId;
	}

	public String getTransactionTime() {
		return TransactionTime;
	}

	public void setTransactionTime(String transactionTime) {
		TransactionTime = transactionTime;
	}

	public String getUSSDResponseString() {
		return USSDResponseString;
	}

	public void setUSSDResponseString(String uSSDResponseString) {
		USSDResponseString = uSSDResponseString;
	}

	public ACTION getUSSDAction() {
		return USSDAction;
	}

	public void setUSSDAction(ACTION uSSDAction) {
		USSDAction = uSSDAction;
	}

	@Override
	public String toString() {
		return "USSDResponse [TransactionId=" + TransactionId
				+ ", TransactionTime=" + TransactionTime
				+ ", USSDResponseString=" + USSDResponseString
				+ ", USSDAction=" + USSDAction + "]";
	}

}
