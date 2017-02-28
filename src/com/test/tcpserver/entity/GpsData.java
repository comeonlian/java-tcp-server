package com.test.tcpserver.entity;

import java.io.Serializable;

/**
 * GPS实体
 * @author lianliang
 */
public class GpsData implements Serializable{
	
	private static final long serialVersionUID = -1238643831620162930L;
	
	private String obdTime; //obd时间
	private String recvTime; //科隆平台接收时间
	
	private int posState; //定位状态 
	private float longitude; //经度
	private float latitude; //纬度
	private float gpsSpeed; //GPS速度
	private float direction; //方向
	private int accState; //ACC状态
	private float altitude;//海拔高度
	private int posStar;//定位星数
	private float obdSpeed;//OBD速度
	
	
	
	public int getPosState() {
		return posState;
	}
	public void setPosState(int posState) {
		this.posState = posState;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getGpsSpeed() {
		return gpsSpeed;
	}
	public void setGpsSpeed(float gpsSpeed) {
		this.gpsSpeed = gpsSpeed;
	}
	public int getAccState() {
		return accState;
	}
	public void setAccState(int accState) {
		this.accState = accState;
	}
	public float getAltitude() {
		return altitude;
	}
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	public int getPosStar() {
		return posStar;
	}
	public void setPosStar(int posStar) {
		this.posStar = posStar;
	}
	public float getObdSpeed() {
		return obdSpeed;
	}
	public void setObdSpeed(float obdSpeed) {
		this.obdSpeed = obdSpeed;
	}
	public float getDirection() {
		return direction;
	}
	public void setDirection(float direction) {
		this.direction = direction;
	}
	public String getObdTime() {
		return obdTime;
	}
	public void setObdTime(String obdTime) {
		this.obdTime = obdTime;
	}
	public String getRecvTime() {
		return recvTime;
	}
	public void setRecvTime(String recvTime) {
		this.recvTime = recvTime;
	}
	@Override
	public String toString() {
		return "GpsData [obdTime=" + obdTime + ", recvTime=" + recvTime + ", posState=" + posState + ", longitude="
				+ longitude + ", latitude=" + latitude + ", gpsSpeed=" + gpsSpeed + ", direction=" + direction
				+ ", accState=" + accState + ", altitude=" + altitude + ", posStar=" + posStar + ", obdSpeed="
				+ obdSpeed + "]";
	}
	
}
