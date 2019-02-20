package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.PushType;

import io.swagger.annotations.ApiModelProperty;

public class UpdateSettingDto {
	
	@ApiModelProperty(value = "全局设置ID")
	private String id;
	
	@ApiModelProperty(value = "申请人填写表单后显示，1：默认文案，2：显示指定类容,3:跳转指定链接")
	private String applyCallback;
	
	@ApiModelProperty(value = "指定内容")
	private String applyCallbackContent;
	
	@ApiModelProperty(value = "指定链接")
	private String applyCallbackLink;
	
	@ApiModelProperty(value = "限制月提交总量")
	private int applyLimitMonthly;
	
	@ApiModelProperty(value = "限制提交总量")
	private int applyLimitTotal;
	
	@ApiModelProperty(value = "当月已申请数量")
	private int currentApplyMonth;
	
	@ApiModelProperty(value = "已申请数量")
	private int currentApplyTotal;
	
	@ApiModelProperty(value = "应用id")
	private String appId;
	
	@ApiModelProperty(value = "是否限制每月提交上限")
	private boolean canApplyLimitMonthly;
	@ApiModelProperty(value = "是否限制提交上限")
	private boolean canApplyLimitTotal;
	
	@ApiModelProperty(value = "推送内容")
	private PushType pushTypes;
	@ApiModelProperty(value = "实时推送")
	private boolean realTimePush;
	@ApiModelProperty(value = "定期推送")
	private boolean regularPush;
	
	@ApiModelProperty(value = "1：早晨9点，2：下午1点，3：下午5点")
	private List<String> regularPushPeriods;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApplyCallback() {
		return applyCallback;
	}

	public void setApplyCallback(String applyCallback) {
		this.applyCallback = applyCallback;
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

	public List<String> getRegularPushPeriods() {
		return regularPushPeriods;
	}

	public void setRegularPushPeriods(List<String> regularPushPeriods) {
		this.regularPushPeriods = regularPushPeriods;
	}
	
	
}
