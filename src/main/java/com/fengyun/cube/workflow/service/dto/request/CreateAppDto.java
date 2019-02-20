package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.Attachment;

import io.swagger.annotations.ApiModelProperty;

public class CreateAppDto {
	
	@ApiModelProperty(value = "应用名称")
	private String name;
	
	@ApiModelProperty(value = "描述")
	private String descriptin;
	
	@ApiModelProperty(value = "附件")
	private List<Attachment> attachment;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescriptin() {
		return descriptin;
	}

	public void setDescriptin(String descriptin) {
		this.descriptin = descriptin;
	}

	public List<Attachment> getAttachment() {
		return attachment;
	}

	public void setAttachment(List<Attachment> attachment) {
		this.attachment = attachment;
	}


}
