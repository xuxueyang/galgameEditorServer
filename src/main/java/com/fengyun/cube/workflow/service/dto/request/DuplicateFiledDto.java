package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class DuplicateFiledDto {
	
	@ApiModelProperty(value = "应用id")
	private String appId;
	@ApiModelProperty(value = "字段id")
	private String queId;
	@ApiModelProperty(value = "字段value")
	private Object value;
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
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	
}
