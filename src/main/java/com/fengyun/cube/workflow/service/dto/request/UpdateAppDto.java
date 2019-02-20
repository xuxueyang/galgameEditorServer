package com.fengyun.cube.workflow.service.dto.request;

import java.util.List;

import com.fengyun.cube.workflow.domain.Attachment;

import io.swagger.annotations.ApiModelProperty;

public class UpdateAppDto {
	private String id;
	@ApiModelProperty(value = "应用名称")
	private String name;
	
	@ApiModelProperty(value = "描述")
	private String description;
	
	@ApiModelProperty(value = "附件")
	private List<Attachment> attachment;
	
	@ApiModelProperty(value = "动态表单数据")
	private String formData;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Attachment> getAttachment() {
		return attachment;
	}

	public void setAttachment(List<Attachment> attachment) {
		this.attachment = attachment;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFormData() {
		return formData;
	}

	public void setFormData(String formData) {
		this.formData = formData;
	}
	
}
