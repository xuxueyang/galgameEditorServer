package com.fengyun.cube.workflow.web.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fengyun.cube.core.constant.ReturnCode;
import com.fengyun.cube.core.dto.ReturnResultDTO;
import com.fengyun.cube.core.resource.BaseResource;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.App;
import com.fengyun.cube.workflow.domain.ApplySetting;
import com.fengyun.cube.workflow.service.AppService;
import com.fengyun.cube.workflow.service.dto.request.CreateAppDto;
import com.fengyun.cube.workflow.service.dto.request.QueryAppDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateAppDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateSettingDto;
import com.fengyun.cube.workflow.service.dto.response.AppDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "应用管理",description = "应用管理")
@RestController
@RequestMapping("/api")
public class AppResource extends BaseResource{

	@Autowired
	private AppService appService;

	@PostMapping("/app")
    @ApiOperation(value = "创建应用", httpMethod = "POST", response = ReturnResultDTO.class, notes = "创建应用")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> createApp(@RequestBody CreateAppDto createAppDto) {
		try {
			return prepareReturnResult(ReturnCode.CREATE_SUCCESS, appService.save(createAppDto));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}
    @PostMapping("/app/{id}")
    @ApiOperation(value = "根据有的应用复制新的应用", httpMethod = "POST", response = ReturnResultDTO.class, notes = "根据有的应用复制新的应用")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> copyAppByAppId(@PathVariable("id") String id) {
        try {
            App app = appService.findById(id);
            if(app == null||Constants.APP_STATUS_DELETE.equals(app.getStatus())){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
            }
            //未发布的不能设置复制
			if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus())){
				return prepareReturnResult(ReturnCode.ERROR_INVALID_STATUS_CODE, null);
			}
            return prepareReturnResult(ReturnCode.CREATE_SUCCESS, appService.copyAppByApp(app));
//            return prepareReturnResult(ReturnCode.CREATE_SUCCESS, "返回新的AppId");
        } catch (Exception e) {
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
        }
    }
	@PutMapping("/app")
    @ApiOperation(value = "更新应用", httpMethod = "PUT", response = ReturnResultDTO.class, notes = "更新应用")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> updateApp(@RequestBody UpdateAppDto updateAppDto) {
		try {
			if(Validators.fieldBlank(updateAppDto.getId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			App app = appService.findById(updateAppDto.getId());
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			appService.update(updateAppDto, app);
			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_UPDATE, null);
		}
	}

	@GetMapping("/app/{id}")
    @ApiOperation(value = "应用详情", httpMethod = "GET", notes = "应用详情")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<ReturnResultDTO<AppDto>> findDetail(@PathVariable("id") String id) {
		try {
			return prepareReturnResult(ReturnCode.GET_SUCCESS, appService.findDetail(id));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@GetMapping("/apps")
    @ApiOperation(value = "应用查询", httpMethod = "GET", notes = "应用查询")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<ReturnResultDTO<List<AppDto>>> findAll(QueryAppDto queryAppDto) {
		try {
			return prepareReturnResult(ReturnCode.GET_SUCCESS, appService.findAll(queryAppDto));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@DeleteMapping("/app/{id}")
    @ApiOperation(value = "应用删除", httpMethod = "DELETE", notes = "应用删除")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<ReturnResultDTO<?>> delete(@PathVariable("id") String id) {
		try {
			App app = appService.findById(id);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			appService.delete(id);
			return prepareReturnResult(ReturnCode.DELETE_SUCCESS,null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_DELETE, null);
		}
	}

	@PutMapping("/app/setting")
    @ApiOperation(value = "应用全局设置", httpMethod = "PUT", notes = "应用全局设置")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> updateSetting(@RequestBody UpdateSettingDto updateSettingDto) {
		try {
			if(Validators.fieldBlank(updateSettingDto.getId())
					|| Validators.fieldBlank(updateSettingDto.getAppId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			ApplySetting setting = appService.getSettingById(updateSettingDto.getId());
			if(setting == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			App app = appService.findById(updateSettingDto.getAppId());
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			appService.updateSetting(updateSettingDto);

			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_UPDATE, null);
		}
	}

	@GetMapping("/app/setting/{app-id}")
    @ApiOperation(value = "获取全局设置", httpMethod = "GET", notes = "获取应用全局设置")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getSetting(@PathVariable("app-id") String appId) {
		try {
			App app = appService.findById(appId);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.GET_SUCCESS, appService.getSettingByAppId(appId, Constants.COLLECTION_UN_PUBLISH_SETTING));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@PutMapping("/app/publish/{id}")
    @ApiOperation(value = "应用发布", httpMethod = "PUT", notes = "应用发布")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> publishApp(@PathVariable("id") String id) {
		try {
			App app = appService.findById(id);
			if(app == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if (Constants.APP_STATUS_PUBLISH.equals(app.getStatus())
					|| Constants.APP_STATUS_DELETE.equals(app.getStatus())) {// 应用已发布或删除
				return prepareReturnResult(ReturnCode.ERROR_PUBLISH, null);
			}
			//验证节点用户是否有配置用户信息
			if(!appService.checkNodeUsers(app.getId())){
				return prepareReturnResult(ReturnCode.ERROR_APP_CONFIG, null);
			}
			appService.publishApp(id);
			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, null);
		}
	}

}
