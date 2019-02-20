package com.fengyun.cube.workflow.web.rest;


import com.fengyun.cube.core.constant.ReturnCode;
import com.fengyun.cube.core.dto.ReturnResultDTO;
import com.fengyun.cube.core.resource.BaseResource;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.service.AppService;
import com.fengyun.cube.workflow.service.WorkflowLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "工作流节点日志管理",description = "工作流节点日志管理")
@RestController
@RequestMapping("/api")
public class WorkflowLogResource extends BaseResource {
    @Autowired
    private WorkflowLogService workflowLogService;;


    @GetMapping("/workflowlog/{dataId}")
    @ApiOperation(value = "获取日志信息", httpMethod = "GET", response = ReturnResultDTO.class, notes = "获取日志信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity<?> getNode(@PathVariable("dataId") String dataId) {
        try {
            //返回一个排好序的数组——每个里面含有：节点ID、节点名称 、更新时间、操作
            if(Validators.fieldBlank(dataId)){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
            }
            String userId = SecurityUtils.getCurrentUserId();
            return prepareReturnResult(ReturnCode.GET_SUCCESS, workflowLogService.getLogs(userId,dataId));
        } catch (Exception e) {
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY, null);
        }
    }
}
