package com.fengyun.cube.workflow.domain;


import java.util.List;

public class App {
	
	private String id;
	
	private String name;
	
	private String status;
	
	private String description;
	
	private List<Attachment> attachment;
	
	private String createdId;
	
	private String updatedId;
	
	private String createdDate;
	
	private String updatedDate;
	
	private CacheApp cacheApp;
	
	private String processInstanceId;
	
	private String lastPublishDate;

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


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CacheApp getCacheApp() {
		return cacheApp;
	}

	public void setCacheApp(CacheApp cacheApp) {
		this.cacheApp = cacheApp;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
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
