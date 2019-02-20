package com.fengyun.cube.workflow.repository;


import com.fengyun.cube.workflow.domain.OaGroupAuditingAgent;

public interface OaGroupAuditingAgentMapper {
    OaGroupAuditingAgent findOneByDepartmentId(final String departmentId);

    int insert(OaGroupAuditingAgent userSuperiors);

    int update(OaGroupAuditingAgent userSuperiors);

    int deleteAll();
}
