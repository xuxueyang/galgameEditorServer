package com.fengyun.cube.workflow.web.rest;

import com.fengyun.cube.core.constant.ReturnCode;
import com.fengyun.cube.core.resource.BaseResource;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.App;
//import com.fengyun.cube.workflow.domain.PushInfo;
import com.fengyun.cube.workflow.domain.Task;
import com.fengyun.cube.workflow.service.AppService;
//import com.fengyun.cube.workflow.service.PushService;
import com.fengyun.cube.workflow.service.TaskService;
import com.fengyun.cube.workflow.service.dto.request.TransferTaskDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateTaskDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "查询任务进度",description = "查询任务流的各项任务进度")
@RestController
@RequestMapping("/api")
public class TaskResource extends BaseResource {
    @Autowired
    private TaskService taskService;

    @Autowired
    private AppService appService;

//    @Autowired
//    private PushService pushService;

    @GetMapping("/task/all-finished")
    @ApiOperation(value = "获取已办事项", httpMethod = "GET", notes = "todo获取已办事项。处理人是自己")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getFinishedTask(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                //根据appId获取到全部的单据ID获取所有审批完（已通过和未通过）
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllFinished(app,acctId,userId,pageable));
            }else{
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllFinished(acctId,userId,pageable));
            }

//            测试一下邮件发送~~
//            PushInfo pushInfo = new PushInfo();
//            pushInfo.setPushResult(Constants.PUSH_INFO_RESULT_N);
//            pushInfo.setPushType(Constants.PUSH_TYPE_MAIL);
//            pushInfo.setPushAddress("1059738716@qq.com");
//            pushInfo.setPushValue("推送消息");
//            pushService.push(pushInfo);
//            return prepareReturnResult(ReturnCode.CREATE_SUCCESS,null);
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }

    @GetMapping("/task/all-pass")
    @ApiOperation(value = "已通过的", httpMethod = "GET", notes = "已通过的。发起人是自己")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getPassedTask(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllPassTask(app,userId,pageable));
            }else{
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllPassTask(userId,pageable));
            }

        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/reject")
    @ApiOperation(value ="已拒绝", httpMethod = "GET", notes = "已拒绝。发起人是自己")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getUnPassedTask(String appId,Pageable pageable){
        try{
            //如果appId为空那么查出所有的
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllUnPassTask(app,userId,pageable));
            }else{
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllUnPassTask(userId,pageable));
            }
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/all-resolving")
    @ApiOperation(value = "流程中", httpMethod = "GET", notes = "显示我申请的还在流程中的")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getInResolving(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllInResolvingTask(app,userId,pageable));
            }else{
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.getAllInResolvingTask(userId,pageable));
            }

        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/all-carbon")
    @ApiOperation(value = "显示抄送节点", httpMethod = "GET", notes = "todo 显示抄送节点。在节点的人员列表中")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getCarbonCopyTask(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllCarbonCopy(app,acctId,userId,pageable));
            }else{
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllCarbonCopy(acctId,userId,pageable));
            }
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/my-task")
    @ApiOperation(value = "显示待办事项", httpMethod = "GET", notes = "显示待办事项。是需要我处理的")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getTodoTask(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String userId = SecurityUtils.getCurrentUserId();
                String acctId = SecurityUtils.getCurrentAcctId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllTodoTasks(app,acctId,userId,pageable));
            }else {
                String userId = SecurityUtils.getCurrentUserId();
                String acctId = SecurityUtils.getCurrentAcctId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllTodoTasks(acctId,userId,pageable));
            }
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/draft")
    @ApiOperation(value = "显示草稿", httpMethod = "GET", notes = "显示草稿。暂存的单据")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getDraft(String appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllDrafts(app,acctId,userId,pageable));
            }else{
                String acctId = SecurityUtils.getCurrentAcctId();
                String userId = SecurityUtils.getCurrentUserId();
                return prepareReturnResult(ReturnCode.GET_SUCCESS,taskService.findAllDrafts(acctId,userId,pageable));
            }
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }
    @GetMapping("/task/unperfect")
    @ApiOperation(value = "显示待完善的", httpMethod = "GET", notes = "显示待完善的。由下一个节点审批撤回")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功")})
    public ResponseEntity getPrefect(String  appId,Pageable pageable){
        try{
            if(Validators.fieldNotBlank(appId)){
                App app = appService.findById(appId);
                if(app==null){
                    return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
                }
//            if(!taskService.checkRevertPermission(task)){
//                return prepareReturnResult(ReturnCode.ERROR_NO_PERMISSIONS_UPDATE,null);
//            }
                List<Task> allunPrefect = taskService.getAllunPrefect(app.getId(),pageable);
                return prepareReturnResult(ReturnCode.GET_SUCCESS,allunPrefect);
            }else{
                List allunPrefect = taskService.getAllunPrefect(pageable);
                return prepareReturnResult(ReturnCode.GET_SUCCESS,allunPrefect);
            }

        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_QUERY,null);
        }
    }

    @PostMapping("/task/pass")
    @ApiOperation(value = "通过任务",httpMethod = "POST",notes = "通过任务")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "成功")})
    public ResponseEntity passTask(@RequestBody UpdateTaskDto updateTaskDto){
        try{
            Task task = taskService.findTaskById(updateTaskDto.getTaskId());
            if(task==null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
            }
            taskService.passTask(updateTaskDto.getAppId(),task,updateTaskDto.getUpdateDataDtoList());
            return prepareReturnResult(ReturnCode.UPDATE_SUCCESS,null);
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_UPDATE,null);
        }
    }
    @PostMapping("/task/refuse")
    @ApiOperation(value = "拒绝任务",httpMethod = "POST",notes = "拒绝任务")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "成功")})
    public ResponseEntity refuseTask(@RequestBody UpdateTaskDto updateTaskDto){
        try{
            Task task = taskService.findTaskById(updateTaskDto.getTaskId());
            if(task==null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
            }
            taskService.unpassTask(updateTaskDto.getAppId(),task,updateTaskDto.getUpdateDataDtoList());
            return prepareReturnResult(ReturnCode.UPDATE_SUCCESS,null);
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_UPDATE,null);
        }
    }
    @PostMapping("/task/revert")
    @ApiOperation(value = "撤回",httpMethod = "POST",notes = "拒绝任务")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "成功")})
    public ResponseEntity revertTask(@RequestBody UpdateTaskDto updateTaskDto){
        try{
            Task task = taskService.findTaskById(updateTaskDto.getTaskId());
            if(task==null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
            }
            //验证任务的撤回权限
            if(!taskService.checkRevertPermission(task)){
                return prepareReturnResult(ReturnCode.ERROR_NO_PERMISSIONS_UPDATE,null);
            }
            taskService.revertTask(updateTaskDto.getAppId(),task,updateTaskDto.getUpdateDataDtoList());
            return prepareReturnResult(ReturnCode.UPDATE_SUCCESS,null);
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_UPDATE,null);
        }
    }
    @PostMapping("/task/transfer")
    @ApiOperation(value = "转交给其他人员",httpMethod = "POST",notes = "拒绝任务")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "成功")})
    public ResponseEntity transferTask(@RequestBody TransferTaskDto transferTaskDto){
        try{
            Task task = taskService.findTaskById(transferTaskDto.getTaskId());
            if(task==null){
                return prepareReturnResult(ReturnCode.ERROR_RESOURCE_NOT_EXIST_CODE,null);
            }
            taskService.transferTask(transferTaskDto.getAppId(),task,transferTaskDto.getTransferList());
            return prepareReturnResult(ReturnCode.UPDATE_SUCCESS,null);
        }catch (Exception e){
            LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "", e);
            return prepareReturnResult(ReturnCode.ERROR_UPDATE,null);
        }
    }
}
