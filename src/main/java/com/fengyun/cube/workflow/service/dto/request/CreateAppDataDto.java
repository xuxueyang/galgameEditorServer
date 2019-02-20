package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class CreateAppDataDto {

	@ApiModelProperty(value = "单据状态，D：草稿，P：提交")
	private String status;
	
	@ApiModelProperty(value = "单据数据")
	private String formdataJson;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFormdataJson() {
		return formdataJson;
	}

	public void setFormdataJson(String formdataJson) {
		this.formdataJson = formdataJson;
	}
	
	
}
