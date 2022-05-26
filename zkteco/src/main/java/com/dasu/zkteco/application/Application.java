package com.dasu.zkteco.application;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baomidou.mybatisplus.service.IService;
import com.dasu.core.attence.bean.Device;
import com.dasu.core.attence.server.IDeviceServer;
import com.dasu.core.attence.server.ISystemServer;
import com.dasu.zkteco.sdk.AttenceUnits;
import com.dasu.zkteco.sdk.Record;
import com.dasu.zkteco.sdk.ZkemSDK;
import com.jacob.activeX.ActiveXComponent;

public class Application {

	public static ApplicationContext ac = null;

	public static String deviceCode = null;
	/**
	 * @desc 根据录入设备编号查询设备IP地址及端口，并启动设备
	 * @param args
	 * @author 张贵启
	 */
	public static void main(String[] args) {
		try
		{
			ac = new ClassPathXmlApplicationContext("classpath:/spring-context.xml");
			Properties prop = new Properties();
			prop.load(Application.class.getClass().getResourceAsStream("/system.properties"));
			deviceCode = prop.getProperty("deviceCode");
			String ip = "";
			int port = 0;
			ZkemSDK sdk = new ZkemSDK();
			ActiveXComponent zkem = sdk.getComponent();
			boolean connect = false;
			while (!connect) {
				System.out.println("请输入考勤设备号:");
				if(deviceCode == null || deviceCode.equals(""))
				{
					Scanner sc = new Scanner(System.in);
					deviceCode = sc.nextLine();
				}
				
				IDeviceServer deviceServer = (IDeviceServer) ac.getBean("deviceService");
				Device device = deviceServer.selectDeviceByCode(deviceCode);
				if (device != null) {
					ip = device.getIp();
					port = Integer.parseInt(device.getPort());
					System.out.println("系统查找到设备,IP地址:" + ip + ",端口号:" + port);
					System.out.println("设备连接中");
					connect = sdk.connect(zkem, ip, port);
					if (!connect) {
						System.out.println("设备连接失败，请确认设备IP地址及端口是否正常，如果正确，请重试");
					} else {
						System.out.println("设备连接成功");
					}
				} else {
					System.out.println("您输入的设备号在系统中不存在，请重新输入");
					deviceCode = "";
				}

			}
			
			if(Boolean.parseBoolean(prop.getProperty("loadData")))
			{
				List<Record> list = sdk.getRecords(zkem, prop.getProperty("userCode")
						, prop.getProperty("startTime"), prop.getProperty("endTime"));
				for (Record record : list) {
					System.out.println(record.getUserCode()+",时间："+record.getSignDate());
					AttenceUnits unit = new AttenceUnits();
					Calendar cal = Calendar.getInstance();
					cal.setTime(record.getSignDate());
					unit.setAttence(record.getUserCode(),cal);
				}
			}
			sdk.regEvent(zkem);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Scanner sc = new Scanner(System.in);
		}
		
	}

}
