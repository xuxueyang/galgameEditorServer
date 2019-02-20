package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.AutoJudge;

import io.swagger.annotations.ApiModelProperty;

public class UpdateBranchDto {
	
	@ApiModelProperty(value = "分支ID")
	private String auditNodeId;
	
	@ApiModelProperty(value = "分支筛选条件")
	private List<List<AutoJudge>> autoJudges; //分支筛选条件
	
	@ApiModelProperty(value = "应用ID")
	private String appId;

	public String getAuditNodeId() {
		return auditNodeId;
	}

	public void setAuditNodeId(String auditNodeId) {
		this.auditNodeId = auditNodeId;
	}

	public List<List<AutoJudge>> getAutoJudges() {
		return autoJudges;
	}

	public void setAutoJudges(List<List<AutoJudge>> autoJudges) {
		this.autoJudges = autoJudges;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}
