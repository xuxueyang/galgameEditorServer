package com.fengyun.cube.workflow.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;

public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private int version;

    private String createdId;

    private String updatedId;

    private String createdDate;

    private String updatedDate;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public void create(String userId) {
        this.version = 0;
        this.createdId = userId;
        this.updatedId = userId;
        this.createdDate = ""+ZonedDateTime.now();
        this.updatedDate = ""+ZonedDateTime.now();
    }

    public void update(String userId) {
        this.updatedId = userId;
        this.updatedDate = ""+ZonedDateTime.now();
    }
}
