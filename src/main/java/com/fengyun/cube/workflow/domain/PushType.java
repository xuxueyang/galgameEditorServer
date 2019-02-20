package com.fengyun.cube.workflow.domain;

import io.swagger.annotations.ApiModelProperty;

public class PushType {

	@ApiModelProperty(value = "申请结果")
	private boolean applyResult;
	@ApiModelProperty(value = "抄送数据")
	private boolean ccData;
	@ApiModelProperty(value = "待处理数据")
	private boolean dealData;
	
	public boolean isApplyResult() {
		return applyResult;
	}
	public void setApplyResult(boolean applyResult) {
		this.applyResult = applyResult;
	}
	public boolean isCcData() {
		return ccData;
	}
	public void setCcData(boolean ccData) {
		this.ccData = ccData;
	}
	public boolean isDealData() {
		return dealData;
	}
	public void setDealData(boolean dealData) {
		this.dealData = dealData;
	}
	
	
}
