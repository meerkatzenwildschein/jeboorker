
package com.shakethat.jpushbullet.net;


public class Extras{
   	private String android_version;
   	private String app_version;
   	private String manufacturer;
   	private String model;
   	private String sdk_version;

 	public String getAndroid_version(){
		return this.android_version;
	}
	public void setAndroid_version(String android_version){
		this.android_version = android_version;
	}
 	public String getApp_version(){
		return this.app_version;
	}
	public void setApp_version(String app_version){
		this.app_version = app_version;
	}
 	public String getManufacturer(){
		return this.manufacturer;
	}
	public void setManufacturer(String manufacturer){
		this.manufacturer = manufacturer;
	}
 	public String getModel(){
		return this.model;
	}
	public void setModel(String model){
		this.model = model;
	}
 	public String getSdk_version(){
		return this.sdk_version;
	}
	public void setSdk_version(String sdk_version){
		this.sdk_version = sdk_version;
	}
}
