package com.fengyun.cube.workflow.service.dto.response;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import com.fengyun.cube.workflow.domain.Attachment;
import com.fengyun.cube.workflow.domain.WorkflowNode;
import com.mongodb.DBObject;

public class AppDto {
	private String id;
	private String name;
	private String status;
	
	private String description;
	
	private List<Attachment> attachment;
	
	private String createdId;
	
	private String updatedId;
	
	private String createdDate;
	
	private String updatedDate;
	
	private CacheAppDto cacheAppDto;
	
	private List<Object> formData;
	
	private List<WorkflowNode> workflowData;
	
	private String lastPublishDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedId() {
		return createdId;
	}

	public void setCreatedId(String createdId) {
		this.createdId = createdId;
	}

	public String getUpdatedId() {
		return updatedId;
	}

	public void setUpdatedId(String updatedId) {
		this.updatedId = updatedId;
	}




//	public List<DBObject> getFormData() {
//		return formData;
//	}
//
//	public void setFormData(List<DBObject> formData) {
//		this.formData = formData;
//	}
	
	

	public CacheAppDto getCacheAppDto() {
		return cacheAppDto;
	}

	public List<Object> getFormData() {
		return formData;
	}

	public void setFormData(List<Object> formData) {
		this.formData = formData;
	}

	public void setCacheAppDto(CacheAppDto cacheAppDto) {
		this.cacheAppDto = cacheAppDto;
	}

	public List<WorkflowNode> getWorkflowData() {
		return workflowData;
	}

	public void setWorkflowData(List<WorkflowNode> workflowData) {
		this.workflowData = workflowData;
	}


	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getLastPublishDate() {
		return lastPublishDate;
	}

	public void setLastPublishDate(String lastPublishDate) {
		this.lastPublishDate = lastPublishDate;
	}
}
