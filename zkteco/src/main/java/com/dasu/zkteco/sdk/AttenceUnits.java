package com.dasu.zkteco.sdk;

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
import com.dasu.core.attence.server.IDepartDutyConfigServer;
import com.dasu.core.attence.server.IDeviceServer;
import com.dasu.core.attence.server.IDutyServer;
import com.dasu.core.attence.server.ILeaveServer;
import com.dasu.core.attence.server.ISystemServer;
import com.dasu.core.attence.server.IUserAttenceServer;
import com.dasu.core.result.Result;
import com.dasu.zkteco.application.Application;

public class AttenceUnits {

	public void setAttence(String userCode,Calendar now) throws Exception
	{
		ApplicationContext ac = Application.ac;
    	User user = findUser(ac, userCode);
    	List configs = null;
    	if(user!=null)
    	{
    		String code = Application.deviceCode;//设备编号
    		IDutyServer dutyServer = (IDutyServer)ac.getBean("dutyService");
    		boolean duty = dutyServer.existDuty(user.getId(), now.getTime());
    		boolean match = matchUserAttence(ac,user.getId());
    			if(match)
    			{
    				if(duty)
    				{
    					IDepartDutyConfigServer dutyConfigServer = (IDepartDutyConfigServer)ac.getBean("dutyConfigServer");
    					configs = dutyConfigServer.selectDepartDutyByDepartCode(user.getDepCode());
    				}
    				else
    				{
    					configs = getDepartAttenceConfig(ac,user.getDepCode());
    				}
    				
    				if(configs!=null && configs.size()>0)
    				{
    					ISystemServer server = (ISystemServer)ac.getBean("systemService");
    					Calendar amEndTime = Calendar.getInstance();
    					Calendar pmEndTime = Calendar.getInstance();
    					Calendar amStartTime = Calendar.getInstance();
    					Calendar dutyStartTime = Calendar.getInstance();
    					Calendar dutyEndTime = Calendar.getInstance();
    					int year = now.get(Calendar.YEAR);
    					int mouth = (now.get(Calendar.MONTH) + 1);
    					int day = now.get(Calendar.DAY_OF_MONTH);
    					int hour = now.get(Calendar.HOUR_OF_DAY);
    					int mintue = now.get(Calendar.MINUTE);
    					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    					List<UserAttence> attences = getDayAttences(ac,now.getTime(), user.getId());
    					
    					amEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+server.getAmEnd()));
    					pmEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+server.getPmEnd()));
    					amStartTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+server.getAmStart()));
    					dutyStartTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+server.getDutyStart()));
    					dutyEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+server.getDutyEnd()));
    					
    					ILeaveServer leaveServer = (ILeaveServer)ac.getBean("leaveService");
    					List<Leave> leaveList = leaveServer.selectLeavesByUserId(now.getTime()
    							,user.getId());
    					
    					String pmStartTime = server.getPmSplit();
    					Date pmDate = sdf.parse(year+"-"+mouth+"-"+day+" "+pmStartTime);
    					DepAttenceConfig swqdConfig = null,swqtConfig=null,xwqdConfig=null
    							,xwqtConfig=null,zbqdConfig=null,zbqtConfig=null;
    					
    					for (Object config : configs) {
    						DepAttenceConfig depAttenceConfig = (DepAttenceConfig)config;
							if(depAttenceConfig.getType().equals("1"))
							{
								swqdConfig = depAttenceConfig;
								amStartTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
							else if(depAttenceConfig.getType().equals("2"))
							{
								swqtConfig = depAttenceConfig;
								amEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
							else if(depAttenceConfig.getType().equals("3"))
							{
								xwqdConfig = depAttenceConfig;
								pmDate = (sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
							else if(depAttenceConfig.getType().equals("4"))
							{
								xwqtConfig = depAttenceConfig;
								pmEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
							else if(depAttenceConfig.getType().equals("5"))
							{
								zbqdConfig = depAttenceConfig;
								dutyStartTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
							else if(depAttenceConfig.getType().equals("6"))
							{
								zbqtConfig = depAttenceConfig;
								dutyEndTime.setTime(sdf.parse(year+"-"+mouth+"-"+day+" "+depAttenceConfig.getWorkTime()));
							}
						}
    					
    					if(xwqdConfig!=null)
    					{
    						pmStartTime = xwqdConfig.getSignStart();
    						pmDate = sdf.parse(year+"-"+mouth+"-"+day+" "+pmStartTime);
    					}
    					
    					
    					
    					Date workDate = null;
    					Date lastDate = null;
    					UserAttence attence = null;
    					Date tempDate = null;
    					if(swqtConfig != null)
    					{
    						tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+swqtConfig.getSignEnd());
    					}
    					else
    					{
    						tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+server.getAmEnd());
    					}
    					
    					if(now.getTime().compareTo(tempDate)<0)//上午
    					{
    						match = false;
    						for (UserAttence userAttence : attences) {
								if(userAttence.getInOut().equals("1"))//有签到数据
								{
									match = true;
									break;
								}
							}
    						
    						if(match || swqdConfig == null) //有签到数据时
    						{
    							if(swqtConfig!=null) //有上午签退配置
    							{
    								match = false;
    								workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+swqtConfig.getWorkTime());
    								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+swqtConfig.getSignStart());
    								attence = new UserAttence();
    								attence.setUserId(user.getId());
    		    					attence.setType("1");//终端签到
    		    					attence.setAddress(code);//终端设备编号
    								attence.setWorkTime(workDate);
    								attence.setSignTime(now.getTime());
    								attence.setInOut("2");//签退
    								attence.setCreateUser(user.getId());
    								
    								if(now.getTime().compareTo(lastDate)<0)
    								{
    									match = false;
    									boolean temp = false;
    									Leave tempLeave = null;
    									for (Leave leave : leaveList) {
    										if(lastDate.compareTo(leave.getStartTime())>=0
    												&&lastDate.compareTo(leave.getEndTime())<=0)
    										{
    											match = false;
    											lastDate = leave.getStartTime();
    											temp = true;
    											break;
    										}
    										else if(lastDate.compareTo(leave.getEndTime())>0
    												&&now.getTime().compareTo(leave.getEndTime())<0)
    										{
    											tempLeave = leave;
    											match = true;
    											break;
    										}
    									}
    									
    									if(match)
    									{
    										if(now.getTime().compareTo(tempLeave.getStartTime())<0)
    										{
    											attence.setTimestamp((tempLeave.getStartTime().getTime()-now.getTime().getTime())
    													+(workDate.getTime()-tempLeave.getEndTime().getTime()));//设置早退时间
            									attence.setState("3");//早退
    										}
    										else if(now.getTime().compareTo(tempLeave.getEndTime())>0)
    										{
    											attence.setTimestamp(workDate.getTime()-now.getTime().getTime());//设置早退时间
            									attence.setState("3");//早退
    										}
    										else
    										{
    											attence.setTimestamp(workDate.getTime()-tempLeave.getEndTime().getTime());//设置早退时间
            									attence.setState("3");//早退
    										}
    									}
    									else
    									{
    										if(now.getTime().compareTo(lastDate)<0)
    	    								{
    											if(temp)
    											{
    												attence.setTimestamp(lastDate.getTime()-now.getTime().getTime());//设置早退时间
    											}
    											else
    											{
    												attence.setTimestamp(workDate.getTime()-now.getTime().getTime());
    											}
            									attence.setState("3");//早退
    	    								}
    										else
    										{
    											attence.setTimestamp((long)0);
    	    									attence.setState("1");//正常
    										}
    									}
    								}
    								else
    								{
    									attence.setTimestamp((long)0);
    									attence.setState("1");//正常
    								}
    							}
    						}
    						else //没有签到数据时
    						{
    							if(swqdConfig!=null) //有上午签到配置
    							{
    								if(now.getTime().compareTo(
    										sdf.parse(year+"-"+mouth+"-"+day+" "+swqdConfig.getSignStart()))>=0) //早于签到时间不计算
    								{
    									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+swqdConfig.getWorkTime());
        								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+swqdConfig.getSignEnd());
        								attence = new UserAttence();
        								attence.setUserId(user.getId());
        		    					attence.setType("1");//终端签到
        		    					attence.setAddress(code);//终端设备编号
        								attence.setWorkTime(workDate);
        								attence.setSignTime(now.getTime());
        								attence.setInOut("1");//签到
        								attence.setCreateUser(user.getId());
        								
        								if(now.getTime().compareTo(lastDate)>0)
        								{
        									match = false;
        									for (Leave leave : leaveList) {
        										if(lastDate.compareTo(leave.getStartTime())>=0
        												&&lastDate.compareTo(leave.getEndTime())<=0)
        										{
        											lastDate = leave.getEndTime();
        											match = true;
        											break;
        										}
        									}
        									
        									if(now.getTime().compareTo(lastDate)>0)
        									{
        										if(match)
        										{
        											attence.setTimestamp(now.getTime().getTime()-lastDate.getTime());//设置迟到时间
        											attence.setState("2");//迟到
        										}
        										else
        										{
        											attence.setTimestamp(now.getTime().getTime()-workDate.getTime());//设置迟到时间
        											attence.setState("2");//迟到
        										}
        										
        									}
        									else
        									{
        										attence.setTimestamp((long)0);
            									attence.setState("1");//正常
        									}
        								}
        								else
        								{
        									attence.setTimestamp((long)0);
        									attence.setState("1");//正常
        								}
    								}
    							}
    						}
    					}
    					else
    					{
    						if(!duty)
    						{
    							if(xwqtConfig != null)
    								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getSignEnd());
    							else
    								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+server.getPmEnd());
    							
    							if(now.getTime().compareTo(tempDate)<=0)//当前时间大于下午签退时间后不计算
    							{
    								match = false;
            						for (UserAttence userAttence : attences) {
        								if(userAttence.getInOut().equals("3"))//有签到数据
        								{
        									match = true;
        									break;
        								}
        							}
            						
            						if(match || xwqdConfig==null) //签退操作
            						{
            							if(xwqtConfig!=null) //有下午签退配置
            							{
            									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getWorkTime());
                								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getSignStart());
                								attence = new UserAttence();
                								attence.setUserId(user.getId());
                		    					attence.setType("1");//终端签到
                		    					attence.setAddress(code);//终端设备编号
                								attence.setWorkTime(workDate);
                								attence.setSignTime(now.getTime());
                								attence.setInOut("4");//签退
                								attence.setCreateUser(user.getId());
                								
                								if(now.getTime().compareTo(lastDate)<0)
                								{
                									match = false;
                									boolean temp = false;
                									Leave tempLeave = null;
                									for (Leave leave : leaveList) {
                										if(lastDate.compareTo(leave.getStartTime())>=0
                												&&lastDate.compareTo(leave.getEndTime())<=0)
                										{
                											match = false;
                											lastDate = leave.getStartTime();
                											temp = true;
                											break;
                										}
                										else if(lastDate.compareTo(leave.getEndTime())>0
                												&&now.getTime().compareTo(leave.getEndTime())<0)
                										{
                											tempLeave = leave;
                											match = true;
                											break;
                										}
                									}
                									
                									if(match)
                									{
                										if(now.getTime().compareTo(tempLeave.getStartTime())<0)
                										{
                											attence.setTimestamp((tempLeave.getStartTime().getTime()-now.getTime().getTime())
                													+(workDate.getTime()-tempLeave.getEndTime().getTime()));//设置早退时间
                        									attence.setState("3");//早退
                										}
                										else if(now.getTime().compareTo(tempLeave.getEndTime())>0)
                										{
                											attence.setTimestamp(workDate.getTime()-now.getTime().getTime());//设置早退时间
                        									attence.setState("3");//早退
                										}
                										else
                										{
                											attence.setTimestamp(workDate.getTime()-tempLeave.getEndTime().getTime());//设置早退时间
                        									attence.setState("3");//早退
                										}
                									}
                									else
                									{
                										if(now.getTime().compareTo(lastDate)<0)
                	    								{
                											if(temp)
                											{
                												attence.setTimestamp(lastDate.getTime()-now.getTime().getTime());//设置早退时间
                											}
                											else
                											{
                												attence.setTimestamp(workDate.getTime()-now.getTime().getTime());
                											}
                        									attence.setState("3");//早退
                	    								}
                										else
                										{
                											attence.setTimestamp((long)0);
                	    									attence.setState("1");//正常
                										}
                									}
                								}
                								else
                								{
                									attence.setTimestamp((long)0);
                									attence.setState("1");//正常
                								}
            								}
            						}
            						else //下午签到
            						{
            							if(xwqdConfig!=null) //有下午签到配置,下午签到
            							{
            								if(now.getTime().compareTo(
            										sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getSignStart()))>=0)//签到时间早上下午签到配置
            								{
            									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getWorkTime());
                								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getSignEnd());
                								attence = new UserAttence();
                								attence.setUserId(user.getId());
                		    					attence.setType("1");//终端签到
                		    					attence.setAddress(code);//终端设备编号
                								attence.setWorkTime(workDate);
                								attence.setSignTime(now.getTime());
                								attence.setInOut("3");//签到
                								attence.setCreateUser(user.getId());
                								
                								if(now.getTime().compareTo(lastDate)>0)
                								{
                									match = false;
                									for (Leave leave : leaveList) {
                										if(lastDate.compareTo(leave.getStartTime())>=0
                												&&lastDate.compareTo(leave.getEndTime())<=0)
                										{
                											lastDate = leave.getEndTime();
                											match = true;
                											break;
                										}
                									}
                									
                									if(now.getTime().compareTo(lastDate)>0)
                									{
                										if(match)
                										{
                											attence.setTimestamp(now.getTime().getTime()-lastDate.getTime());//设置迟到时间
                											attence.setState("2");//迟到
                										}
                										else
                										{
                											attence.setTimestamp(now.getTime().getTime()-workDate.getTime());//设置迟到时间
                											attence.setState("2");//迟到
                										}
                										
                									}
                									else
                									{
                										attence.setTimestamp((long)0);
                    									attence.setState("1");//正常
                									}
                								}
                								else
                								{
                									attence.setTimestamp((long)0);
                									attence.setState("1");//正常
                								}
            								}
            							}
            						}
    							}
    						}
    						else //当日人员值班
    						{
    							if(xwqtConfig != null)
    								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getSignEnd());
    							else if(zbqdConfig != null)
    								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+zbqdConfig.getSignStart());
    							else
    								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+server.getPmEnd());
    							
    							if(now.getTime().compareTo(tempDate)>0)//下班时间后
    							{
    								match = false;
            						for (UserAttence userAttence : attences) {
        								if(userAttence.getInOut().equals("5"))//有签到数据
        								{
        									match = true;
        									break;
        								}
        							}
            						
            						if(match || zbqdConfig == null) //有签到数据时或未配值班签时
            						{
            							if(zbqtConfig!=null) //有值班签退配置，签退
            							{
            								if(now.getTime().compareTo(
            										sdf.parse(year+"-"+mouth+"-"+day+" "+zbqtConfig.getSignEnd()))<=0)//超过值班签退时间后不计算
            								{
            									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+zbqtConfig.getWorkTime());
                								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+zbqtConfig.getSignStart());
                								attence = new UserAttence();
                								attence.setUserId(user.getId());
                		    					attence.setType("1");//终端签到
                		    					attence.setAddress(code);//终端设备编号
                								attence.setWorkTime(workDate);
                								attence.setSignTime(now.getTime());
                								attence.setInOut("6");//签退
                								attence.setCreateUser(user.getId());
                								
                								if(now.getTime().compareTo(lastDate)<0)
                								{
                									match = false;
                									boolean temp = false;
                									Leave tempLeave = null;
                									for (Leave leave : leaveList) {
                										if(lastDate.compareTo(leave.getStartTime())>=0
                												&&lastDate.compareTo(leave.getEndTime())<=0)
                										{
                											match = false;
                											lastDate = leave.getStartTime();
                											temp = true;
                											break;
                										}
                										else if(lastDate.compareTo(leave.getEndTime())>0
                												&&now.getTime().compareTo(leave.getEndTime())<0)
                										{
                											tempLeave = leave;
                											match = true;
                											break;
                										}
                									}
                									
                									if(match)
                									{
                										if(now.getTime().compareTo(tempLeave.getStartTime())<0)
                										{
                											attence.setTimestamp((tempLeave.getStartTime().getTime()-now.getTime().getTime())
                													+(workDate.getTime()-tempLeave.getEndTime().getTime()));//设置早退时间
                        									attence.setState("3");//早退
                										}
                										else if(now.getTime().compareTo(tempLeave.getEndTime())>0)
                										{
                											attence.setTimestamp(workDate.getTime()-now.getTime().getTime());//设置早退时间
                        									attence.setState("3");//早退
                										}
                										else
                										{
                											attence.setTimestamp(workDate.getTime()-tempLeave.getEndTime().getTime());//设置早退时间
                        									attence.setState("3");//早退
                										}
                									}
                									else
                									{
                										if(now.getTime().compareTo(lastDate)<0)
                	    								{
                											if(temp)
                											{
                												attence.setTimestamp(lastDate.getTime()-now.getTime().getTime());//设置早退时间
                											}
                											else
                											{
                												attence.setTimestamp(workDate.getTime()-now.getTime().getTime());
                											}
                        									attence.setState("3");//早退
                	    								}
                										else
                										{
                											attence.setTimestamp((long)0);
                	    									attence.setState("1");//正常
                										}
                									}
                								}
                								else
                								{
                									attence.setTimestamp((long)0);
                									attence.setState("1");//正常
                								}
            								}
            							}
            						}
            						else //没有签到数据时
            						{
            							if(zbqdConfig!=null) //有值班签到配置,签到
            							{
            								if(now.getTime().compareTo(
            										sdf.parse(year+"-"+mouth+"-"+day+" "+zbqdConfig.getSignStart()))>=0)//小于签到开始时间不计算
            								{
            									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+zbqdConfig.getWorkTime());
                								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+zbqdConfig.getSignEnd());
                								attence = new UserAttence();
                								attence.setUserId(user.getId());
                		    					attence.setType("1");//终端签到
                		    					attence.setAddress(code);//终端设备编号
                								attence.setWorkTime(workDate);
                								attence.setSignTime(now.getTime());
                								attence.setInOut("5");//签到
                								attence.setCreateUser(user.getId());
                								
                								if(now.getTime().compareTo(lastDate)>0)
                								{
                									match = false;
                									for (Leave leave : leaveList) {
                										if(lastDate.compareTo(leave.getStartTime())>=0
                												&&lastDate.compareTo(leave.getEndTime())<=0)
                										{
                											lastDate = leave.getEndTime();
                											match = true;
                											break;
                										}
                									}
                									
                									if(now.getTime().compareTo(lastDate)>0)
                									{
                										if(match)
                										{
                											attence.setTimestamp(now.getTime().getTime()-lastDate.getTime());//设置迟到时间
                											attence.setState("2");//迟到
                										}
                										else
                										{
                											attence.setTimestamp(now.getTime().getTime()-workDate.getTime());//设置迟到时间
                											attence.setState("2");//迟到
                										}
                										
                									}
                									else
                									{
                										attence.setTimestamp((long)0);
                    									attence.setState("1");//正常
                									}
                								}
                								else
                								{
                									attence.setTimestamp((long)0);
                									attence.setState("1");//正常
                								}
            								}
            							}
            						}
    							}
    							else //下班以前
    							{
    								if(xwqtConfig != null)
        								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getSignEnd());
        							else
        								tempDate = sdf.parse(year+"-"+mouth+"-"+day+" "+server.getPmEnd());
        							
        							if(now.getTime().compareTo(tempDate)<=0)//当前时间大于下午签退时间后不计算
        							{
        								match = false;
                						for (UserAttence userAttence : attences) {
            								if(userAttence.getInOut().equals("3"))//有签到数据
            								{
            									match = true;
            									break;
            								}
            							}
                						
                						if(match || xwqdConfig==null) //签退操作
                						{
                							if(xwqtConfig!=null) //有下午签退配置
                							{
                									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getWorkTime());
                    								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqtConfig.getSignStart());
                    								attence = new UserAttence();
                    								attence.setUserId(user.getId());
                    		    					attence.setType("1");//终端签到
                    		    					attence.setAddress(code);//终端设备编号
                    								attence.setWorkTime(workDate);
                    								attence.setSignTime(now.getTime());
                    								attence.setInOut("4");//签退
                    								attence.setCreateUser(user.getId());
                    								
                    								if(now.getTime().compareTo(lastDate)<0)
                    								{
                    									match = false;
                    									boolean temp = false;
                    									Leave tempLeave = null;
                    									for (Leave leave : leaveList) {
                    										if(lastDate.compareTo(leave.getStartTime())>=0
                    												&&lastDate.compareTo(leave.getEndTime())<=0)
                    										{
                    											match = false;
                    											lastDate = leave.getStartTime();
                    											temp = true;
                    											break;
                    										}
                    										else if(lastDate.compareTo(leave.getEndTime())>0
                    												&&now.getTime().compareTo(leave.getEndTime())<0)
                    										{
                    											tempLeave = leave;
                    											match = true;
                    											break;
                    										}
                    									}
                    									
                    									if(match)
                    									{
                    										if(now.getTime().compareTo(tempLeave.getStartTime())<0)
                    										{
                    											attence.setTimestamp((tempLeave.getStartTime().getTime()-now.getTime().getTime())
                    													+(workDate.getTime()-tempLeave.getEndTime().getTime()));//设置早退时间
                            									attence.setState("3");//早退
                    										}
                    										else if(now.getTime().compareTo(tempLeave.getEndTime())>0)
                    										{
                    											attence.setTimestamp(workDate.getTime()-now.getTime().getTime());//设置早退时间
                            									attence.setState("3");//早退
                    										}
                    										else
                    										{
                    											attence.setTimestamp(workDate.getTime()-tempLeave.getEndTime().getTime());//设置早退时间
                            									attence.setState("3");//早退
                    										}
                    									}
                    									else
                    									{
                    										if(now.getTime().compareTo(lastDate)<0)
                    	    								{
                    											if(temp)
                    											{
                    												attence.setTimestamp(lastDate.getTime()-now.getTime().getTime());//设置早退时间
                    											}
                    											else
                    											{
                    												attence.setTimestamp(workDate.getTime()-now.getTime().getTime());
                    											}
                            									attence.setState("3");//早退
                    	    								}
                    										else
                    										{
                    											attence.setTimestamp((long)0);
                    	    									attence.setState("1");//正常
                    										}
                    									}
                    								}
                    								else
                    								{
                    									attence.setTimestamp((long)0);
                    									attence.setState("1");//正常
                    								}
                								}
                						}
                						else //下午签到
                						{
                							if(xwqdConfig!=null) //有下午签到配置,下午签到
                							{
                								if(now.getTime().compareTo(
                										sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getSignStart()))>=0)//签到时间早上下午签到配置
                								{
                									workDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getWorkTime());
                    								lastDate = sdf.parse(year+"-"+mouth+"-"+day+" "+xwqdConfig.getSignEnd());
                    								attence = new UserAttence();
                    								attence.setUserId(user.getId());
                    		    					attence.setType("1");//终端签到
                    		    					attence.setAddress(code);//终端设备编号
                    								attence.setWorkTime(workDate);
                    								attence.setSignTime(now.getTime());
                    								attence.setInOut("3");//签到
                    								attence.setCreateUser(user.getId());
                    								
                    								if(now.getTime().compareTo(lastDate)>0)
                    								{
                    									match = false;
                    									for (Leave leave : leaveList) {
                    										if(lastDate.compareTo(leave.getStartTime())>=0
                    												&&lastDate.compareTo(leave.getEndTime())<=0)
                    										{
                    											lastDate = leave.getEndTime();
                    											match = true;
                    											break;
                    										}
                    									}
                    									
                    									if(now.getTime().compareTo(lastDate)>0)
                    									{
                    										if(match)
                    										{
                    											attence.setTimestamp(now.getTime().getTime()-lastDate.getTime());//设置迟到时间
                    											attence.setState("2");//迟到
                    										}
                    										else
                    										{
                    											attence.setTimestamp(now.getTime().getTime()-workDate.getTime());//设置迟到时间
                    											attence.setState("2");//迟到
                    										}
                    										
                    									}
                    									else
                    									{
                    										attence.setTimestamp((long)0);
                        									attence.setState("1");//正常
                    									}
                    								}
                    								else
                    								{
                    									attence.setTimestamp((long)0);
                    									attence.setState("1");//正常
                    								}
                								}
                							}
                						}
        							}
    							}
    						}
    					}
    					
    					if(attence!=null)//有需要签到/签退的记录
    					{
    						IUserAttenceServer server1 = (IUserAttenceServer)ac.getBean("userAttenceService");
    						Result result = server1.intsertAttence(attence);
    						System.out.println(result.getMsg());
    					}
    					
    				}
    				else
    				{
    					System.out.println("人员所在部门考勤配置未完善");
    				}
    			}
    			else
    			{
    				System.out.println("警员免考勤或尚未注册考勤");
    			}
    	}
    	else
    	{
    		System.out.println("系统未找到对应警号("+userCode+")民警信息");
    	}
	}
	
	/**
	 * @desc 查检警员信息是否存在
	 * @param ac
	 * @param userCode
	 * @return
	 */
	private User findUser(ApplicationContext ac,String userCode)
	{
		IUserServer userServer = (IUserServer)ac.getBean("userService");
    	User user = userServer.getUserByCode(userCode);
    	return user;
	}
	
	/**
	 * @desc 检查警员所在部门是否参与考勤
	 */
	private boolean matchDepartAttence(ApplicationContext ac,String deviceCode
			,String depCode)
	{
		boolean match = false;
		IDeviceServer deviceServer = (IDeviceServer)ac.getBean("deviceService");
		List<Device> deps = deviceServer.selectListByDepartment(depCode);
		for (Device device : deps) {
			if(deviceCode.equals(device.getCode()))
			{
				match = true;
				break;
			}
		}
		return match;
	}
	
	/**
	 * @desc 检查人员是否进行考勤
	 */
	private boolean matchUserAttence(ApplicationContext ac,String userId)
	{
		IAttenceUserServer attenceServer = (IAttenceUserServer)ac.getBean("attenceUserService");
		UserAttenceVo vo = attenceServer.selectAttenceUserById(userId);
		if(vo!=null)
		{
			if(vo.getAttenceType().equals("1"))//正常考勤
				return true;
		}
		return false;
	}
	
	/**
	 * @desc 获取部门考勤配置信息
	 */
	private List<DepAttenceConfig> getDepartAttenceConfig(ApplicationContext ac
			,String depCode)
	{
		IDepartAttenceConfigServer server = (IDepartAttenceConfigServer)ac.getBean("departAttenceService");
		DepartAttenceVo vo = server.selectDepartAttenceByDepartCode(depCode);
		if(vo!=null)
		{
			return vo.getAttences();
		}
		return null;
	}
	
	/**
	 * @desc 获取部门考勤配置信息
	 */
	private List<UserAttence> getDayAttences(ApplicationContext ac,Date date
			,String userId)
	{
		IUserAttenceServer server = (IUserAttenceServer)ac.getBean("userAttenceService");
		return server.selectAttenceByDateAndUserId(date, userId);
	}
	
}
