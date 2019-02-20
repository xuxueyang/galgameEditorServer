package com.fengyun.cube.workflow.service.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class UpdateDataDto {
    @ApiModelProperty(value = "queId")
    private String queId;

    @ApiModelProperty(value = "value")
    private String value;

    public String getQueId() {
        return queId;
    }

    public void setQueId(String queId) {
        this.queId = queId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
