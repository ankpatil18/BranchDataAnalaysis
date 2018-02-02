package com.data.analysis;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
	
	public List<CallLogs> callLogsList= new ArrayList<CallLogs>();
	public List<Contacts> contactList = new ArrayList<Contacts>();
	public List<SMSLogs> smsLogsList = new ArrayList<SMSLogs>();
	public LoanPaid loanPaid;
	public Integer NumberOfContacts;
	public Long numberOfSMStoMPESA;
	public Long numberOfSMStoSAFARI;
	public Long numberofCallDayTime;
	public Long numberOfOutgoingCallDayTimeCall;
	

}
