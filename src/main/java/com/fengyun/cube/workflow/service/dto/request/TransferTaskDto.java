package com.fengyun.cube.workflow.service.dto.request;

import com.fengyun.cube.workflow.domain.AuditUserInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class TransferTaskDto  {
    @ApiModelProperty(value = "appId")
    private String appId;
    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "想要转交的人员列表")
    private List<AuditUserInfo> transferList;

    public List<AuditUserInfo> getTransferList() {
        return transferList;
    }

    public void setTransferList(List<AuditUserInfo> transferList) {
        this.transferList = transferList;
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
}
