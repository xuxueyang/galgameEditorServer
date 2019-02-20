package com.fengyun.cube.workflow.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.fengyun.cube.workflow.service.BranchService;
import com.fengyun.cube.workflow.service.WorkflowService;
import com.fengyun.cube.workflow.service.dto.request.CreateBranchDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateBranchDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "工作流分支管理",description = "工作流分支管理")
@RestController
@RequestMapping("/api")
public class BranchResource extends BaseResource{

	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private AppService appService;
	
	@Autowired
	private BranchService branchService;
	
	@PostMapping("/workflow/branch")
    @ApiOperation(value = "添加分支", httpMethod = "POST", response = ReturnResultDTO.class, notes = "添加分支")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> createBranch(@RequestBody CreateBranchDto createBranchDto) {
		try {
			if(Validators.fieldBlank(createBranchDto.getAuditNodeId()) ||
					Validators.fieldBlank(createBranchDto.getAppId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(createBranchDto.getAppId());
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			WorkflowNode workflowNode = workflowService.findById(createBranchDto.getAuditNodeId());
			if(workflowNode == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.CREATE_SUCCESS,branchService.createBranch(createBranchDto,app));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}
	
	@PutMapping("/workflow/branch")
    @ApiOperation(value = "修改分支", httpMethod = "PUT", response = ReturnResultDTO.class, notes = "修改分支")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> updateBranch(@RequestBody UpdateBranchDto updateBranchDto) {
		try {
			if(Validators.fieldBlank(updateBranchDto.getAuditNodeId()) || Validators.fieldBlank(updateBranchDto.getAppId())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(updateBranchDto.getAppId());
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			
			WorkflowNode workflowNode = workflowService.findById(updateBranchDto.getAuditNodeId());
			if(workflowNode == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS,branchService.updateBranch(updateBranchDto,app));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_UPDATE, null);
		}
	}
	
	@GetMapping("/workflow/branch/{id}")
    @ApiOperation(value = "获取分支信息", httpMethod = "GET", response = ReturnResultDTO.class, notes = "获取分支信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getBranch(@PathVariable("id") String id) {
		try {
			WorkflowNode workflowNode = workflowService.findById(id);
			return prepareReturnResult(ReturnCode.GET_SUCCESS,workflowNode);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}
	
	@DeleteMapping("/workflow/branch/{id}")
    @ApiOperation(value = "删除分支信息", httpMethod = "DELETE", response = ReturnResultDTO.class, notes = "删除分支信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> deleteBranch(@PathVariable("id") String id,String appId) {
		try {
			if(Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(appId);
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			//判断分支是否存在
			WorkflowNode branch = branchService.findBranchById(id);
			if(branch == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			branchService.delete(id,app);
			return prepareReturnResult(ReturnCode.DELETE_SUCCESS,null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_DELETE, null);
		}
	}
}
