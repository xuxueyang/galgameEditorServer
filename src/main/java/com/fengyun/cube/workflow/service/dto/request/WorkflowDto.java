package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.AuditUserInfo;

import io.swagger.annotations.ApiModelProperty;

public class WorkflowDto {
	@ApiModelProperty(value = "节点类型，0：申请节点，1：审批节点，2：填写节点，3：抄送节点，4：分支节点，5：q-robot添加节点，6：q-robot更新节点，7：逐级审批节点")
	private int type;//节点类型
	@ApiModelProperty(value = "应用ID")
	private String appId;
	
	@ApiModelProperty(value = "上一个节点ID")
	private String preNodeId;//上一个节点id
	
	@ApiModelProperty(value = "节点名称")
	private String auditNodeName;//节点名称
	
	@ApiModelProperty(value = "用户信息")
	private List<AuditUserInfo> auditUserInfos; //用户
	
	@ApiModelProperty(value = "是否为第一层")
	private Boolean root;

	@ApiModelProperty(value = "如果是逐级审批节点，需要设置逐级审批的层数。为空则默认所有层")
	private int multiAudit;

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getPreNodeId() {
		return preNodeId;
	}
	public void setPreNodeId(String preNodeId) {
		this.preNodeId = preNodeId;
	}
	public String getAuditNodeName() {
		return auditNodeName;
	}
	public void setAuditNodeName(String auditNodeName) {
		this.auditNodeName = auditNodeName;
	}
	public List<AuditUserInfo> getAuditUserInfos() {
		return auditUserInfos;
	}
	public void setAuditUserInfos(List<AuditUserInfo> auditUserInfos) {
		this.auditUserInfos = auditUserInfos;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public Boolean getRoot() {
		return root;
	}
	public void setRoot(Boolean root) {
		this.root = root;
	}


	public int getMultiAudit() {
		return multiAudit;
	}

	public void setMultiAudit(int multiAudit) {
		this.multiAudit = multiAudit;
	}
}
