package com.test.tcpserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.test.tcpserver.entity.GpsData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Э������
 * @author lianliang
 */
public class ParseProtocolData {
	
	private ParseProtocolDataAfter parseProtocolDataAfter = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private byte cmdFlag;
	private byte replyFlag;//�ظ����
	private String vCode = "";// v��
	private String deviceCode = "";// ����
	private ByteBuf buffer = Unpooled.buffer(1024 * 10);
	
	public ParseProtocolData(ParseProtocolDataAfter parseProtocolDataAfter){
		this.parseProtocolDataAfter = parseProtocolDataAfter;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	private byte[] getDataUnitMsg() {
		if (buffer.readableBytes() < 38) {
			return null;
		}

		// ��סԭʼλ��
		int mark0 = buffer.readerIndex();
		// ��֤ͷ��
		byte head = 0x23;
		if (head != (buffer.readByte()) || head != (buffer.readByte())) {
			return null;
		}
		int headIndex = buffer.readerIndex() - 2;

		cmdFlag = buffer.readByte();
		replyFlag = buffer.readByte();
		// v��
		byte[] vByte = new byte[17];
		buffer.readBytes(vByte);
		vCode = new String(vByte);
		// ����
		byte[] deviceCodeByte = new byte[13];
		buffer.readBytes(deviceCodeByte);
		deviceCode = new String(deviceCodeByte);
		buffer.readByte();// ����
		int len = buffer.readUnsignedShort();// ����

		// ���ݰ��Ƿ��㹻����
		if (buffer.readableBytes() < len + 1) {
			buffer.readerIndex(mark0);
			return null;
		}
		
		byte[] dataUnit = new byte[len];
		byte[] fullData = new byte[len + 38];

		// ������
		{
			buffer.readBytes(dataUnit);//��ȡ���ݵ�Ԫ
			
			buffer.readerIndex(headIndex);
			buffer.readBytes(fullData);
			
			byte sum = 0;
			for (int i = 2; i <= fullData.length - 2; i++) {
				sum ^= (byte) (fullData[i] & 0xff);
			}
			
			if(sum == fullData[fullData.length - 1]){
				return fullData;
			}else{
				return null;
			}
		}
	}

	public Object parse(Channel channel, ByteBuf sourceData) throws Exception {
		buffer.writeBytes(sourceData);
		
		byte[] fullData = getDataUnitMsg();
		if(fullData == null){
			return null;
		}
		//sourceData.readerIndex(0);
		
		if(cmdFlag == 1){
			parseLogin(channel, fullData);
		}else if(cmdFlag == 3){
			//����ʵʱ�������жϰ����ͣ����÷��� 
			parseGps(channel, fullData);
		}
		buffer.discardReadBytes();
		return null;
	}
	/**
	 * @param channel
	 * @param originData
	 * @throws Exception
	 */
	private void parseLogin(Channel channel, byte[] originData) throws Exception {
		if(replyFlag!=-2)
			return ;
		//������¼��Ϣ
		originData[3] = 0x01; //Ӧ���ʶ
		
		parseResponseTime(originData); //Ӧ��ʱ��
		
		byte code = transCheckCode(originData);
		originData[originData.length-1] = code;
		
		//System.out.println("-->��Ӧ��:"+byteToHexString(originData));
		// �ظ�
		channel.writeAndFlush(originData).sync();
		//parseProtocolDataAfter.onParsedLoginMsg(null);
	}
	
	/**
	 * @param channel
	 * @param sourceData
	 */
	private void parseGps(Channel channel, byte[] fullData) {
		ByteBuf tempBuffer = Unpooled.buffer(1024);
		tempBuffer.writeBytes(fullData);
		//����GPS
		tempBuffer.skipBytes(37);
		
		GpsData gps = new GpsData();
		byte[] obdTime = new byte[6];
		byte[] recvTime = new byte[6];
		
		tempBuffer.readBytes(obdTime);
		tempBuffer.readBytes(recvTime);
		
		tempBuffer.skipBytes(1);
		
		int posState = tempBuffer.readByte(); //��λ״̬
		float longitude = tempBuffer.readUnsignedInt(); //����
		float latitude = tempBuffer.readUnsignedInt(); //γ��
		float gpsSpeed = tempBuffer.readUnsignedShort(); //GPS�ٶ�
		float direction = tempBuffer.readUnsignedShort(); //����
		int accState = tempBuffer.readUnsignedByte(); //ACC״̬
		float altitude = tempBuffer.readUnsignedInt(); //���θ߶�
		int posStar = tempBuffer.readUnsignedByte(); //��λ����
		float obdSpeed = tempBuffer.readUnsignedShort(); //OBD�ٶ�
		
		gps.setPosStar(posState);
		longitude = (float) (longitude / Math.pow(10, 6));
		gps.setLongitude(longitude);
		latitude = (float) (latitude / Math.pow(10, 6));
		gps.setLatitude(latitude);
		gpsSpeed = gpsSpeed * 0.1f;
		gps.setGpsSpeed(gpsSpeed);
		gps.setDirection(direction);
		gps.setAccState(accState);
		altitude = altitude * 1.0f / 10;
		gps.setAltitude(altitude);
		gps.setPosStar(posStar);
		obdSpeed = obdSpeed * 1.0f;
		if(obdSpeed==0.0)
			gps.setObdSpeed(gpsSpeed);
		else
			gps.setObdSpeed(obdSpeed);
		gps.setObdTime(parseTime(obdTime));
		gps.setRecvTime(parseTime(recvTime));
		
		tempBuffer.release();
		tempBuffer = null;
//		//��������
		parseProtocolDataAfter.onParsedGps(gps);
	}
	
	
	/**
	 * ��������Ӧ��ʱ��
	 * @param originData
	 */
	private void parseResponseTime(byte[] originData) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		originData[37] = (byte) (calendar.get(Calendar.YEAR) - 2000);
		originData[38] = (byte) (calendar.get(Calendar.MONTH) + 1);
		originData[39] = (byte) calendar.get(Calendar.DAY_OF_MONTH); 
		originData[40] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		originData[41] = (byte) calendar.get(Calendar.MINUTE);
		originData[42] = (byte) calendar.get(Calendar.SECOND);
	}
	/**
	 * ת��ʱ��
	 * @param bytes
	 * @return
	 */
	private String parseTime(byte[] bytes){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, bytes[0]+2000);
		calendar.set(Calendar.MONTH, bytes[1]-1);
		calendar.set(Calendar.DAY_OF_MONTH, bytes[2]);
		calendar.set(Calendar.HOUR_OF_DAY, bytes[3]);
		calendar.set(Calendar.MINUTE, bytes[4]);
		calendar.set(Calendar.SECOND, bytes[5]);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * ����ת��У�������
	 * @param bytes
	 * @return
	 */
	private byte transCheckCode(byte[] bytes){
		byte sum = 0;
		for (int i = 2; i <= bytes.length - 2; i++) {
			sum ^= (byte) (bytes[i] & 0xff);
		}
		return sum;
	}
	
	public static String byteToHexString(byte[] bytes) {
		StringBuffer resStr = new StringBuffer();
		int len = bytes.length;
		for (int i = 0; i < len; i++) {
			if (i != (len - 1))
				resStr.append(String.format("%02x", bytes[i]) + " ");
			else
				resStr.append(String.format("%02x", bytes[i]));
		}
		return resStr.toString();
	}
	
}
