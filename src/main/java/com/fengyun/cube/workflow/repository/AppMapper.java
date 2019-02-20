package com.fengyun.cube.workflow.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fengyun.cube.workflow.domain.App;

public interface AppMapper {
	
	void insert(App app);

	void update(App app);

	App findById(String id);

	List<App> findAll(@Param("searchContent")String searchContent, @Param("status")String status);

	void updateStatus(@Param("status")String status, @Param("id")String id,@Param("updatedId")String updatedId, @Param("updatedDate")ZonedDateTime updatedDate);

}
