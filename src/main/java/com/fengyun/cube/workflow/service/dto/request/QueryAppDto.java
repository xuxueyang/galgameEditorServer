package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class QueryAppDto {
	
	@ApiModelProperty(value = "搜索内容")
	private String searchContent;
	@ApiModelProperty(value = "应用状态")
	private List<String> status;
	
	public String getSearchContent() {
		return searchContent;
	}
	public void setSearchContent(String searchContent) {
		this.searchContent = searchContent;
	}
	public List<String> getStatus() {
		return status;
	}
	public void setStatus(List<String> status) {
		this.status = status;
	}
	
	
}
