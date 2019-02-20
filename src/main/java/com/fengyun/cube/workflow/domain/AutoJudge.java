package com.fengyun.cube.workflow.domain;

public class AutoJudge extends AuditEdit{
	private String queType;//字段类型
	
	private int judgeType;//条件类型
	
	private String judgeValue;//值

	public String getQueType() {
		return queType;
	}

	public void setQueType(String queType) {
		this.queType = queType;
	}

	public int getJudgeType() {
		return judgeType;
	}

	public void setJudgeType(int judgeType) {
		this.judgeType = judgeType;
	}

	public String getJudgeValue() {
		return judgeValue;
	}

	public void setJudgeValue(String judgeValue) {
		this.judgeValue = judgeValue;
	}
	
	
}
