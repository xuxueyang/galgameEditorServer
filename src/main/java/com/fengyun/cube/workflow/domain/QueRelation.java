package com.fengyun.cube.workflow.domain;

import io.swagger.annotations.ApiModelProperty;

public class QueRelation {
	@ApiModelProperty(value = "当前应用，字段ID")
	private String queId;
	@ApiModelProperty(value = "关联应用，字段ID")
	private String relatedQueId;
	private String type;
	
	public String getQueId() {
		return queId;
	}
	public void setQueId(String queId) {
		this.queId = queId;
	}
	public String getRelatedQueId() {
		return relatedQueId;
	}
	public void setRelatedQueId(String relatedQueId) {
		this.relatedQueId = relatedQueId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
