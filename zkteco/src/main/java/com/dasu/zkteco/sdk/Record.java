package com.dasu.zkteco.sdk;

import java.util.Date;

/**
 * @desc 考勤记录
 * @author DS_Json
 *
 */
public class Record {

	private String userCode;//用户ID
	private Date signDate;//签到时间
	
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public Date getSignDate() {
		return signDate;
	}
	public void setSignDate(Date signDate) {
		this.signDate = signDate;
	}
	
	
}
