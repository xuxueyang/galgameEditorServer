package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class PutAppDataDto {
    @ApiModelProperty(value = "草稿的单据ID")
    private String dataId;

    @ApiModelProperty(value = "单据数据")
    private String formdataJson;



    public String getFormdataJson() {
        return formdataJson;
    }

    public void setFormdataJson(String formdataJson) {
        this.formdataJson = formdataJson;
    }


    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
