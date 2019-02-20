package com.fengyun.cube.workflow.domain;

public class ApplyConfig {

	private int applyType;
	
	private String applyPass;//设置填写密码
	
	private int canApplyNum;//限制申请次数
	
	private boolean canApplyRevert;//允许申请人撤回申请
	
	public int getApplyType() {
		return applyType;
	}

	public void setApplyType(int applyType) {
		this.applyType = applyType;
	}

	public String getApplyPass() {
		return applyPass;
	}

	public void setApplyPass(String applyPass) {
		this.applyPass = applyPass;
	}

	public int getCanApplyNum() {
		return canApplyNum;
	}

	public void setCanApplyNum(int canApplyNum) {
		this.canApplyNum = canApplyNum;
	}

	public boolean isCanApplyRevert() {
		return canApplyRevert;
	}

	public void setCanApplyRevert(boolean canApplyRevert) {
		this.canApplyRevert = canApplyRevert;
	}
}
