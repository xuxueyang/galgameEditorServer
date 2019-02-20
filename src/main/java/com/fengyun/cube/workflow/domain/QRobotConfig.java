package com.fengyun.cube.workflow.domain;

import java.util.List;

public class QRobotConfig {
	private List<QueRelation>  queRelation;
	private String relatedAppId;
	private  List<QueRelation>  filterCondition;
	public List<QueRelation> getQueRelation() {
		return queRelation;
	}
	public void setQueRelation(List<QueRelation> queRelation) {
		this.queRelation = queRelation;
	}
	public String getRelatedAppId() {
		return relatedAppId;
	}
	public void setRelatedAppId(String relatedAppId) {
		this.relatedAppId = relatedAppId;
	}
	public List<QueRelation> getFilterCondition() {
		return filterCondition;
	}
	public void setFilterCondition(List<QueRelation> filterCondition) {
		this.filterCondition = filterCondition;
	}
	
}
