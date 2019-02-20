package com.fengyun.cube.workflow.domain;

import java.util.List;

public class WorkflowNode {
	private ApplyConfig applyConfig;//申请节点配置
	private List<AuditEdit> auditEdits;//编辑
	private boolean auditFeedback;//允许负责人填写处理反馈
	private List<AuditEdit> auditHides; //隐藏
	private String auditNodeId;//节点id
	private String auditNodeName;//节点名称
	private List<AuditUserInfo> auditUserInfos; //用户
	private List<List<AutoJudge>> autoJudges; //分支筛选条件
	private List<WorkflowNode> branches;//分支
	private WorkflowNode nextAuditNode; //下个节点
	private boolean revert;//允许回退操作
	private boolean transfer;//允许转交数据至其他成员
	private int type;//节点类型
	private String preNodeId;//上一个节点id
	private String appId;//表单id
	private boolean root; //是否为根节点
	private boolean isBranchEnd; 
	private QRobotConfig qRobotConfig;
	private String nextNodeId;
	private String processInstanceId;//流程实例id，发布时使用
	private int multiAudit;//逐级审批的层数
	private boolean needMergeMultiAudit;//是否需要合并审批
	private boolean canAgentAudit; //允许代理人审批

	private int countersign;//会签标识
	
	public ApplyConfig getApplyConfig() {
		return applyConfig;
	}
	public void setApplyConfig(ApplyConfig applyConfig) {
		this.applyConfig = applyConfig;
	}
	public List<AuditEdit> getAuditEdits() {
		return auditEdits;
	}
	public void setAuditEdits(List<AuditEdit> auditEdits) {
		this.auditEdits = auditEdits;
	}
	public boolean isAuditFeedback() {
		return auditFeedback;
	}
	public void setAuditFeedback(boolean auditFeedback) {
		this.auditFeedback = auditFeedback;
	}
	public List<AuditEdit> getAuditHides() {
		return auditHides;
	}
	public void setAuditHides(List<AuditEdit> auditHides) {
		this.auditHides = auditHides;
	}
	public String getAuditNodeId() {
		return auditNodeId;
	}
	public void setAuditNodeId(String auditNodeId) {
		this.auditNodeId = auditNodeId;
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
	public List<List<AutoJudge>> getAutoJudges() {
		return autoJudges;
	}
	public void setAutoJudges(List<List<AutoJudge>> autoJudges) {
		this.autoJudges = autoJudges;
	}
	public boolean isRevert() {
		return revert;
	}
	public void setRevert(boolean revert) {
		this.revert = revert;
	}
	public boolean isTransfer() {
		return transfer;
	}
	public void setTransfer(boolean transfer) {
		this.transfer = transfer;
	}
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
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public List<WorkflowNode> getBranches() {
		return branches;
	}
	public void setBranches(List<WorkflowNode> branches) {
		this.branches = branches;
	}
	public WorkflowNode getNextAuditNode() {
		return nextAuditNode;
	}
	public void setNextAuditNode(WorkflowNode nextAuditNode) {
		this.nextAuditNode = nextAuditNode;
	}
//	public int getIndex() {
//		return index;
//	}
//	public void setIndex(int index) {
//		this.index = index;
//	}
	public QRobotConfig getqRobotConfig() {
		return qRobotConfig;
	}
	public void setqRobotConfig(QRobotConfig qRobotConfig) {
		this.qRobotConfig = qRobotConfig;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	public boolean isBranchEnd() {
		return isBranchEnd;
	}
	public void setBranchEnd(boolean isBranchEnd) {
		this.isBranchEnd = isBranchEnd;
	}
	public String getNextNodeId() {
		return nextNodeId;
	}
	public void setNextNodeId(String nextNodeId) {
		this.nextNodeId = nextNodeId;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public int getCountersign() {
		return countersign;
	}
	public void setCountersign(int countersign) {
		this.countersign = countersign;
	}

	public int getMultiAudit() {
		return multiAudit;
	}

	public void setMultiAudit(int multiAudit) {
		this.multiAudit = multiAudit;
	}

	public boolean isNeedMergeMultiAudit() {
		return needMergeMultiAudit;
	}

	public void setNeedMergeMultiAudit(boolean needMergeMultiAudit) {
		this.needMergeMultiAudit = needMergeMultiAudit;
	}

	public boolean isCanAgentAudit() {
		return canAgentAudit;
	}

	public void setCanAgentAudit(boolean canAgentAudit) {
		this.canAgentAudit = canAgentAudit;
	}
}
