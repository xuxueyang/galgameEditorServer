package com.fengyun.cube.workflow.repository;


import com.fengyun.cube.workflow.domain.OaGroupSuperiors;

public interface OaGroupSuperiorsMapper {
    OaGroupSuperiors findOneByDepartmentId(final String departmentId);

    int insert(OaGroupSuperiors userSuperiors);

    int deleteAll();
}
