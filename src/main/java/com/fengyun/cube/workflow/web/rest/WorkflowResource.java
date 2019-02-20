package com.fengyun.cube.workflow.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import com.fengyun.cube.workflow.service.WorkflowService;
import com.fengyun.cube.workflow.service.dto.request.CreateBranchDto;
import com.fengyun.cube.workflow.service.dto.request.WorkflowDto;
import com.fengyun.cube.workflow.service.dto.request.WorkflowNodeDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

@Api(value = "工作流节点管理",description = "工作流节点管理")
@RestController
@RequestMapping("/api")
public class WorkflowResource extends BaseResource{

	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private AppService appService;


	@PostMapping("/workflow/node")
    @ApiOperation(value = "添加节点", httpMethod = "POST", response = ReturnResultDTO.class, notes = "添加节点")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> createNode(@RequestBody WorkflowDto workflowDto) {
		try {
			if(Validators.fieldBlank(workflowDto.getType())
					|| Validators.fieldBlank(workflowDto.getAppId())
					|| Validators.fieldBlank(workflowDto.getPreNodeId())
					|| Validators.fieldBlank(workflowDto.getRoot())){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(workflowDto.getAppId());
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			WorkflowNode preNode = workflowService.findById(workflowDto.getPreNodeId());
			if(preNode == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.CREATE_SUCCESS, workflowService.createWorkflowNode(workflowDto,workflowDto.getRoot(),app));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_CREATE, null);
		}
	}
//	@GetMapping("/t/aaa")
//	@ApiResponses(value = {@ApiResponse(code = 200,message = "成功")})
//	public ResponseEntity wqeq(){
//		try{
//			workflowService.test1();
//			return null;
//		}catch (Exception e){
//			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
//			return prepareReturnResult(ReturnCode.ERROR_UPDATE,null);
//		}
//	}
	@PutMapping("/workflow/node")
    @ApiOperation(value = "更新节点信息", httpMethod = "PUT", response = ReturnResultDTO.class, notes = "更新节点信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> updateNode(@RequestBody WorkflowNodeDto workflowNodeDto) {
		try {
			if (Validators.fieldBlank(workflowNodeDto.getType())
					|| Validators.fieldBlank(workflowNodeDto.getAppId())
					|| Validators.fieldBlank(workflowNodeDto.getAuditNodeId())) {
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(workflowNodeDto.getAppId());
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			WorkflowNode node = workflowService.findById(workflowNodeDto.getAuditNodeId());
			if(node == null){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			return prepareReturnResult(ReturnCode.UPDATE_SUCCESS, workflowService.updateNode(workflowNodeDto,app));
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_UPDATE, null);
		}
	}
//    @GetMapping("/workflow/{appId}/{preNodeId}/{processId}")
//    @ApiOperation(value = "测试", httpMethod = "GET", response = ReturnResultDTO.class, notes = "测试")
//    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
//    public ResponseEntity<?> getNextNode(@PathVariable("appId") String appId,@PathVariable("preNodeId") String preNodeId,
//                                         @PathVariable("processId") String processId) {
//        try {
//            WorkflowNode node = workflowService.findById(preNodeId);
//            List<WorkflowNode> list = new ArrayList<>();
//            workflowService.searchNextNode(node,appId,Constants.COLLECTION_PUBLISH_WORKFLOW,list,processId,"");
//            return prepareReturnResult(ReturnCode.GET_SUCCESS,list);
//        } catch (Exception e) {
//            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
//            return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
//        }
//    }

	@GetMapping("/workflow/node/{id}")
    @ApiOperation(value = "获取节点信息", httpMethod = "GET", response = ReturnResultDTO.class, notes = "获取节点信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getNode(@PathVariable("id") String id) {
		try {
			WorkflowNode node = workflowService.findById(id);
			return prepareReturnResult(ReturnCode.GET_SUCCESS, node);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
		}
	}

	@DeleteMapping("/workflow/node/{id}")
    @ApiOperation(value = "删除节点", httpMethod = "DELETE", response = ReturnResultDTO.class, notes = "删除节点")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> deleteNode(@PathVariable("id") String id,String appId) {
		try {
			if(Validators.fieldBlank(appId)){
				return prepareReturnResult(ReturnCode.ERROR_FIELD_EMPTY, null);
			}
			//验证有效性
			App app = appService.findById(appId);
			if(app == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}

			WorkflowNode node = workflowService.findById(id);
			if(node == null ){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE, null);
			}
			if(Constants.WORKFLOW_TYPE_APPLY == node.getType()){
				return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_ALLOWED_DELETE, null);
			}
			workflowService.delete(node,app);
			return prepareReturnResult(ReturnCode.DELETE_SUCCESS,null);
		} catch (Exception e) {
			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
 			return prepareReturnResult(ReturnCode.ERROR_DELETE, null);
		}
	}
}
