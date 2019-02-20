package com.fengyun.cube.workflow.service.dto.response;

import java.util.List;

import com.fengyun.cube.workflow.domain.Attachment;

public class CacheAppDto {
	private String name;
	private String description;
	private List<Attachment> attachment;
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

}
