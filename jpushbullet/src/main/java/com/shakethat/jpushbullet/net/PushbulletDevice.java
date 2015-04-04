
package com.shakethat.jpushbullet.net;

import java.util.List;

public class PushbulletDevice{
   	private List<Devices> devices;
   	private List<Extras> shared_devices;

 	public List<Devices> getDevices(){
		return this.devices;
	}
	public void setDevices(List<Devices> devices){
		this.devices = devices;
	}
 	public List<Extras> getShared_devices(){
		return this.shared_devices;
	}
	public void setShared_devices(List<Extras> shared_devices){
		this.shared_devices = shared_devices;
	}
}
