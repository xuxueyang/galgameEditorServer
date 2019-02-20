package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class OverlapFiledDto {
	@ApiModelProperty(value = "应用id")
	private String appId;
	@ApiModelProperty(value = "字段id")
	private String queId;
	@ApiModelProperty(value = "字段开始值")
	private Long startValue;
	@ApiModelProperty(value = "字段结束值")
	private Long endValue;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getQueId() {
		return queId;
	}
	public void setQueId(String queId) {
		this.queId = queId;
	}
	public Long getStartValue() {
		return startValue;
	}
	public void setStartValue(Long startValue) {
		this.startValue = startValue;
	}
	public Long getEndValue() {
		return endValue;
	}
	public void setEndValue(Long endValue) {
		this.endValue = endValue;
	}
}
