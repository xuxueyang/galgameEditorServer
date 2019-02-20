package com.fengyun.cube.workflow.domain;

import io.swagger.annotations.ApiModelProperty;

public class AuditUserInfo {
	@ApiModelProperty(value = "用户id")
    private String userId;
	@ApiModelProperty(value = "账号id")
    private String acctId;
	@ApiModelProperty(value = "组织架构id（动态部门负责人）")
	private String groupIdForLeader;

	@ApiModelProperty(value = "上级领导：由该关联人的上级领导处理")
	private boolean groupSuperior;

	@ApiModelProperty(value = "审批代理人：由该关联人的部门审批代理人处理")
	private boolean groupAuditAgent;

	@ApiModelProperty(value = "动态表单的成员的queId")
	private String memQueId;

	@ApiModelProperty(value = "是否申请人部门负责人")
	private boolean createIdForLeader;

	@ApiModelProperty(value = "是否申请人")
	private boolean needApplyUser;
	
	@ApiModelProperty(value = "昵称")
	private String nickName;
	@ApiModelProperty(value = "邮箱")
	private String email;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAcctId() {
		return acctId;
	}
	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}
	public String getGroupIdForLeader() {
		return groupIdForLeader;
	}
	public void setGroupIdForLeader(String groupIdForLeader) {
		this.groupIdForLeader = groupIdForLeader;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isCreateIdForLeader() {
		return createIdForLeader;
	}
	public void setCreateIdForLeader(boolean createIdForLeader) {
		this.createIdForLeader = createIdForLeader;
	}
	public boolean isNeedApplyUser() {
		return needApplyUser;
	}
	public void setNeedApplyUser(boolean needApplyUser) {
		this.needApplyUser = needApplyUser;
	}

	public String getMemQueId() {
		return memQueId;
	}

	public void setMemQueId(String memQueId) {
		this.memQueId = memQueId;
	}


	public boolean isGroupSuperior() {
		return groupSuperior;
	}

	public void setGroupSuperior(boolean groupSuperior) {
		this.groupSuperior = groupSuperior;
	}

	public boolean isGroupAuditAgent() {
		return groupAuditAgent;
	}

	public void setGroupAuditAgent(boolean groupAuditAgent) {
		this.groupAuditAgent = groupAuditAgent;
	}
}
