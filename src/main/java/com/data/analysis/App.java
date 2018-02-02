package com.data.analysis;

import java.util.List;
import java.util.Objects;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App {
	public static final int TOTAL_USERS = 400;
	public static final String PATH = "D:/BranchInterview/Branch_user_logs/user_logs/";
	public static final String BACKSLASH = "/";
	public static final String COLLATED_CALL_LOG_FILENAME = "collated_call_log.txt";
	public static final String COLLATED_SMS_LOG_FILENAME = "collated_sms_log.txt";
	public static final String COLLATED_CONTACT_LIST_FILENAME = "collated_contact_list.txt";
	public static final String USER_STATUS_FILENAME = "user_status.csv";
	public static final int NUMBER_OF_REPAID_OR_DEFAULTERS = 200;

	public static void main(String[] args) {
		Integer repaidAverageConatct = 0, defaulterAverageContact = 0;
		Long repaidAverageSMSToMpesa = 0l, defaulterAverageSMSToMpesa = 0l;
		Long repaidAverageSMSToSafari = 0l, defaulterAverageSMSToSafari = 0l;
		Long repaidAverageDayTimeCall = 0l, defaulterAverageDayTimeCall = 0l;
		Long repaidAverageOutgoinCallDayTimeCall = 0l, defaulterOutgoinCallDayTimeCall = 0l;
		
		List<UserProfile> userList = new ArrayList<UserProfile>();
		List<UserStatus> userStatusList = new ArrayList<UserStatus>();
		String userStatusFilename = PATH + USER_STATUS_FILENAME;

		try (Reader reader = Files.newBufferedReader(Paths.get(userStatusFilename));
				CSVReader csvReader = new CSVReader(reader);) {
			String[] nextRecord;
			nextRecord = csvReader.readNext();
			while ((nextRecord = csvReader.readNext()) != null) {
				UserStatus userStatus = new UserStatus();
				userStatus.user_id = nextRecord[0];
				userStatus.DateTime = nextRecord[1];
				userStatus.loanPaid = nextRecord[2].equals("repaid") ? LoanPaid.Repaid : LoanPaid.Deafulter;
				userStatusList.add(userStatus);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 1; i <= TOTAL_USERS; i++) {
			UserProfile user = new UserProfile();
			List<CallLogs> callLogsList = new ArrayList<CallLogs>();
			List<Contacts> contactList = new ArrayList<Contacts>();
			List<SMSLogs> smsLogs = new ArrayList<SMSLogs>();
			String username = "user-" + i;
			for (int j = 1; j <= 2; j++) {
				String device = "device-" + j;
				String filename = PATH + username + BACKSLASH + device + BACKSLASH + COLLATED_CALL_LOG_FILENAME;
				boolean existsfile = new File(filename).exists();
				if (existsfile) {
					callLogsList.addAll(ReadCallLogs(filename));
				}
				filename = PATH + username + BACKSLASH + device + BACKSLASH + COLLATED_CONTACT_LIST_FILENAME;
				existsfile = new File(filename).exists();
				if (existsfile) {
					contactList.addAll(ReadContactList(filename));
				}

				filename = PATH + username + BACKSLASH + device + BACKSLASH + COLLATED_SMS_LOG_FILENAME;
				existsfile = new File(filename).exists();
				if (existsfile) {
					smsLogs.addAll(ReadSMSLog(filename));
				}

			}
			user.callLogsList.addAll(callLogsList);
			user.contactList.addAll(contactList);
			user.smsLogsList.addAll(smsLogs);
			user.loanPaid = userStatusList.get(i - 1).loanPaid;
			user.NumberOfContacts = contactList.size();
			user.numberOfSMStoMPESA = smsLogs.stream().filter(sms -> sms.sms_address != null)
					.filter(sms -> sms.sms_address.equals("MPESA")).count();
			user.numberOfSMStoSAFARI = smsLogs.stream().filter(sms -> sms.sms_address != null)
					.filter(sms -> sms.sms_address.equals("Safaricom")).count();
			user.numberofCallDayTime = callLogsList.stream().filter(call -> call.datetime != null)
					.filter(call -> call.datetime.getHours() >= 6 && call.datetime.getHours() < 18).count();
			user.numberOfOutgoingCallDayTimeCall = callLogsList.stream().filter(call -> call.datetime != null)
					.filter(call -> call.call_type != null && !call.call_type.isEmpty())
					.filter(call -> call.datetime.getHours() >= 6 && call.datetime.getHours() < 18
							&& call.call_type.equals("1"))
					.count();

			// System.out.printf("\n Number OF Contact for user-%d : %d - %s
			// ",i, user.NumberOfContacts , user.loanPaid);
			// System.out.printf("\n Number OF SMS to MPESA for user-%d : %d -
			// %s ",i, user.numberOfSMStoMPESA , user.loanPaid);
			if (user.loanPaid.equals(LoanPaid.Repaid)) {
				repaidAverageConatct += user.NumberOfContacts;
				repaidAverageSMSToMpesa += user.numberOfSMStoMPESA;
				repaidAverageSMSToSafari += user.numberOfSMStoSAFARI;
				repaidAverageDayTimeCall += user.numberofCallDayTime;
				repaidAverageOutgoinCallDayTimeCall += user.numberOfOutgoingCallDayTimeCall;
			}
			if (user.loanPaid.equals(LoanPaid.Deafulter)) {
				defaulterAverageContact += user.NumberOfContacts;
				defaulterAverageSMSToMpesa += user.numberOfSMStoMPESA;
				defaulterAverageSMSToSafari += user.numberOfSMStoSAFARI;
				defaulterAverageDayTimeCall += user.numberofCallDayTime;
				defaulterOutgoinCallDayTimeCall += user.numberOfOutgoingCallDayTimeCall;

			}
			userList.add(user);
		}

		repaidAverageConatct = repaidAverageConatct / NUMBER_OF_REPAID_OR_DEFAULTERS;
		defaulterAverageContact = defaulterAverageContact / NUMBER_OF_REPAID_OR_DEFAULTERS;

		repaidAverageSMSToMpesa = repaidAverageSMSToMpesa / NUMBER_OF_REPAID_OR_DEFAULTERS;
		defaulterAverageSMSToMpesa = defaulterAverageSMSToMpesa / NUMBER_OF_REPAID_OR_DEFAULTERS;

		repaidAverageSMSToSafari = repaidAverageSMSToSafari / NUMBER_OF_REPAID_OR_DEFAULTERS;
		defaulterAverageSMSToSafari = defaulterAverageSMSToSafari / NUMBER_OF_REPAID_OR_DEFAULTERS;

		repaidAverageDayTimeCall = repaidAverageDayTimeCall / NUMBER_OF_REPAID_OR_DEFAULTERS;
		defaulterAverageDayTimeCall = defaulterAverageDayTimeCall / NUMBER_OF_REPAID_OR_DEFAULTERS;

		repaidAverageOutgoinCallDayTimeCall = repaidAverageOutgoinCallDayTimeCall / NUMBER_OF_REPAID_OR_DEFAULTERS;
		defaulterOutgoinCallDayTimeCall = defaulterOutgoinCallDayTimeCall / NUMBER_OF_REPAID_OR_DEFAULTERS;

		System.out.printf("\n repaidAverageConatct =%d ,deafulterAverageContact =%d ", repaidAverageConatct,
				defaulterAverageContact);
		System.out.printf("\n repaidAverageSMS =%d, defaulterAverageSMS =%d ", repaidAverageSMSToMpesa,
				defaulterAverageSMSToMpesa);
		System.out.printf("\n repaidAverageSMSSafari =%d, defaulterAverageSMSSafari =%d ", repaidAverageSMSToSafari,
				defaulterAverageSMSToSafari);
		System.out.printf("\n repaidAverageDayTimeCall =%d, defaulterAverageDayTimeCall =%d ", repaidAverageDayTimeCall,
				defaulterAverageDayTimeCall);
		System.out.printf("\n repaidAverageOutgoinCallDayTimeCall =%d , defaulterOutgoinCallDayTimeCall=%d ",
				repaidAverageOutgoinCallDayTimeCall, defaulterOutgoinCallDayTimeCall);

	}

	private static List<SMSLogs> ReadSMSLog(String filename) {
		JSONParser parser = new JSONParser();
		List<SMSLogs> smslogsList = new ArrayList<SMSLogs>();
		try {

			Object obj = parser.parse(new FileReader(filename));
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				SMSLogs smslogs = new SMSLogs();
				smslogs.contact_id = (Long) extractInfo(jsonObject, "contact_id");
				smslogs.datetime = (Long) extractInfo(jsonObject, "datetime");
				smslogs.item_id = (Long) extractInfo(jsonObject, "item_id");
				smslogs.message_body = (String) extractInfo(jsonObject, "message_body");
				smslogs.sms_address = (String) extractInfo(jsonObject, "sms_address");
				smslogs.sms_type = (Long) extractInfo(jsonObject, "sms_type");
				smslogsList.add(smslogs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return smslogsList;
	}

	private static Object extractInfo(JSONObject jsonObject, String ObjName) {
		return jsonObject.containsKey(ObjName) ? (Object) jsonObject.get(ObjName) : null;

	}

	private static List<Contacts> ReadContactList(String filename) {
		JSONParser parser = new JSONParser();
		List<Contacts> contactList = new ArrayList<Contacts>();
		try {
			Object obj = parser.parse(new FileReader(filename));
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.size(); i++) {
				try {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					Contacts contacts = new Contacts();
					PhoneNumber phoneNumber = new PhoneNumber();
					contacts.item_id = (Long) extractInfo(jsonObject, "item_id");
					contacts.times_contacted = (Long) extractInfo(jsonObject, "times_contacted");
					contacts.display_name = (String) extractInfo(jsonObject, "display_name");

					contacts.last_time_contacted = (Long) extractInfo(jsonObject, "last_time_contacted");
					JSONArray phoneNumberJsonArray = (JSONArray) extractInfo(jsonObject, "phone_numbers");
					JSONObject phoneNumberJsonObject = !phoneNumberJsonArray.isEmpty()
							? (JSONObject) phoneNumberJsonArray.get(0) : new JSONObject();
					phoneNumber.item_id = (Long) extractInfo(phoneNumberJsonObject, "item_id");
					phoneNumber.normalized_phone_number = (String) extractInfo(phoneNumberJsonObject,
							"normalized_phone_number");
					phoneNumber.phone_number = (String) extractInfo(phoneNumberJsonObject, "phone_number");
					contacts.phone_numbers = phoneNumber;
					contactList.add(contacts);
				} catch (NullPointerException npe) {
					continue;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return contactList;

	}

	@SuppressWarnings("deprecation")
	private static List<CallLogs> ReadCallLogs(String filename) {
		JSONParser parser = new JSONParser();
		List<CallLogs> callLogsList = new ArrayList<CallLogs>();

		try {

			Object obj = parser.parse(new FileReader(filename));

			JSONArray jsonArray = (JSONArray) obj;

			for (int i = 0; i < jsonArray.size(); i++) {
				CallLogs callLogs = new CallLogs();
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				callLogs.phone_number = (String) extractInfo(jsonObject, "phone_number");
				callLogs.geocoded_location = (String) extractInfo(jsonObject, "geocoded_location");
				callLogs.features_video = (Boolean) extractInfo(jsonObject, "features_video");
				callLogs.call_type = (String) extractInfo(jsonObject, "call_type");
				callLogs.cached_name = (String) extractInfo(jsonObject, "cached_name");
				callLogs.duration = (String) extractInfo(jsonObject, "duration");
				callLogs.data_usage = (Long) extractInfo(jsonObject, "data_usage");
				callLogs.country_iso = (String) extractInfo(jsonObject, "country_iso");
				String epcoh = (String) extractInfo(jsonObject, "datetime");
				callLogs.datetime = new Date(Long.valueOf(epcoh) * 1000);
				callLogs.item_id = (Long) extractInfo(jsonObject, "item_id");
				callLogs.is_read = (Boolean) extractInfo(jsonObject, "is_read");

				callLogsList.add(callLogs);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return callLogsList;

	}
}
