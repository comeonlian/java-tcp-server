package com.test.tcpserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.test.tcpserver.entity.GpsData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * 协力解析
 * @author lianliang
 */
public class ParseProtocolData {
	
	private ParseProtocolDataAfter parseProtocolDataAfter = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private byte cmdFlag;
	private byte replyFlag;//回复标记
	private String vCode = "";// v码
	private String deviceCode = "";// 车机
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

		// 记住原始位置
		int mark0 = buffer.readerIndex();
		// 验证头部
		byte head = 0x23;
		if (head != (buffer.readByte()) || head != (buffer.readByte())) {
			return null;
		}
		int headIndex = buffer.readerIndex() - 2;

		cmdFlag = buffer.readByte();
		replyFlag = buffer.readByte();
		// v码
		byte[] vByte = new byte[17];
		buffer.readBytes(vByte);
		vCode = new String(vByte);
		// 车机
		byte[] deviceCodeByte = new byte[13];
		buffer.readBytes(deviceCodeByte);
		deviceCode = new String(deviceCodeByte);
		buffer.readByte();// 加密
		int len = buffer.readUnsignedShort();// 包长

		// 数据包是否足够包长
		if (buffer.readableBytes() < len + 1) {
			buffer.readerIndex(mark0);
			return null;
		}
		
		byte[] dataUnit = new byte[len];
		byte[] fullData = new byte[len + 38];

		// 正常包
		{
			buffer.readBytes(dataUnit);//读取数据单元
			
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
			//解析实时包，并判断包类型，调用方法 
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
		//解析登录信息
		originData[3] = 0x01; //应答标识
		
		parseResponseTime(originData); //应答时间
		
		byte code = transCheckCode(originData);
		originData[originData.length-1] = code;
		
		//System.out.println("-->响应包:"+byteToHexString(originData));
		// 回复
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
		//解析GPS
		tempBuffer.skipBytes(37);
		
		GpsData gps = new GpsData();
		byte[] obdTime = new byte[6];
		byte[] recvTime = new byte[6];
		
		tempBuffer.readBytes(obdTime);
		tempBuffer.readBytes(recvTime);
		
		tempBuffer.skipBytes(1);
		
		int posState = tempBuffer.readByte(); //定位状态
		float longitude = tempBuffer.readUnsignedInt(); //经度
		float latitude = tempBuffer.readUnsignedInt(); //纬度
		float gpsSpeed = tempBuffer.readUnsignedShort(); //GPS速度
		float direction = tempBuffer.readUnsignedShort(); //方向
		int accState = tempBuffer.readUnsignedByte(); //ACC状态
		float altitude = tempBuffer.readUnsignedInt(); //海拔高度
		int posStar = tempBuffer.readUnsignedByte(); //定位星数
		float obdSpeed = tempBuffer.readUnsignedShort(); //OBD速度
		
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
//		//其他操作
		parseProtocolDataAfter.onParsedGps(gps);
	}
	
	
	/**
	 * 重新设置应答时间
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
	 * 转换时间
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
	 * 数据转发校验码计算
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
