package com.fengyun.cube.workflow.domain;

public class Variable {

	private String id;

	private long completeNum;//完成数
	private long completeNumY;//通过数据
    private long completeNumN;//拒绝数目

    private long totalNum;//总数

	private String dataId;//单据Id

	private String processInstanceId; //流程实例id

	private String workflowNodeId;//节点Id

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCompleteNum() {
		return completeNum;
	}

	public void setCompleteNum(long completeNum) {
		this.completeNum = completeNum;
	}

	public long getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
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

	public String getWorkflowNodeId() {
		return workflowNodeId;
	}

	public void setWorkflowNodeId(String workflowNodeId) {
		this.workflowNodeId = workflowNodeId;
	}


    public long getCompleteNumY() {
        return completeNumY;
    }

    public void setCompleteNumY(long completeNumY) {
        this.completeNumY = completeNumY;
    }

    public long getCompleteNumN() {
        return completeNumN;
    }

    public void setCompleteNumN(long completeNumN) {
        this.completeNumN = completeNumN;
    }
}
