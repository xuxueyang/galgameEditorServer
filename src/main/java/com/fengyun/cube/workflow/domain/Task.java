package com.fengyun.cube.workflow.domain;



public class Task {

	private String id;

    private String appId;//appId


    private String dataId;//单据ID

	private String processInstanceId;//流程实例ID

	private String operatorUserId;//用户Id

	private String opeartorAccId;//账号ID

	private String operateResult;//审批结果

	private String createdDate;//创建时间

	private String currentNodeId;//流程节点

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}


	public String getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(String operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getOpeartorAccId() {
		return opeartorAccId;
	}

	public void setOpeartorAccId(String opeartorAccId) {
		this.opeartorAccId = opeartorAccId;
	}

	public String getOperateResult() {
		return operateResult;
	}

	public void setOperateResult(String operateResult) {
		this.operateResult = operateResult;
	}



	public String getCurrentNodeId() {
		return currentNodeId;
	}

	public void setCurrentNodeId(String currentNodeId) {
		this.currentNodeId = currentNodeId;
	}


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
}
