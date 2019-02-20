package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class ConditionDto {
	@ApiModelProperty(value = "当前应用，字段value")
	private String queValue;
	@ApiModelProperty(value = "关联应用，字段ID")
	private String relatedQueId;
	public String getQueValue() {
		return queValue;
	}
	public void setQueValue(String queValue) {
		this.queValue = queValue;
	}
	public String getRelatedQueId() {
		return relatedQueId;
	}
	public void setRelatedQueId(String relatedQueId) {
		this.relatedQueId = relatedQueId;
	}
	
}
