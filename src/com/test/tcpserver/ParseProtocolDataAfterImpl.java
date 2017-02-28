package com.test.tcpserver;

public class ParseProtocolDataAfterImpl extends ParseProtocolDataAfter{

	public Object onParsedLoginMsg(Object loginInfo){
		System.err.println("login:"+loginInfo);
		return null;
	}
	
	public Object onParsedGps(Object gpsInfo){
		System.err.println("gps:"+gpsInfo.toString());
		return null;
	}
}