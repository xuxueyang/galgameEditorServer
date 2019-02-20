package com.fengyun.cube.workflow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.service.dto.request.ConditionDto;
import com.fengyun.cube.workflow.service.dto.request.RelateDataDto;
import com.fengyun.cube.workflow.service.dto.request.RelateOtherDataDto;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Transactional
@Service
public class RelateOtherService {

    //返回的是CODE
    private static final String OA_DIC_URL = "https://ac1.fengyuntec.com/api/dataDictionary/data/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c&dataTypeCode=oa_interface_parameters";

    private static final String SUBJECT_URL = "https://ac1.fengyuntec.com/api/subject/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c";

    private static final String CUSTOMER = "CUSTOMER";//客户信息
    private static final String PROJECT = "PROJECT";//项目信息
    private static final String CONTRACT = "CONTRACT";//合同信息
    private static final String DATADICTIONARY = "DATADICTIONARY";//oa数据字典
    private static final String SUBJECT = "SUBJECT"; // 科目信息
    private static final String relateOtherId="relateOtherId";
    private class type{
        private String id;
        private String name;
        public type(String id,String name){
            this.id = id;
            this.name = name;
        }
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public List<type> getTypes() {
        List<type>  types = new ArrayList<>();
        types.add(new type(CUSTOMER,"客户信息"));
        types.add(new type(PROJECT,"项目信息"));
        types.add(new type(CONTRACT,"合同信息"));
        types.add(new type(DATADICTIONARY,"数据字典"));
        types.add(new type(SUBJECT,"科目信息"));
        return types;
    }
    public Set<type> getOtherRelateData(RelateDataDto relateDataDto) {
        //type:relateDataDto.getRelateAppId()
        if(CUSTOMER.equals(relateDataDto.getRelateAppId())||
                PROJECT.equals(relateDataDto.getRelateAppId())||
                CONTRACT.equals(relateDataDto.getRelateAppId())){
            //这种情况先查出值，再作为参数塞入
            if(relateDataDto.getConditions()!= null && relateDataDto.getConditions().size()>0) {//筛选条件不为空
                Set<type> values = new HashSet<>();
                Set<JSONObject> valuesList = null;
                for (ConditionDto queFilter :  relateDataDto.getConditions()) {
                    //TODO 逻辑：判断
                    //relateDataDto.getRelateQueId():判断返回的显示值
                    //relateDataDto.getConditions().get(0).getRelatedQueId():判断删选的值
                    if(relateOtherId.equals(queFilter.getRelatedQueId())){
                        valuesList = getValuesList(relateDataDto.getRelateAppId(), queFilter.getQueValue());
                        if(valuesList!=null||valuesList.size()>0){
                            break;
                        }
                    }
                }
                if(valuesList!=null&&valuesList.size()>0){
                    //valuesList是全部的值
                    for(JSONObject str:valuesList){
                        //根据不同字段得到不同的值
                        Object o = str.get(relateDataDto.getRelateQueId());
                        if(o!=null){
                            values.add(new type(str.get("id").toString(),JSONObject.toJSONString(o)));
                        }
                    }
                    return values;
                }
            }
            return null;
        }else{
            Set<JSONObject> set = getValuesList(relateDataDto.getRelateAppId(),relateDataDto.getRelateQueId());

            Set<type> filterDataIds = new HashSet<>();
//            if(relateDataDto.getConditions()!= null && relateDataDto.getConditions().size()>0){//筛选条件不为空
//                // 删选条件
//                //获取所有满足条件的单据
//                int i = 0;
//                for (JSONObject value : set) {
//                    for (ConditionDto queFilter :  relateDataDto.getConditions()) {
//                        if(value.get("name").equals(queFilter.getQueValue())){
//                            i = i+1;//如果满足条件就将筛选条件去掉，当筛选条件为空时，表示满足所有条件
//                        }
//                    }
//
//                    if(i == relateDataDto.getConditions().size()){
//                        filterDataIds.add(new type(value.get("id").toString(),value.get("name").toString()));
//                    }
//                }
//            }
//            return filterDataIds;
            if(set!=null&&set.size()>0){
                for(JSONObject value:set){
                    filterDataIds.add(new type(value.get("id").toString(),value.get("name").toString()));
                }
            }
            return filterDataIds;
        }
    }
    public Object getLevelList(String type){
        switch (type){
            case CUSTOMER:
            {
                //客户只要员工字段
                ListItem item = new ListItem();
                item.setName("员工ID");
                item.setId(relateOtherId);
                item.setNeedShow(false);
                List<ListItem> lists = new ArrayList<>();
                lists.add(item);
                lists.add(new ListItem("id","客户ID",true));
                lists.add(new ListItem("name","客户名称",true));
                lists.add(new ListItem("status","客户状态",true));
                lists.add(new ListItem("statusCode","客户状态代码",true));
                lists.add(new ListItem("nature","客户性质",true));
                lists.add(new ListItem("natureCode","客户性质代码",true));
                lists.add(new ListItem("type","客户类型",true));
                lists.add(new ListItem("typeCode","客户类型代码",true));
                lists.add(new ListItem("salesManager","销售经理",true));
                lists.add(new ListItem("sharePeople","共享人",true));
                lists.add(new ListItem("address","客户地址",true));
                return lists;
            }
            case PROJECT:
            {
                //项目只有客户字段
                ListItem item = new ListItem();
                item.setName("客户ID");
                item.setId(relateOtherId);
                List<ListItem> lists = new ArrayList<>();
                lists.add(new ListItem("id","项目ID",true));
                lists.add(new ListItem("name","项目名称",true));
                lists.add(new ListItem("salesPhase","销售阶段",true));
                lists.add(new ListItem("salesPhaseCode","销售阶段代码",true));
                lists.add(new ListItem("internalNum","项目编号",true));

                lists.add(item);
                return lists;
            }
            case CONTRACT:
            {
                //合同只要项目字段
                ListItem item = new ListItem();
                item.setName("项目ID");
                item.setId(relateOtherId);
//                item.setName("合同名称");
//                item.setId("salesChanceName");
                List<ListItem> lists = new ArrayList<>();
                lists.add(new ListItem("id","项目ID",true));
                lists.add(new ListItem("name","合同名称",true));
                lists.add(new ListItem("contractNum","合同编号",true));
                lists.add(new ListItem("type","业务类型",true));
                lists.add(new ListItem("typeCode","合同类型代码",true));
                lists.add(new ListItem("contractMoney","合同金额",true));
                lists.add(new ListItem("businessType","业务类型",true));
                lists.add(new ListItem("businessTypeCode","业务类型代码",true));
                lists.add(new ListItem("signCompany","签约单位",true));
                lists.add(new ListItem("signCompanyCode","签约单位代码",true));
                lists.add(new ListItem("signDate","签约时间",true));
                lists.add(new ListItem("startContractDate","合同开始时间",true));
                lists.add(new ListItem("endContractDate","合同结束时间",true));

                lists.add(item);
                return lists;
            }
            case DATADICTIONARY:
            {
                return getFirstList(OA_DIC_URL);
            }
            case SUBJECT:
            {
                return getFirstList(SUBJECT_URL);
            }
            default:
                return null;
        }
    }
    private String httpGet(String url){
        String result = "";
        //同步全部用户
        {
            System.out.println("URL:"+url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse response = null;
            CloseableHttpClient httpclient = null;
            httpclient = HttpClients.createDefault();
            try {
                response = httpclient.execute(httpGet);
                if (response.getEntity() == null) {
                    return null;
                }
                result = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }
    private List<JSONObject> getFirstList(String url){
        String result = httpGet(url);
//        LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", url);

//        LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", result);

        if(result==null||"".equals(result))
            return null;
        //TODO 一些code字段的可能需要配置转换
        RelateOtherDto dto = null;
        try{
            dto = JSON.parseObject(result, RelateOtherDto.class);
        }catch (Exception e){
            return null;
        }
//        LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", dto);

        if("0".equals(dto.getCode())){
            List<JSONObject> listItems = new ArrayList<>();
            try {
                JSONArray objects = JSON.parseArray(dto.getObj());
                if(objects!=null){
                    for(int i=0;i<objects.size();i++){
                        listItems.add(objects.getJSONObject(i));
                    }
                }
            }catch (Exception e){
                //如果转换有错，视为一个
                try {
                    JSONObject tmp = JSONObject.parseObject(dto.getObj());
                    listItems.add(tmp);
                }catch (Exception e2){
                    //说明
                    return null;
                }
            }
            //一些系统是code，一些系统是ID，所以需要转换，统一为id
            if(listItems!=null&&listItems.size()>0){
                for(JSONObject item:listItems){
                    //优先放置ID，如果没有，则放置code
                    if(Validators.fieldBlank(item.get("id"))&&Validators.fieldNotBlank(item.get("code"))){
//                        item.setId(item.get("code"));
                        item.put("id",item.get("code"));
                    }
                }
            }
            return listItems;
        }else{
            return null;
        }
    }
    public Set<JSONObject> getValuesList(String type,String needId) {
        switch (type){
            case CUSTOMER:
                return getCustomerValues(needId);
            case PROJECT:
                return getProjectValues(needId);
            case CONTRACT:
                return getContractValues(needId);
            case DATADICTIONARY:
                return getDicValues(needId);
            case SUBJECT:
                return getSubjectValuse(needId);
            default:
                return null;
        }
    }

    private Set<JSONObject> getSubjectValuse(String needId) {
        Set<JSONObject> set = new HashSet<>();
        if(needId!=null&&!"".equals(needId)){
            List<JSONObject> firstList = getFirstList(SUBJECT_URL);
            if(firstList!=null&&firstList.size()>0){
                //得到具体的那个Name
                for(JSONObject item:firstList){
//                    String name = getSubjectNameById(item, needId);
//                    if(name!=null&&!"".equals(name))
//                        set.add(new type(item.getId(),name));
                    set.add(item);
                }
            }
        }
        return set;
    }
    private String getSubjectNameById(ListItem item,String id){
        if(item!=null&&item.getChildren()!=null&&item.getChildren().size()>0){
            for(ListItem tmp:item.getChildren()){
                if(tmp.getId().equals(id)){
                    return tmp.getName();
                }else{
                    String name = getSubjectNameById(tmp, id);
                    if(name!=null&&!"".equals(name))
                        return name;
                }
            }
        }
        return "";
    }
    private Set<JSONObject> getDicValues(String code) {
        Set<JSONObject> set = new HashSet<>();
        if(code!=null&&!"".equals(code)){
            List<JSONObject> firstList = getFirstList(OA_DIC_URL_GET + code);
            //特殊处理一下 id和code字段
            if(firstList!=null&&firstList.size()>0){
                for(JSONObject item:firstList){
                    if(Validators.fieldBlank(item.get("id"))&&Validators.fieldNotBlank(item.get("code"))){
                        item.put("id",item.get("code"));
                    }
                }
                for(JSONObject item:firstList){
                    set.add(item);
                }
            }
        }
        return set;
    }

    private Set<JSONObject> getContractValues( String id) {
        Set<JSONObject> set = new HashSet<>();
        if(id!=null&&!"".equals(id)){
            List<JSONObject> firstList = getFirstList(OA_CONTRACT_URL_GET + id);
            if(firstList!=null&&firstList.size()>0){
                for(JSONObject item:firstList){
                    if(Validators.fieldBlank(item.get("id"))&&Validators.fieldNotBlank(item.get("code"))){
                        item.put("id",item.get("code"));
                    }
                }
                for(JSONObject item:firstList){
                    set.add(item);
                }
            }
        }
        return set;
    }

    private Set<JSONObject> getProjectValues( String id) {
        Set<JSONObject> set = new HashSet<>();
        if(id!=null&&!"".equals(id)){
            List<JSONObject> firstList = getFirstList(OA_PROJECT_URL_GET +id);
            if(firstList!=null&&firstList.size()>0){
                for(JSONObject item:firstList){
                    set.add(item);
                }
            }
        }
        return set;
    }


    private Set<JSONObject> getCustomerValues( String id) {
        Set<JSONObject> set = new HashSet<>();
        if(id!=null&&!"".equals(id)){
            List<JSONObject> firstList = getFirstList(OA_CUSTOMER_URL_GET + id);
            if(firstList!=null&&firstList.size()>0){
                for(JSONObject item:firstList){
                    set.add(item);
                }
            }
        }
        return set;
    }

    private static final String OA_PROJECT_URL_GET = "https://ac1.fengyuntec.com/api/project/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c&customerId=";
    private static final String OA_CONTRACT_URL_GET = "https://ac1.fengyuntec.com/api/contract/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c&salesChanceId=";
    private static final String OA_CUSTOMER_URL_GET = "https://ac1.fengyuntec.com/api/customer/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c&employeeId=";
    //    返回的是CODE
    private static final String OA_DIC_URL_GET = "https://ac1.fengyuntec.com/api/dataDictionary/list?token=e010b016-9beb-470a-abbb-38ee9614eb2c&dataTypeCode=";



    //一级列表
    private static  class RelateOtherDto {
        private String code;
        private String msg;
        private String obj;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getObj() {
            return obj;
        }

        public void setObj(String obj) {
            this.obj = obj;
        }

    }
    private static  class ListItem{
        private String id;
        private String name;
        private String code;
        private boolean needShow;
        private List<ListItem> children;
        public ListItem(){
            needShow=true;
        }
        public ListItem(String id,String name,boolean needShow){
            this.id=id;
            this.name=name;
            this.needShow = needShow;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<ListItem> getChildren() {
            return children;
        }

        public void setChildren(List<ListItem> children) {
            this.children = children;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isNeedShow() {
            return needShow;
        }

        public void setNeedShow(boolean needShow) {
            this.needShow = needShow;
        }
    }
}
