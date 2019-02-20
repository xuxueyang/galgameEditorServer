package com.fengyun.cube.workflow.web.rest;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.fengyun.cube.workflow.service.RelateOtherService;
import com.fengyun.cube.workflow.service.dto.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fengyun.cube.core.constant.ReturnCode;
import com.fengyun.cube.core.dto.ReturnResultDTO;
import com.fengyun.cube.core.resource.BaseResource;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.App;
import com.fengyun.cube.workflow.domain.WorkflowNode;
import com.fengyun.cube.workflow.service.AppService;
import com.fengyun.cube.workflow.service.FormDataService;
import com.fengyun.cube.workflow.service.WorkflowService;
import com.mongodb.DBObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.websocket.server.PathParam;

@Api(value = "应用单据管理",description = "应用单据管理")
@RestController
@RequestMapping("/api/app")
public class FormDataResource extends BaseResource{

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private AppService appService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private RelateOtherService relateOtherService;

	@GetMapping("/check-apply-pwd")
    @ApiOperation(value = "验证申请密码", httpMethod = "GET", notes = "验证申请密码")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> checkPwd(String appId,String pwd) {
		try {
			if(Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus()) || Validators.fieldBlank(app.getProcessInstanceId())){
				return prepareReturnResult(ReturnCode.ERROR_UN_PUBLISH, null);
			}
			if(Validators.fieldNotBlank(pwd)){
				return prepareReturnResult(ReturnCode.GET_SUCCESS, formDataService.checkApplyPwd(appId,pwd,app.getProcessInstanceId()));
			}else{
				return prepareReturnResult(ReturnCode.GET_SUCCESS, formDataService.isNeedPwd(appId,app.getProcessInstanceId()));
			}


		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@GetMapping("/check-apply-permission")
    @ApiOperation(value = "验证申请权限", httpMethod = "GET", notes = "验证申请权限")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> checkApplyPermission(String appId) {
		try {
			if(Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus()) || Validators.fieldBlank(app.getProcessInstanceId())){
				return prepareReturnResult(ReturnCode.ERROR_UN_PUBLISH, null);
			}
			return prepareReturnResult(ReturnCode.GET_SUCCESS, formDataService.checkApplyPermission(appId,app.getProcessInstanceId()));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@GetMapping("/publish/form-data")
    @ApiOperation(value = "获取节点的表单信息", httpMethod = "GET", notes = "获取节点的表单信息、如果nodeId为空，则获取root根节点的节点表单数据")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getPublishFormData(String appId,String workflowNodeId) {
		try {
			if(Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus()) || Validators.fieldBlank(app.getProcessInstanceId())){
				return prepareReturnResult(ReturnCode.ERROR_UN_PUBLISH, null);
			}

			//验证workflowNodeId
			if(Validators.fieldNotBlank(workflowNodeId)){
				//验证workflowNodeId的合法性
				WorkflowNode node = formDataService.checkNode(app.getId(),app.getProcessInstanceId(),workflowNodeId);
				if(node == null){
					return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
				}
				Map<String,Object> result = formDataService.getFormDataByNode(app,node);
				return prepareReturnResult(ReturnCode.GET_SUCCESS, result);
			}else{
				Map<String,Object> result = formDataService.getFormDataForApplyNode(app);
				return prepareReturnResult(ReturnCode.GET_SUCCESS, result);
			}

		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}
    @PutMapping("/{app-id}/data")
    @ApiOperation(value = "单据草稿提交", httpMethod = "PUT", response = ReturnResultDTO.class, notes = "单据草稿提交")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> putAppData(@PathVariable("app-id") String appId,@RequestBody PutAppDataDto putAppDataDto) {
        try {
//            String str = "[[{\"min_word\":0,\"default_type\":\"custom\",\"cname\":\"单行文字\",\"default_item\":\"input\",\"index\":0,\"type\":\"input\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_title\":true,\"show_width\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_limit_word\":true,\"show_default\":true},\"title\":\"姓名\",\"limit_word\":false,\"required\":false,\"max_word\":0,\"queId\":\"dad95a0e85a3442a9a906b976ffd8686\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":0,\"value\":\"xxy\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}],[{\"min_word\":0,\"default_type\":\"custom\",\"cname\":\"多行文字\",\"default_item\":\"textarea\",\"index\":1,\"type\":\"textarea\",\"displayOption\":{\"show_hint\":true,\"show_title\":true,\"show_width\":true,\"hide_default_type_select\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_limit_word\":true,\"show_default\":true},\"title\":\"简介\",\"limit_word\":false,\"required\":false,\"max_word\":0,\"queId\":\"ed43008d74db46b6bf12dfaafef0c0fe\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":1,\"value\":\"123\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}],[{\"default_type\":\"custom\",\"cname\":\"数字\",\"default_item\":\"input\",\"index\":2,\"type\":\"number\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_decimal\":true,\"show_title\":true,\"show_width\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_default\":true},\"title\":\"工资\",\"required\":false,\"queId\":\"623cfb1e748f445b8762ddea96e3cb0f\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":2,\"decimal\":true,\"norepeat\":false,\"canEdit\":true,\"children\":null,\"value\":\"123\",\"error\":\"\"}],[{\"default_type\":\"custom\",\"cname\":\"手机\",\"default_item\":\"input\",\"index\":3,\"type\":\"phone\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_title\":true,\"show_shared\":true,\"show_width\":true,\"show_required\":true,\"show_repeat\":true,\"show_default\":true},\"title\":\"手机\",\"required\":false,\"queId\":\"df7c195eaf954b329fd74cf22555c2a9\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":3,\"value\":\"13914014940\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}]]";
//            putAppDataDto.setFormdataJson(str);
            if(Validators.fieldBlank(putAppDataDto.getFormdataJson())||
                Validators.fieldBlank(putAppDataDto.getDataId())){
                return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
            }
            App app = appService.findById(appId);
            if(app == null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
            }
            //TODO 全局认证
            if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus()) || Validators.fieldBlank(app.getProcessInstanceId())){
                return prepareReturnResult(ReturnCode.ERROR_UN_PUBLISH, null);
            }
            JSONArray data = JSON.parseArray(putAppDataDto.getFormdataJson());
            if(data == null){
                return prepareReturnResult(ReturnCode.ERROR_FIELD_FORMAT, null);
            }
            WorkflowNode applyNode = workflowService.findApplyNode(app.getId(),app.getProcessInstanceId());
            if(applyNode == null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
            }
            formDataService.submitData(app,data,applyNode,putAppDataDto.getDataId());
            Map<String,String> map = new HashMap<>();
//            map.put("applyCallback",""+applySetting.getApplyCallback());
//            map.put("applyCallbackContent",applySetting.getApplyCallbackContent());
//            map.put("applyCallbackLink",applySetting.getApplyCallbackLink());

            return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, map);
        } catch (Exception e) {
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
        }
    }

	@PostMapping("/{app-id}/data")
    @ApiOperation(value = "单据申请", httpMethod = "POST", response = ReturnResultDTO.class, notes = "单据申请")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> createAppData(@PathVariable("app-id") String appId,@RequestBody CreateAppDataDto createAppDataDto) {
		try {
//		    String str = "[[{\"min_word\":0,\"default_type\":\"custom\",\"cname\":\"单行文字\",\"default_item\":\"input\",\"index\":0,\"type\":\"input\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_title\":true,\"show_width\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_limit_word\":true,\"show_default\":true},\"title\":\"姓名\",\"limit_word\":false,\"required\":false,\"max_word\":0,\"queId\":\"dad95a0e85a3442a9a906b976ffd8686\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":0,\"value\":\"xxy\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}],[{\"min_word\":0,\"default_type\":\"custom\",\"cname\":\"多行文字\",\"default_item\":\"textarea\",\"index\":1,\"type\":\"textarea\",\"displayOption\":{\"show_hint\":true,\"show_title\":true,\"show_width\":true,\"hide_default_type_select\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_limit_word\":true,\"show_default\":true},\"title\":\"简介\",\"limit_word\":false,\"required\":false,\"max_word\":0,\"queId\":\"ed43008d74db46b6bf12dfaafef0c0fe\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":1,\"value\":\"123\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}],[{\"default_type\":\"custom\",\"cname\":\"数字\",\"default_item\":\"input\",\"index\":2,\"type\":\"number\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_decimal\":true,\"show_title\":true,\"show_width\":true,\"show_shared\":true,\"show_required\":true,\"show_repeat\":true,\"show_default\":true},\"title\":\"工资\",\"required\":false,\"queId\":\"623cfb1e748f445b8762ddea96e3cb0f\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":2,\"decimal\":true,\"norepeat\":false,\"canEdit\":true,\"children\":null,\"value\":\"123\",\"error\":\"\"}],[{\"default_type\":\"custom\",\"cname\":\"手机\",\"default_item\":\"input\",\"index\":3,\"type\":\"phone\",\"displayOption\":{\"show_default_type\":true,\"show_hint\":true,\"show_title\":true,\"show_shared\":true,\"show_width\":true,\"show_required\":true,\"show_repeat\":true,\"show_default\":true},\"title\":\"手机\",\"required\":false,\"queId\":\"df7c195eaf954b329fd74cf22555c2a9\",\"hint\":\"\",\"appId\":\"76da7fc451d34efd8ec3afefe4abb7ff\",\"width\":100,\"colIndex\":0,\"rowIndex\":3,\"value\":\"13914014940\",\"norepeat\":false,\"canEdit\":true,\"children\":null,\"error\":\"\"}]]";
//		    createAppDataDto.setFormdataJson(str);
			if(Validators.fieldBlank(createAppDataDto.getStatus()) || Validators.fieldBlank(createAppDataDto.getFormdataJson())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(!Constants.WORKFLOW_STATUS_D.equals(createAppDataDto.getStatus()) &&
					!Constants.WORKFLOW_STATUS_P.equals(createAppDataDto.getStatus())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_RANGE, null);
			}

//			//TODO 获取全局设置，验证
//            ApplySetting applySetting = appService.getSettingByAppId(app.getId(),Constants.COLLECTION_PUBLISH_SETTING);
//			//验证提交总量
//            int applyLimitTotal = applySetting.getApplyLimitTotal();
//            if(applyLimitTotal>0){
//                List<String> dataIds = formDataService.getAllDataId(app.getId(),app.getProcessInstanceId(),null,null);
//                if(dataIds.size()>applyLimitTotal){
//                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NUMBER_UPPER_LIMIT,null);
//                }
//            }
//            //验证每个月提交量
//            int currentApplyMonth = applySetting.getCurrentApplyMonth();
//            if(currentApplyMonth>0){
//                //获取当月的提交量
//                //构造当月的月初与月末date
//                Calendar cFirst = Calendar.getInstance();
//                cFirst.add(Calendar.MONTH, 0);
//                cFirst.set(Calendar.DAY_OF_MONTH,1);
//                cFirst.set(Calendar.HOUR_OF_DAY,cFirst.getActualMinimum(Calendar.HOUR_OF_DAY));
//                cFirst.set(Calendar.MINUTE,cFirst.getActualMinimum(Calendar.HOUR_OF_DAY));
//                cFirst.set(Calendar.SECOND,cFirst.getActualMinimum(Calendar.HOUR_OF_DAY));
//                Calendar cLast = Calendar.getInstance();
//                cLast.set(Calendar.DAY_OF_MONTH, cLast.getActualMaximum(Calendar.DAY_OF_MONTH));
//                cLast.set(Calendar.HOUR_OF_DAY,cLast.getActualMaximum(Calendar.HOUR_OF_DAY));
//                cLast.set(Calendar.MINUTE,cLast.getActualMaximum(Calendar.MINUTE));
//                cLast.set(Calendar.SECOND,cLast.getActualMaximum(Calendar.SECOND));
//
//                List<String> dataIds = formDataService.getAllDataId(app.getId(),app.getProcessInstanceId(),cFirst.getTime(),cLast.getTime());
//                if(dataIds.size()>applyLimitTotal){
//                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NUMBER_UPPER_LIMIT,null);
//                }
//            }
            if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus()) || Validators.fieldBlank(app.getProcessInstanceId())){
				return prepareReturnResult(ReturnCode.ERROR_UN_PUBLISH, null);
			}
			JSONArray data = JSON.parseArray(createAppDataDto.getFormdataJson());
			if(data == null){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_FORMAT, null);
			}
			WorkflowNode applyNode = workflowService.findApplyNode(app.getId(),app.getProcessInstanceId());
			if(applyNode == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			formDataService.saveData(app,data,applyNode,createAppDataDto.getStatus());
//			//TODO 申请创建成功的话，会有三种显示文案：http跳转、自定义显示内容、默认文案，需要返回给前端吧。
            Map<String,String> map = new HashMap<>();
//            map.put("applyCallback",""+applySetting.getApplyCallback());
//            map.put("applyCallbackContent",applySetting.getApplyCallbackContent());
//            map.put("applyCallbackLink",applySetting.getApplyCallbackLink());

            return prepareReturnResult(ReturnCode.CREATE_SUCCESS, map);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}
	@GetMapping("/form-data")
    @ApiOperation(value = "获取应用字段信息", httpMethod = "GET", notes = "获取应用字段信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getformData(String appId,boolean isPublish) {
		try {
			if(Validators.fieldBlank(appId) || Validators.fieldBlank(isPublish)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			List<Object> list = formDataService.getFormDataByAppId(appId, isPublish);
			return prepareReturnResult(ReturnCode.GET_SUCCESS, list);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@GetMapping("/form-data/{data-id}")
    @ApiOperation(value = "获取单据数据", httpMethod = "GET", notes = "获取单据数据")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getformData(@PathVariable("data-id") String dataId,String processInstanceId,String appId,String workflowNodeId) {
		try {
			if(Validators.fieldBlank(processInstanceId)
					|| Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			List<DBObject> list = formDataService.getAppDataById(dataId,processInstanceId,appId,workflowNodeId);
			return prepareReturnResult(ReturnCode.GET_SUCCESS, list);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}
	@GetMapping("/relate-data-types")
	@ApiOperation(value = "外部关联数据的表单列表;CUSTOMER是获取用户的外部数据;PROJECT获取外部项目信息;CONTRACT获取外部合同信息", httpMethod = "GET", notes = "关联外部已有数据")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
	public ResponseEntity<?> relateOhterTypesData() {
		try {
			//获取到所有的type
			return prepareReturnResult(ReturnCode.GET_SUCCESS,relateOtherService.getTypes());
		}catch (Exception e){
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@GetMapping("/relate-data-other")
	@ApiOperation(value = "外部关联数据的表单列表;CUSTOMER是获取用户的外部数据;PROJECT获取外部项目信息;CONTRACT获取外部合同信息", httpMethod = "GET", notes = "关联外部已有数据")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
	public ResponseEntity<?> relateOhterAppsData(@PathParam("type") String type) {
		try {
			//根据type查询一级列表
			if(Validators.fieldBlank(type))
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY,null);
			return prepareReturnResult(ReturnCode.GET_SUCCESS, relateOtherService.getLevelList(type));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@PostMapping("/relate-data-other")
	@ApiOperation(value = "根据表单和字段获取到外部关联数据的值", httpMethod = "POST", notes = "关联外部已有数据")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
	public ResponseEntity<?> relateOhterValueByIds(@RequestBody RelateDataDto relateDataDto) {
		try {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", JSON.toJSON( relateDataDto));

			if(Validators.fieldBlank(relateDataDto.getRelateAppId())||
					Validators.fieldBlank(relateDataDto.getRelateQueId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//如果筛选条件不为空，验证筛选条件
			if(relateDataDto.getConditions()!=null&&relateDataDto.getConditions().size()!=0){
				for (ConditionDto conditionDto : relateDataDto.getConditions()) {
					if(Validators.fieldBlank(conditionDto.getQueValue()) || Validators.fieldBlank(conditionDto.getRelatedQueId())){
						return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
					}
				}
			}
			return prepareReturnResult(ReturnCode.GET_SUCCESS, relateOtherService.getOtherRelateData(relateDataDto));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@PostMapping("/relate-data")
    @ApiOperation(value = "关联数据", httpMethod = "POST", notes = "关联已有数据")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> relateData(@RequestBody RelateDataDto relateDataDto) {
		try {
			if(Validators.fieldBlank(relateDataDto.getRelateAppId()) ||
					Validators.fieldBlank(relateDataDto.getRelateQueId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}

			//如果筛选条件不为空，验证筛选条件
            if(relateDataDto.getConditions()!=null&&relateDataDto.getConditions().size()!=0){
                for (ConditionDto conditionDto : relateDataDto.getConditions()) {
                    if(Validators.fieldBlank(conditionDto.getQueValue()) || Validators.fieldBlank(conditionDto.getRelatedQueId())){
                        return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
                    }
                }
            }
			App relateApp = appService.findById(relateDataDto.getRelateAppId());
			if(relateApp == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			Set<Object> list = formDataService.getRelateData(relateDataDto,relateApp);
			return prepareReturnResult(ReturnCode.GET_SUCCESS, list);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@PostMapping("/field/duplication")
    @ApiOperation(value = "字段重复验证", httpMethod = "POST", notes = "字段重复验证")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> validateDuplicate(@RequestBody DuplicateFiledDto duplicateFiledDto) {
		try {
			if(Validators.fieldBlank(duplicateFiledDto.getAppId()) ||
					Validators.fieldBlank(duplicateFiledDto.getQueId()) ||
					Validators.fieldBlank(duplicateFiledDto.getValue())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(duplicateFiledDto.getAppId());
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(formDataService.validateDuplicate(duplicateFiledDto,app)){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.CREATE_SUCCESS, null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}

	@PostMapping("/field/datetime/overlap")
    @ApiOperation(value = "起止时间重叠验证", httpMethod = "POST", notes = "起止时间重叠验证")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> validateOverlap(@RequestBody OverlapFiledDto overlapFiledDto) {
		try {
			if(Validators.fieldBlank(overlapFiledDto.getAppId()) ||
					Validators.fieldBlank(overlapFiledDto.getQueId()) ||
					Validators.fieldBlank(overlapFiledDto.getStartValue()) ||
					Validators.fieldBlank(overlapFiledDto.getEndValue())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(overlapFiledDto.getAppId());
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(formDataService.validateOverlap(overlapFiledDto,app)){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.CREATE_SUCCESS, null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}
}
