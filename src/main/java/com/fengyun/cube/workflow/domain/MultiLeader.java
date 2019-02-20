package com.fengyun.cube.workflow.domain;

public class MultiLeader {
    private String superiorId;//领导人ID
    private OaGroupAuditingAgent auditingAgent;//代理审批人

    public String getSuperiorId() {
        return superiorId;
    }

    public void setSuperiorId(String superiorId) {
        this.superiorId = superiorId;
    }

    public OaGroupAuditingAgent getAuditingAgent() {
        return auditingAgent;
    }

    public void setAuditingAgent(OaGroupAuditingAgent auditingAgent) {
        this.auditingAgent = auditingAgent;
    }
}
