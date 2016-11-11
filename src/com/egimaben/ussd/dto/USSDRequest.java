package com.egimaben.ussd.dto;

public class USSDRequest {
	@Override
	public String toString() {
		return "USSDRequest [TransactionId=" + TransactionId
				+ ", TransactionTime=" + TransactionTime + ", MSISDN=" + MSISDN
				+ ", USSDServiceCode=" + USSDServiceCode
				+ ", USSDRequestString=" + USSDRequestString + "]";
	}

	private String TransactionId;
	private String TransactionTime;
	private String MSISDN;
	private String USSDServiceCode;
	private String USSDRequestString;

	public USSDRequest() {

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

	public String getMSISDN() {
		return MSISDN;
	}

	public void setMSISDN(String mSISDN) {
		MSISDN = mSISDN;
	}

	public String getUSSDServiceCode() {
		return USSDServiceCode;
	}

	public void setUSSDServiceCode(String uSSDServiceCode) {
		USSDServiceCode = uSSDServiceCode;
	}

	public String getUSSDRequestString() {
		return USSDRequestString;
	}

	public void setUSSDRequestString(String uSSDRequestString) {
		USSDRequestString = uSSDRequestString;
	}

}
