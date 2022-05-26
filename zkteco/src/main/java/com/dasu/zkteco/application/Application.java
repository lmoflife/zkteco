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
	 * @desc ����¼���豸��Ų�ѯ�豸IP��ַ���˿ڣ��������豸
	 * @param args
	 * @author �Ź���
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
				System.out.println("�����뿼���豸��:");
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
					System.out.println("ϵͳ���ҵ��豸,IP��ַ:" + ip + ",�˿ں�:" + port);
					System.out.println("�豸������");
					connect = sdk.connect(zkem, ip, port);
					if (!connect) {
						System.out.println("�豸����ʧ�ܣ���ȷ���豸IP��ַ���˿��Ƿ������������ȷ��������");
					} else {
						System.out.println("�豸���ӳɹ�");
					}
				} else {
					System.out.println("��������豸����ϵͳ�в����ڣ�����������");
					deviceCode = "";
				}

			}
			
			if(Boolean.parseBoolean(prop.getProperty("loadData")))
			{
				List<Record> list = sdk.getRecords(zkem, prop.getProperty("userCode")
						, prop.getProperty("startTime"), prop.getProperty("endTime"));
				for (Record record : list) {
					System.out.println(record.getUserCode()+",ʱ�䣺"+record.getSignDate());
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
