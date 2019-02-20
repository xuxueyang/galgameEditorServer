package com.fengyun.cube.workflow.domain;

import java.io.Serializable;

public class OaGroupAuditingAgent extends BaseEntity implements Serializable {


    private String agentId;

    private String departmentId;

    private String tenantCode;

    private String spaceCode;

    public String getAgentUserId() {
        return agentId;
    }

    public void setAgentUserId(String agentId) {
        this.agentId = agentId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getSpaceCode() {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode) {
        this.spaceCode = spaceCode;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}
