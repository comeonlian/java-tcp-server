package com.test.tcpserver;

/**
 * ҵ�����
 * @author lianliang
 */
public abstract class ParseProtocolDataAfter {

	public abstract Object onParsedLoginMsg(Object loginInfo);
	
	public abstract Object onParsedGps(Object gpsInfo);
}