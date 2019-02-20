package com.fengyun.cube.workflow.domain;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

//推送信息
public class PushInfo {
    private String id;

    private String appId;//appId


    private String dataId;//单据ID


    private String processInstanceId;//流程实例ID

    private String createdDate;//创建时间

    private List<String> pushTime;//推动时间

    private String pushResult;//推送结果;

    private int pushType;//推动类型_邮箱还是手机

    private String pushAddress;//推送的地址

    private String pushValue;//推送的内容

    private String pushDataType;//推送内容的类型——抄送数据、处理数据、通知结果


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }






    public String getPushResult() {
        return pushResult;
    }

    public void setPushResult(String pushResult) {
        this.pushResult = pushResult;
    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }


    public String getPushAddress() {
        return pushAddress;
    }

    public void setPushAddress(String pushAddress) {
        this.pushAddress = pushAddress;
    }

    public String getPushValue() {
        return pushValue;
    }

    public void setPushValue(String pushValue) {
        this.pushValue = pushValue;
    }

    public String getPushDataType() {
        return pushDataType;
    }

    public void setPushDataType(String pushDataType) {
        this.pushDataType = pushDataType;
    }

    public List<String> getPushTime() {
        return pushTime;
    }

    public void setPushTime(List<String> pushTime) {
        this.pushTime = pushTime;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
