package com.test.tcpserver.entity;

import java.io.Serializable;

/**
 * GPSʵ��
 * @author lianliang
 */
public class GpsData implements Serializable{
	
	private static final long serialVersionUID = -1238643831620162930L;
	
	private String obdTime; //obdʱ��
	private String recvTime; //��¡ƽ̨����ʱ��
	
	private int posState; //��λ״̬ 
	private float longitude; //����
	private float latitude; //γ��
	private float gpsSpeed; //GPS�ٶ�
	private float direction; //����
	private int accState; //ACC״̬
	private float altitude;//���θ߶�
	private int posStar;//��λ����
	private float obdSpeed;//OBD�ٶ�
	
	
	
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
