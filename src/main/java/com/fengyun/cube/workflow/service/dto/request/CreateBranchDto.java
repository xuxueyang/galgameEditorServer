package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class CreateBranchDto {

	@ApiModelProperty(value = "应用ID")
	private String appId;
	
	@ApiModelProperty(value = "分支节点ID")
	private String auditNodeId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAuditNodeId() {
		return auditNodeId;
	}

	public void setAuditNodeId(String auditNodeId) {
		this.auditNodeId = auditNodeId;
	}
}
