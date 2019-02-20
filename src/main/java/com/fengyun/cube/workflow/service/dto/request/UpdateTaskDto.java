package com.fengyun.cube.workflow.service.dto.request;

import com.fengyun.cube.workflow.domain.AuditUserInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

public class UpdateTaskDto {
    @ApiModelProperty(value = "appId")
    private String appId;
    @ApiModelProperty(value = "任务ID")
    private String taskId;
    @ApiModelProperty(value = "修改值的ID和value")
    private List<UpdateDataDto> updateDataDtoList;



    public UpdateTaskDto() {
    }



    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<UpdateDataDto> getUpdateDataDtoList() {
        return updateDataDtoList;
    }

    public void setUpdateDataDtoList(List<UpdateDataDto> updateDataDtoList) {
        this.updateDataDtoList = updateDataDtoList;
    }
}
