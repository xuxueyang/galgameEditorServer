package com.fengyun.cube.workflow.domain;

import java.util.List;

public class ApplySetting {
	
	private String id;
	
	private String applyCallback;
	
	private String applyCallbackContent;
	
	private String applyCallbackLink;
	
	private int applyLimitMonthly;
	
	private int applyLimitTotal;
	
	private int canApplyTotal;
	
	private int currentApplyMonth;
	
	private int currentApplyTotal;
	
	private String appId;
	
	private boolean canApplyLimitMonthly;
	
	private boolean canApplyLimitTotal;
	
	private PushType pushTypes;
	
	private boolean realTimePush;
	
	private boolean regularPush;
	
	private List<String> regularPushPeriods;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCanApplyTotal() {
		return canApplyTotal;
	}

	public void setCanApplyTotal(int canApplyTotal) {
		this.canApplyTotal = canApplyTotal;
	}


	public String getApplyCallbackContent() {
		return applyCallbackContent;
	}

	public void setApplyCallbackContent(String applyCallbackContent) {
		this.applyCallbackContent = applyCallbackContent;
	}

	public String getApplyCallbackLink() {
		return applyCallbackLink;
	}

	public void setApplyCallbackLink(String applyCallbackLink) {
		this.applyCallbackLink = applyCallbackLink;
	}

	public int getApplyLimitMonthly() {
		return applyLimitMonthly;
	}

	public void setApplyLimitMonthly(int applyLimitMonthly) {
		this.applyLimitMonthly = applyLimitMonthly;
	}

	public int getApplyLimitTotal() {
		return applyLimitTotal;
	}

	public void setApplyLimitTotal(int applyLimitTotal) {
		this.applyLimitTotal = applyLimitTotal;
	}

	public int getCurrentApplyMonth() {
		return currentApplyMonth;
	}

	public void setCurrentApplyMonth(int currentApplyMonth) {
		this.currentApplyMonth = currentApplyMonth;
	}

	public int getCurrentApplyTotal() {
		return currentApplyTotal;
	}

	public void setCurrentApplyTotal(int currentApplyTotal) {
		this.currentApplyTotal = currentApplyTotal;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}


	public PushType getPushTypes() {
		return pushTypes;
	}

	public void setPushTypes(PushType pushTypes) {
		this.pushTypes = pushTypes;
	}

	public boolean isRealTimePush() {
		return realTimePush;
	}

	public void setRealTimePush(boolean realTimePush) {
		this.realTimePush = realTimePush;
	}

	public boolean isRegularPush() {
		return regularPush;
	}

	public void setRegularPush(boolean regularPush) {
		this.regularPush = regularPush;
	}

	public String getApplyCallback() {
		return applyCallback;
	}

	public void setApplyCallback(String applyCallback) {
		this.applyCallback = applyCallback;
	}

	public boolean isCanApplyLimitMonthly() {
		return canApplyLimitMonthly;
	}

	public void setCanApplyLimitMonthly(boolean canApplyLimitMonthly) {
		this.canApplyLimitMonthly = canApplyLimitMonthly;
	}

	public boolean isCanApplyLimitTotal() {
		return canApplyLimitTotal;
	}

	public void setCanApplyLimitTotal(boolean canApplyLimitTotal) {
		this.canApplyLimitTotal = canApplyLimitTotal;
	}

	public List<String> getRegularPushPeriods() {
		return regularPushPeriods;
	}

	public void setRegularPushPeriods(List<String> regularPushPeriods) {
		this.regularPushPeriods = regularPushPeriods;
	}

}
