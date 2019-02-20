package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.QueRelation;

import io.swagger.annotations.ApiModelProperty;

public class RelateDataDto {
	
	@ApiModelProperty(value = "关联应用;或关联外部接口的type")
	private String relateAppId;
	@ApiModelProperty(value = "筛选条件")
	private List<ConditionDto> conditions;
	
	@ApiModelProperty(value = "关联字段")
	private String relateQueId;

	public String getRelateAppId() {
		return relateAppId;
	}

	public void setRelateAppId(String relateAppId) {
		this.relateAppId = relateAppId;
	}

	public List<ConditionDto> getConditions() {
		return conditions;
	}

	public void setConditions(List<ConditionDto> conditions) {
		this.conditions = conditions;
	}

	public String getRelateQueId() {
		return relateQueId;
	}

	public void setRelateQueId(String relateQueId) {
		this.relateQueId = relateQueId;
	}

	
}
