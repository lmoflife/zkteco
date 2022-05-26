package com.dasu.zkteco.sdk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.dasu.core.ams.bean.User;
import com.dasu.core.ams.server.IUserServer;
import com.dasu.core.attence.bean.DepAttenceConfig;
import com.dasu.core.attence.bean.Device;
import com.dasu.core.attence.bean.Leave;
import com.dasu.core.attence.bean.UserAttence;
import com.dasu.core.attence.model.vo.DepartAttenceVo;
import com.dasu.core.attence.model.vo.UserAttenceVo;
import com.dasu.core.attence.server.IAttenceUserServer;
import com.dasu.core.attence.server.IDepartAttenceConfigServer;
import com.dasu.core.attence.server.IDeviceServer;
import com.dasu.core.attence.server.ILeaveServer;
import com.dasu.core.attence.server.ISystemServer;
import com.dasu.core.attence.server.IUserAttenceServer;
import com.dasu.core.result.Result;
import com.dasu.core.utils.AttenceTools;
import com.dasu.zkteco.application.Application;
import com.jacob.com.Variant;

public class SensorEvents {
	

	public void OnConnected(Variant[] arge){
		System.out.println("当成功连接机器时触发该事件，无返回值====");
	}
	
	public void OnDisConnected(Variant[] arge){
		System.out.println("当断开机器时触发该事件，无返回值====");
	}
	
	public void OnAlarm(Variant[] arge){
		System.out.println("当机器报警时触发该事件===="+arge);
	}
	
    public void OnAttTransactionEx(Variant[] arge) throws Exception{
    	
    	ApplicationContext ac = Application.ac;
    	ISystemServer server = (ISystemServer)ac.getBean("systemService");
    	Calendar now = Calendar.getInstance();
		now.setTime(server.getServerDate());
		AttenceUnits unit = new AttenceUnits();
		unit.setAttence(String.valueOf(arge[0]), now);
		
    	for (int i = 0; i < arge.length; i++) {
			System.out.println(arge[i]);
		}
    	System.out.println("当验证通过时触发该事件====**"+arge);
	}
    
	public void OnEnrollFingerEx(Variant[] arge){
		System.out.println("登记指纹时触发该事件===="+arge.clone());
	}
	
	public void OnFinger(Variant[] arge){
		System.out.println("当机器上指纹头上检测到有指纹时触发该消息，无返回值");
	}
	
	public void OnFingerFeature(Variant[] arge){
		System.out.println("登记用户指纹时，当有指纹按下时触发该消息===="+arge);
	}
	
	public void OnHIDNum(Variant[] arge){
		System.out.println("当刷卡时触发该消息===="+arge);
	}
	
	public void OnNewUser(Variant[] arge){
		System.out.println("当成功登记新用户时触发该消息===="+arge);
	}
	
	public void OnVerify(Variant[] arge){
		System.out.println("当用户验证时触发该消息===="+arge);
	}
	
	public void OnWriteCard(Variant[] arge){
		System.out.println("当机器进行写卡操作时触发该事件===="+arge);
	}
	
	public void OnEmptyCard(Variant[] arge){
		System.out.println("当清空 MIFARE 卡操作时触发该事件===="+arge);
	}
	
	public void OnEMData(Variant[] arge){
		System.out.println("当机器向 SDK 发送未知事件时，触发该事件===="+arge);
	}
	

	
	
  
}