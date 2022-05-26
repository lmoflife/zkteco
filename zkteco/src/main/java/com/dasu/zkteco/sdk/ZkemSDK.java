package com.dasu.zkteco.sdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.DispatchEvents;
import com.jacob.com.STA;
import com.jacob.com.Variant;

/**
 * ���ӿ��ڻ�����ȡ���������ڻ�����
 * @author LiangMeng
 * @date 2018/08/28
 */
public class ZkemSDK {
	
	public  ActiveXComponent getComponent()
	{
		 ActiveXComponent zkem = new ActiveXComponent("zkemkeeper.ZKEM.1");
		 return zkem;
	}
	
	/**
	 * ���ӿ��ڻ�
	 * @param address IP��ַ
	 * @param port    �˿ں�
	 * @return
	 */
	public boolean connect(ActiveXComponent zkem,String address, int port){
		return zkem.invoke("Connect_NET", address, port).getBoolean();
	}
	
	/**
	 * �Ͽ����ڻ�����
	 */
	public void disConnect(ActiveXComponent zkem){
		zkem.invoke("Disconnect");
	}
	
	/**
	 * ����
	 * @return
	 */
	public boolean openDoor(ActiveXComponent zkem){
		return Dispatch.call(zkem.getObject(), "ACUnlock",new Variant(1), new Variant(20)).getBoolean();
	}
	
	/**
	 * �����¼�����
	 */
	public void  regEvent(ActiveXComponent zkem){
		zkem.invoke("RegEvent", new Variant(1), new Variant(1));
    	zkem.invoke("ReadRTLog", new Variant(1));
    	zkem.invoke("GetRTLog", new Variant(1));
    	new DispatchEvents(zkem.getObject(), new SensorEvents());
    	new STA().doMessagePump();
	}
	
	/**
	 * @desc 获取人员某个时间段考勤数据
	 * @return
	 * @throws Exception
	 */
	public static List<Record> getRecords(ActiveXComponent zkem,String userCode
			,String startTime,String endTime) throws Exception{
		Variant dwMachineNumber = new Variant(1, true);//机器号
        Variant dwEnrollNumber = new Variant("10", true);
        Variant dwVerifyMode = new Variant(0 , true);
        Variant dwInOutMode = new Variant(0, true);
        Variant dwYear = new Variant(0, true);
        Variant dwMonth = new Variant(0, true);
        Variant dwDay = new Variant(0, true);
        Variant dwHour = new Variant(0, true);
        Variant dwMinute = new Variant(0, true);
        Variant dwSecond = new Variant(0, true);
        Variant dwWorkCode = new Variant(0, true);
        
        List<Record> strList = new ArrayList<Record>();
        boolean newresult = false;
        
        do {
            Variant vResult = Dispatch.call(zkem, "SSR_GetGeneralLogData", 
            		dwMachineNumber, 
            		dwEnrollNumber, 
            		dwVerifyMode, 
            		dwInOutMode, 
            		dwYear, 
            		dwMonth, 
            		dwDay, 
            		dwHour, 
            		dwMinute, 
            		dwSecond, 
            		dwWorkCode
            	);
            newresult = vResult.getBoolean();
            
            if (newresult) {
            	String month = dwMonth.getIntRef() + "";
                String day = dwDay.getIntRef() + "";
                if (dwMonth.getIntRef() < 10) {
                    month = "0" + dwMonth.getIntRef();
                }
                if (dwDay.getIntRef() < 10) {
                    day = "0" + dwDay.getIntRef();
                }
                String validDate = dwYear.getIntRef() + "-" + month + "-" + day+ " " + 
    					dwHour.getIntRef() + ":" + 
    					dwMinute.getIntRef() + ":" + 
    					dwSecond.getIntRef();;
                
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
                Date bt=sdf.parse(validDate);
                Date dStartTime = null;
                if(startTime!=null && !startTime.equals(""))
                	dStartTime = sdf.parse(startTime);
                Date dEndTime = null;
                if(endTime!=null && !endTime.equals(""))
                {
                	dEndTime = sdf.parse(endTime);
                }
                
                Record record = new Record();
                if(userCode !=null && !userCode.equals(""))
                {
                	if(dwEnrollNumber.getStringRef().equals(userCode))
                		record.setUserCode(dwEnrollNumber.getStringRef());
                }
                else
                {
                	record.setUserCode(dwEnrollNumber.getStringRef());
                }
                
                if(startTime!=null && !startTime.equals(""))
                {
                	if(bt.compareTo(dStartTime)>=0)
                    {
                		 if(endTime!=null && !endTime.equals(""))
                         {
                         	if(bt.compareTo(dEndTime)<0)
                         	{
                         		record.setSignDate(bt);
                         	}
                         }
                		 else
                		 {
                			 record.setSignDate(bt);
                		 }
                    }
                }
                else
                {
                	 if(endTime!=null && !endTime.equals(""))
                     {
                     	if(bt.compareTo(dEndTime)<0)
                     	{
                     		record.setSignDate(bt);
                     	}
                     }
            		 else
            		 {
            			 record.setSignDate(bt);
            		 }
                }
                if(record.getUserCode()!=null&&record.getSignDate()!=null)
                	strList.add(record);
               
            }
        } while (newresult == true);
        
        Collections.sort(strList,new Comparator<Record>() {

			@Override
			public int compare(Record o1, Record o2) {
				return o1.getSignDate().compareTo(o2.getSignDate());
			}
		});
        return strList;
		
	}
	
	
	
}
