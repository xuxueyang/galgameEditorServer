package com.fengyun.cube.workflow.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

import javax.transaction.Transactional;

import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.rpc.uaa.client.CubeuaaClient;
import com.fengyun.cube.rpc.uaa.dto.CubeuaaRPCDto;
import com.fengyun.cube.rpc.uaa.dto.UserInfo;
import com.fengyun.cube.workflow.domain.Task;
import com.fengyun.cube.workflow.domain.WorkflowNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.WorkflowLog;

@Service
@Transactional
public class WorkflowLogService {

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
    private WorkflowService workflowService;
	@Autowired
    private TaskService taskService;
    @Autowired
    private CubeuaaClient cubeuaaClient;

    //记录操作日志
	public void saveLog(String appId, String processInstanceId, String currentUserId, String operation,String reason,String nodeId, String dataId) {
		WorkflowLog log = new WorkflowLog();
		log.setAppId(appId);
		log.setNodeId(nodeId);
		log.setOperationResult(operation);
		log.setOperator(currentUserId);
		log.setReason(reason);
		log.setApprovalDate(""+ZonedDateTime.now());
		log.setProcessInstanceId(processInstanceId);
		log.setDataId(dataId);
		mongoTemplate.save(log, Constants.COLLECTION_WORKFLOW_LOG);
	}

	//dataId是对于某个App发起的一个具体流程的实例，获取到这个实例下所有操作的日志
    public Map getLogs(String userId,String dataId) throws Exception{
        Query query = new Query(Criteria.where("dataId").is(dataId));
        List<WorkflowLog> workflowLogs = mongoTemplate.find(query, WorkflowLog.class, Constants.COLLECTION_WORKFLOW_LOG);
        if(workflowLogs!=null&&workflowLogs.size()>0){
            Map<String,Object> returnMap = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Collections.sort(workflowLogs, (a, b) -> {
                try {
                    return  sdf.parse(a.getApprovalDate()).before(sdf.parse(b.getApprovalDate()))==true?-1:1;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return -1;
                }

            });
            returnMap.put("appId",workflowLogs.get(0).getAppId());
            returnMap.put("dataId",workflowLogs.get(0).getDataId());
            returnMap.put("processInstanceId",workflowLogs.get(0).getProcessInstanceId());
            // 根据节点ID找到节点名字，根据operator找到对应的name，将对应的操作变成文案
            List<Map<String,Object>> resultList = new ArrayList<>();
            for (WorkflowLog workflow:workflowLogs) {
                Map<String,Object> map = new HashMap<>();
                WorkflowNode workflowNode =  workflowService.findById(workflow.getAppId(),workflow.getNodeId(),workflow.getProcessInstanceId());
                map.put("auditNodeName",workflowNode.getAuditNodeName());
                String operationName = "未知操作";
                switch (workflow.getOperationResult()){
                    case Constants.WORKFLOW_OPERRATION_P:
                        operationName="待处理";
                        break;
                    case Constants.WORKFLOW_OPERRATION_YES:
                        operationName="通过";
                        break;
                    case Constants.WORKFLOW_OPERRATION_CC:
                        operationName="抄送";
                        break;
                    case Constants.WORKFLOW_OPERRATION_NO:
                        operationName="拒绝";
                        break;
                    case Constants.WORKFLOW_OPERRATION_REVERT:
                        operationName="撤回";
                        break;
                    case Constants.WORKFLOW_OPERRATION_TRANSFER:
                        operationName="转移数据";
                        break;
                    case Constants.WORKFLOW_OPERRATION_APPLY:
                        operationName="申请";
                        break;
                }
                CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto(workflow.getOperator(), SecurityUtils.getCurrentTenantCode(), SecurityUtils.getCurrentSpaceCode());
                UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
                if(userInfo!=null&&userInfo.getNickName()!=null){
                    map.put("operationResult",userInfo.getNickName()+operationName);
                }else{
                    map.put("operationResult",operationName);
                }

                map.put("approvalDate",workflow.getApprovalDate());
                //获取操作人信息_暂时不需要
//                cubeuaaClient
                resultList.add(map);
            }
            //如果没有到最后的节点，那么显示下一个审批节点（如果存在的话),显示审批人
//            List<WorkflowNode> nextAuditNode = workflowService.getNextAuditNode(workflowLogs);
//            List<WorkflowNode> nextAuditNode = new ArrayList<>();
//            String nodeId = workflowLogs.get(workflowLogs.size()-1).getNodeId();
//            WorkflowNode byId = workflowService.findById(workflowLogs.get(0).getAppId(),
//                    nodeId,workflowLogs.get(0).getProcessInstanceId());
//            workflowService.searchNextNode(byId,
//                    workflowLogs.get(0).getAppId(),Constants.COLLECTION_PUBLISH_WORKFLOW, nextAuditNode, workflowLogs.get(0).getProcessInstanceId(), dataId);
//            if(nextAuditNode!=null&&nextAuditNode.size()>0){
//                Map<String,Object> map = new HashMap<>();
//                map.put("auditNodeName","");
//                map.put("operationResult"," 尚在审批中...");
//                map.put("approvalDate","");
//                resultList.add(map);
//            }else{
//                Map<String,Object> map = new HashMap<>();
//                map.put("auditNodeName","");
//                map.put("operationResult","审批结束");
//                map.put("approvalDate","");
//                resultList.add(map);
//            }
            {
                //TODO 获取到当前的Task们，如果有，获取到任务的操作ID，然后获取到名字，然后ok
                List<Task> tasks = taskService.getLatestTask(workflowLogs.get(0).getAppId(),dataId,workflowLogs.get(0).getProcessInstanceId());
                if(tasks!=null&&tasks.size()>0){
                    Map<String,Object> map = new HashMap<>();
                    map.put("auditNodeName","");
                    StringBuffer sb = new StringBuffer();
                    for(Task task:tasks){
                        String opeartorAccId = task.getOpeartorAccId();
                        CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto(opeartorAccId, SecurityUtils.getCurrentTenantCode(), SecurityUtils.getCurrentSpaceCode());
                        UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
                        if(userInfo!=null&&userInfo.getNickName()!=null)
                            sb.append( userInfo.getNickName()+ " ");
                    }
                    map.put("operationResult",sb.toString()+" 尚在审批中...");
                    map.put("approvalDate","");
                    resultList.add(map);
                }else{
                    Map<String,Object> map = new HashMap<>();
                    map.put("auditNodeName","");
                    map.put("operationResult","审批结束");
                    map.put("approvalDate","");
                    resultList.add(map);
                }
            }
            returnMap.put("result",resultList);
            return returnMap;
        }
        return null;
    }

    /**
     * 得到某个节点的第一个审批人
     * @param workflowNode
     * @param applyId
     * @return
     */
    private String getAuditPeopleName(WorkflowNode workflowNode,String applyId){

	    return "";
    }
}
