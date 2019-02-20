package com.fengyun.cube.workflow.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fengyun.cube.workflow.domain.*;
import com.fengyun.cube.workflow.service.dto.request.UpdateDataDto;
import com.mongodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.workflow.config.Constants;

@Transactional
@Service
public class TaskService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkflowLogService workflowLogService;

    @Autowired
    private FormDataService formDataService;

    private List<App> getAllApps(){
        Query queryApp = new Query();
        queryApp.addCriteria(Criteria.where("status").ne(Constants.APP_STATUS_DELETE)
                .and("status").ne(Constants.APP_STATUS_UN_PUBLISH));
        return  mongoTemplate.find(queryApp, App.class, Constants.COLLECTION_APP);
    }

    public List<List> findAllFinished(String acctId, String userId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(findAllFinished(app,acctId,userId,pageable));
            }
        }
        return lists;
    }
//    /**
//            * 带有查询条件的分页查询
//          * @param criteria
//          * @param pageResult
//          * @return
//     */
//    public PageResult listByPage(Criteria criteria, PageResult pageResult) {
//        if (!pageResult.getOrderBy().equals("")
//                && !pageResult.getOrderBy().equals(null)) {
//            if ("desc".compareToIgnoreCase(pageResult.getSort()) == 0)
//                criteria.addOrder(Order.desc(pageResult.getOrderBy()));
//            else {
//                criteria.addOrder(Order.asc(pageResult.getOrderBy()));
//            }
//        }
//        int counts = criteria.list().size();
//        criteria.setFirstResult(pageResult.getFirstRec());
//        criteria.setMaxResults(pageResult.getPageSize());
//        pageResult.setList(criteria.list());
//        int pageSize = pageResult.getPageSize();
//        int pages = getPages(counts, pageSize);
//        pageResult.setRecords(counts);
//        pageResult.setPages(pages);
//        return pageResult;
//    }
    //得到当前App下，【当前用户】已操作的。需要和【我申请】的概念区分。
    public List findAllFinished(App app, String acctId, String userId,Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("operateResult").ne(Constants.WORKFLOW_OPERRATION_P),Criteria.where("operateResult").ne(Constants.WORKFLOW_OPERRATION_CC));
        String id = "";
        if(acctId!=null&&!"".equals(acctId)){
            id = acctId;
        }else if(userId!=null&&!"".equals(userId)){
            id = userId;
        }
        if(!"".equals(id)){
            query.addCriteria(Criteria.where("opeartorAccId").is(id));
        }
        query.with(pageable);
        List<Task> ownTasks = mongoTemplate.find(query.addCriteria(criteria), Task.class, Constants.COLLECTION_WORKFLOW_TASK);
        if(ownTasks==null){
            ownTasks = new ArrayList<>();
        }
//        //判断当前用户是否有权限处理
//        for (int i = 0; i < tasks.size(); i++) {
//            if (checkTaskPremmsion(tasks.get(i), acctId, userId)) {
//                ownTasks.add(tasks.get(i));
//            }
//        }
        //返回编号、节点状态、表单数据

        List<Map<String, Object>> lists = new ArrayList<>();

        List<String> taskDataIds = new ArrayList<>();
        for(int i=ownTasks.size()-1;i>=0;i--){
            Task task = ownTasks.get(i);
            if(taskDataIds.contains(ownTasks.get(i).getDataId())){
                continue;
            }else{
                taskDataIds.add(ownTasks.get(i).getDataId());
            }
            String nodeId  = task.getCurrentNodeId();
            String processInstanceId = task.getProcessInstanceId();
            WorkflowNode workflowNode = workflowService.findById(app.getId(),nodeId,processInstanceId);
            Map<String, Object> map = new HashMap<>();
//            if(workflowNode!=null){
//                //TODO 要根据任务，获取到最新的节点名称！！！
//                map.put("aduitNodeName",workflowNode.getAuditNodeName());
//                map.put("auditNodeId",workflowNode.getAuditNodeId());
//            }
            {
                WorkflowNode latestNode = getLatestTaskInNode(task.getAppId(), task.getDataId(), task.getProcessInstanceId());
                if(latestNode!=null){
                    map.put("aduitNodeName",latestNode.getAuditNodeName());
                    map.put("auditNodeId",latestNode.getAuditNodeId());
                }else{
                    //需要找到有没有已完成或者未完成的
                    Query queryTask = new Query(
                            Criteria.where("appId").is(task.getAppId()).and("dataId").is(task.getDataId()).and("operateResult")
                                    .is(Constants.WORKFLOW_OPERRATION_NO).and("processInstanceId").is(processInstanceId));
                    List<Task> tasks = mongoTemplate.find(queryTask, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
                    if(tasks!=null&&tasks.size()>0){
                        //有已拒绝的
                        map.put("aduitNodeName","审批结束。未通过");
                        map.put("auditNodeId",workflowNode.getAuditNodeId());
                    }else{
                        //没有未通过的和完成中的，说明是已通过
                        map.put("aduitNodeName","审批结束。已通过");
                        map.put("auditNodeId",workflowNode.getAuditNodeId());
                    }
//                    map.put("aduitNodeName",workflowNode.getAuditNodeName());
//                    map.put("auditNodeId",workflowNode.getAuditNodeId());
                }
            }
            map.put("dataId",task.getDataId());
            map.put("taskId",task.getId());
            map.put("processInstanceId",task.getProcessInstanceId());
            map.put("appId",app.getId());
            map.put("appName",app.getName());
            map.put("result",getFormData(app.getId(),task.getDataId(),workflowNode));
            lists.add(map);
        }

        return lists;
    }
    public WorkflowNode getLatestTaskInNode(String appId,String dataId,String processInstanceId ){
        List<Task> latestTask = getLatestTask(appId, dataId, processInstanceId);
        if(latestTask!=null&&latestTask.size()>0){
            String nodeId  = latestTask.get(0).getCurrentNodeId();
            WorkflowNode workflowNode = workflowService.findById(latestTask.get(0).getAppId(),nodeId,processInstanceId);
            return workflowNode;
        }else{
            return null;
        }
    }
    public List<Task> getLatestTask(String appId,String dataId,String processInstanceId ){
        Query queryTask = new Query(
                Criteria.where("appId").is(appId).and("dataId").is(dataId).and("operateResult")
                        .is(Constants.WORKFLOW_OPERRATION_P).and("processInstanceId").is(processInstanceId));
        return  mongoTemplate.find(queryTask, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
    }

    public List<List> getAllPassTask(String createdId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(getAllPassTask(app,createdId,pageable));
            }
        }
        return lists;
    }

    //获取到我申请的已通过
    public List getAllPassTask(App app, String createdId,Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("queId").is(Constants.QUE_ID_CREATED_ID),
            Criteria.where("value").is(createdId),
            Criteria.where("status").is(Constants.WORKFLOW_STATUS_Y));
        query.with(pageable);
        List<BasicDBObject> ts = mongoTemplate.find(query.addCriteria(criteria), BasicDBObject.class, Constants.COLLECTION_APP_DATA);
        //获取到单据的配置
        List<Map<String,Object>> lists = new ArrayList<>();
        for (BasicDBObject object : ts) {
        	if(object.get("dataId")!= null&&object.get("processInstanceId")!= null){
        		String dataId =object.get("dataId").toString();
//        		Query dataquery = new Query(Criteria.where("appId").is(appId)
//        				.and("status").is(Constants.WORKFLOW_STATUS_Y)
//        				.and("dataId").is(dataId));
//
//        		List<DBObject> data = mongoTemplate.find(dataquery, DBObject.class,  Constants.COLLECTION_APP_DATA);
//        		lists.add(data);
                Map<String,Object> map = new HashMap<>();
                map.put("dataId",dataId);
                map.put("processInstanceId",object.get("processInstanceId"));
                map.put("appId",app.getId());
                map.put("appName",app.getName());
                map.put("result",getFormData(app.getId(),dataId,null));
        		lists.add(map);
        	}
        }
        return lists;
    }

    //找出所有App下的【我申请】的未通过的
    public List getAllUnPassTask(String createdId,Pageable pageable){
        List<App> apps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(apps!=null&&apps.size()>0){
            for(App app:apps){
                lists.add(getAllUnPassTask(app,createdId,pageable));
            }
        }
        return  lists;
    }

    //获取到【我申请的】未通过的
    public List getAllUnPassTask(App app, String createdId,Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("queId").is(Constants.QUE_ID_CREATED_ID),
            Criteria.where("value").is(createdId),
            Criteria.where("status").is(Constants.WORKFLOW_STATUS_N));
        query.with(pageable);
        List<BasicDBObject> ts = mongoTemplate.find(query.addCriteria(criteria), BasicDBObject.class, Constants.COLLECTION_APP_DATA);
        //获取到单据的配置
        List<Map> lists = new ArrayList<>();
        for (BasicDBObject object : ts) {
        	if(object.get("dataId")!= null&&object.get("processInstanceId")!= null){
        		String dataId =object.get("dataId").toString();
//        		Query dataquery = new Query(Criteria.where("appId").is(appId)
//        				.and("status").is(Constants.WORKFLOW_STATUS_N)
//        				.and("dataId").is(dataId));
//
//        		List<DBObject> data = mongoTemplate.find(dataquery, DBObject.class,  Constants.COLLECTION_APP_DATA);
//        		lists.add(data);
                Map<String,Object> map = new HashMap<>();
                map.put("dataId",dataId);
                map.put("appId",app.getId());
                map.put("appName",app.getName());
                map.put("processInstanceId",object.get("processInstanceId"));
                map.put("result",getFormData(app.getId(),dataId,null));
                lists.add(map);
            }
        }
        return lists;
    }
    //对于这种没有App的，默认找出所有的在我申请还在处理中的
    public List<List> getAllInResolvingTask(String createdId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(getAllInResolvingTask(app,createdId,pageable));
            }
        }
        return lists;
    }

    //获取到我申请的还在处理中的
    public List getAllInResolvingTask(App app, String createdId,Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("queId").is(Constants.QUE_ID_CREATED_ID),
            Criteria.where("value").is(createdId),
            Criteria.where("status").is(Constants.WORKFLOW_STATUS_P));
        query.with(pageable);
        List<BasicDBObject> ts = mongoTemplate.find(query.addCriteria(criteria), BasicDBObject.class, Constants.COLLECTION_APP_DATA);
        //获取到单据的配置
        List<Map<String,Object>> lists = new ArrayList<>();
        for (BasicDBObject object : ts) {
        	 Map<String,Object> map = new HashMap<>();
        	if(object.get("dataId")!= null && object.get("processInstanceId") != null){
        		String dataId =object.get("dataId").toString();
        		String processInstanceId = object.get("processInstanceId").toString();
//        		Query dataquery = new Query(Criteria.where("appId").is(appId)
//        				.and("status").is(Constants.WORKFLOW_STATUS_P)
//        				.and("dataId").is(dataId));
//
//        		List<DBObject> data = mongoTemplate.find(dataquery, DBObject.class,  Constants.COLLECTION_APP_DATA);
        		map.put("result",getFormData(app.getId(),dataId,null));
        		map.put("appId", app.getId());
                map.put("appName", app.getName());
                map.put("processInstanceId",processInstanceId);
                map.put("dataId", dataId);

        		Query queryTask = new Query(
						Criteria.where("appId").is(app.getId()).and("dataId").is(dataId).and("operateResult")
								.is(Constants.WORKFLOW_OPERRATION_P).and("processInstanceId").is(processInstanceId));
				List<Task> tasks = mongoTemplate.find(queryTask, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
				//任务去重
				List<WorkflowNode> nodes = new ArrayList<>();
				if(tasks != null && tasks.size()>0){
					Set<String> nodeIds = new HashSet<>();
					for (Task task : tasks) {
						nodeIds.add(task.getCurrentNodeId());
					}
					for (String currentNodeId : nodeIds) {
						Query queryNode = new Query(Criteria.where("auditNodeId").is(currentNodeId));
		                queryNode.addCriteria(Criteria.where("processInstanceId").is(processInstanceId));
		                queryNode.addCriteria(Criteria.where("appId").is(app.getId()));
		                WorkflowNode workflowNode = mongoTemplate.findOne(queryNode,WorkflowNode.class,Constants.COLLECTION_PUBLISH_WORKFLOW);
		                if(workflowNode!= null){
		                	nodes.add(workflowNode);
		                }
					}
				}
				map.put("currentWorkflowNode", nodes);
				lists.add(map);
        	}
        }
        return lists;
    }

    //得到表单数据
    private List getFormData(String appId, String dataId,WorkflowNode workflowNode) {
        List<Object> result = new ArrayList<>();
        List<AuditEdit> hideQues =null;
        if(workflowNode!=null)
            hideQues = workflowNode.getAuditHides(); //隐藏字段
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(appId),
            Criteria.where("dataId").is(dataId));
        //含有各种表单，以及申请人、申请时间、更新时间
        List<DBObject> objects = mongoTemplate.find(query.addCriteria(criteria), DBObject.class, Constants.COLLECTION_APP_DATA);
        for (DBObject object : objects) {
//            Map<String,Object> eachMap = new HashMap<>();
            boolean canHide = false;
            //去掉描述的字段去（前段不需要显示所以删除）
            if("description".equals(object.get("type"))){
                canHide = true;
            }
            //去掉隐藏字段
            if(hideQues!= null && hideQues.size()>0){
                for (AuditEdit hideQue : hideQues) {
                    if(hideQue.getQueId().equals(object.get("queId").toString())){
                        canHide = true;
                        break;
                    }
                    List<DBObject> children = (List<DBObject>) object.get("children");//存在子节点信息
                    //对于子节点，删除需要的字段即可
                    if(children != null && children.size()>0){
                        for (DBObject dbObject:children) {
                            if (hideQue.getQueId().equals(dbObject.get("queId").toString())) {
                                children.remove(dbObject);
                                break;
                            }
                        }
                    }
                }
            }
            if(!canHide){
                result.add(object);
            }
        }
        return result;
    }

    public List<List> findAllCarbonCopy( String acctId, String userId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(findAllCarbonCopy(app,acctId,userId,pageable));
            }
        }
        return lists;
    }

    public List<Map<String, Object>> findAllCarbonCopy(App app, String acctId, String userId,Pageable pageable) {
        Query query = new Query(Criteria.where("operateResult").is(Constants.WORKFLOW_OPERRATION_CC).andOperator(
            Criteria.where("appId").is(app.getId())
        ));
        String id = "";
        if(acctId!=null&&!"".equals(acctId)){
            id = acctId;
        }else if(userId!=null&&!"".equals(userId)){
            id = userId;
        }
        if(!"".equals(id)){
            query.addCriteria(Criteria.where("opeartorAccId").is(id));
        }
        query.with(pageable);
        List<Task> ownTasks = mongoTemplate.find(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
//        //判断当前用户是否有权限处理
//        for (int i = 0; i < tasks.size(); i++) {
//            if (checkTaskPremmsion(tasks.get(i), acctId, userId)) {
//                ownTasks.add(tasks.get(i));
//            }
//        }

        //返回编号、节点状态、表单数据
        List<Map<String, Object>> lists = new ArrayList<>();
        for(Task task:ownTasks){
            String nodeId  = task.getCurrentNodeId();
            String processInstanceId = task.getProcessInstanceId();
            WorkflowNode workflowNode = workflowService.findById(app.getId(),nodeId,processInstanceId);
            Map<String, Object> map = new HashMap<>();
//            if(workflowNode!=null){
//                map.put("aduitNodeName",workflowNode.getAuditNodeName());
//                map.put("auditNodeId",workflowNode.getAuditNodeId());
//            }
            {
                WorkflowNode latestNode = getLatestTaskInNode(task.getAppId(), task.getDataId(), task.getProcessInstanceId());
                if(latestNode!=null){
                    map.put("aduitNodeName",latestNode.getAuditNodeName());
                    map.put("auditNodeId",latestNode.getAuditNodeId());
                }else{
                    //需要找到有没有已完成或者未完成的
                    Query queryTask = new Query(
                            Criteria.where("appId").is(task.getAppId()).and("dataId").is(task.getDataId()).and("operateResult")
                                    .is(Constants.WORKFLOW_OPERRATION_NO).and("processInstanceId").is(processInstanceId));
                    List<Task> tasks = mongoTemplate.find(queryTask, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
                    if(tasks!=null&&tasks.size()>0){
                        //有已拒绝的
                        map.put("aduitNodeName","审批结束。未通过");
                        map.put("auditNodeId",workflowNode.getAuditNodeId());
                    }else{
                        //没有未通过的和完成中的，说明是已通过
                        map.put("aduitNodeName","审批结束。已通过");
                        map.put("auditNodeId",workflowNode.getAuditNodeId());
                    }
//                    map.put("aduitNodeName",workflowNode.getAuditNodeName());
//                    map.put("auditNodeId",workflowNode.getAuditNodeId());
                }
            }
            map.put("dataId",task.getDataId());
            map.put("taskId",task.getId());
            map.put("processInstanceId",task.getProcessInstanceId());
            map.put("appId",app.getId());
            map.put("appName",app.getName());
            map.put("result",getFormData(app.getId(),task.getDataId(),workflowNode));
            lists.add(map);
        }
        return lists;
    }

    public Task findTaskById(String taskId) {
        Query query = new Query(Criteria.where("id").is(taskId));
        return mongoTemplate.findOne(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
    }

    public List<List> findAllTodoTasks( String acctId, String userId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(findAllTodoTasks(app,acctId,userId,pageable));
            }
        }
        return lists;
    }

    public List<Map<String, Object>> findAllTodoTasks(App app, String acctId, String userId,Pageable pageable) {
        Query query = new Query(Criteria.where("operateResult").is(Constants.WORKFLOW_OPERRATION_P).andOperator(
            Criteria.where("appId").is(app.getId())
        ));
        String id = "";
        if(acctId!=null&&!"".equals(acctId)){
            id = acctId;
        }else if(userId!=null&&!"".equals(userId)){
            id = userId;
        }
        if(!"".equals(id)){
            query.addCriteria(Criteria.where("opeartorAccId").is(id));
        }
        query.with(pageable);
        List<Task> ownTasks = mongoTemplate.find(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
//        //判断当前用户是否有权限处理
//        for (int i = 0; i < tasks.size(); i++) {
//            if (checkTaskPremmsion(tasks.get(i), acctId, userId)) {
//                ownTasks.add(tasks.get(i));
//            }
//        }
        //返回编号、节点状态、表单数据
        List<Map<String, Object>> lists = new ArrayList<>();
        for(Task task:ownTasks){
            String nodeId  = task.getCurrentNodeId();
            String processInstanceId = task.getProcessInstanceId();
            WorkflowNode workflowNode = workflowService.findById(app.getId(),nodeId,processInstanceId);
            Map<String, Object> map = new HashMap<>();
            if(workflowNode!=null){
                map.put("auditNodeId",workflowNode.getAuditNodeId());
                map.put("aduitNodeName",workflowNode.getAuditNodeName());
            }
            map.put("dataId",task.getDataId());
            map.put("taskId",task.getId());
            map.put("processInstanceId",task.getProcessInstanceId());
            map.put("appId",app.getId());
            map.put("appName",app.getName());
            map.put("result",getFormData(app.getId(),task.getDataId(),workflowNode));
            lists.add(map);
        }
        return lists;
    }
    public List<List> findAllDrafts( String acctId, String userId,Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(findAllDrafts(app,acctId,userId,pageable));
            }
        }
        return lists;
    }

    public List findAllDrafts(App app, String acctId, String userId,Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("queId").is(Constants.QUE_ID_CREATED_ID),
            Criteria.where("value").is(userId),
            Criteria.where("status").is(Constants.WORKFLOW_STATUS_D));
        query.with(pageable);
        List<BasicDBObject> ts = mongoTemplate.find(query.addCriteria(criteria), BasicDBObject.class, Constants.COLLECTION_APP_DATA);
        //草稿采取默认的申请节点的表单配置
        Query queryNodeConfig = new Query();
        Criteria criteriaNodeConfig = new Criteria();
        criteriaNodeConfig.andOperator(Criteria.where("appId").is(app.getId()),
            Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
            Criteria.where("processInstanceId").is(app.getProcessInstanceId()));
        queryNodeConfig.addCriteria(criteriaNodeConfig);
        WorkflowNode applyNode = mongoTemplate.findOne(queryNodeConfig, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
        //获取到单据的配置
        List<Map> lists = new ArrayList<>();
        for (BasicDBObject object : ts) {
            Map<String,Object> map = new HashMap<>();
            map.put("result",getFormData(app.getId(), object.get("dataId").toString(),applyNode));
            map.put("appId",app.getId());
            map.put("appName",app.getName());
            map.put("dataId",object.get("dataId"));
            map.put("processInstanceId",app.getProcessInstanceId());
            map.put("auditNodeId",applyNode.getAuditNodeId());
            lists.add(map);
        }
        return lists;
    }

    //撤回任务
    public void revertTask(String appId, Task task, List<UpdateDataDto> updateDataDtoList) throws Exception{
        updateData(appId,task.getDataId(),updateDataDtoList);
        //将上一个节点状态设置为待审批
        WorkflowNode workflowNode = workflowService.findById(task.getCurrentNodeId());
        if(!workflowNode.isRevert()){
            return;//不允许回退
        }
        task.setOperateResult(Constants.WORKFLOW_OPERRATION_REVERT);
        mongoTemplate.save(task);

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("dataId").is(task.getDataId()),
            Criteria.where("currentNodeId").is(workflowNode.getPreNodeId()),
            Criteria.where("appId").is(appId),
            Criteria.where("processInstanceId").is(task.getProcessInstanceId()));
        Update update = new Update();
        update.set("operateResult", Constants.WORKFLOW_OPERRATION_P);
        mongoTemplate.updateFirst(query.addCriteria(criteria), update, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
        // 更新任务数量状态
        formDataService.saveTaskVariable(appId, task.getProcessInstanceId(), task.getDataId(), workflowNode.getPreNodeId());
        //记录日志
        workflowLogService.saveLog(appId, task.getProcessInstanceId(), SecurityUtils.getCurrentUserId(), Constants.WORKFLOW_OPERRATION_REVERT, null, task.getCurrentNodeId(), task.getDataId());
    }

    public List<List> getAllunPrefect(Pageable pageable) {
        List<App> allApps = getAllApps();
        List<List> lists = new ArrayList<>();
        if(allApps!=null&&allApps.size()>0){
            for(App app:allApps){
                lists.add(getAllunPrefect(app.getId(),pageable));
            }
        }
        return lists;
    }
    //统计待完善的数目
    public List<Task> getAllunPrefect(String appId,Pageable pageable){
        //单据dataId中，状态为撤回且上一个节点还没有完成的
        Query query = new Query(Criteria.where("operateResult").is(Constants.WORKFLOW_OPERRATION_REVERT).andOperator(
            Criteria.where("appId").is(appId)
        ));
        query.with(pageable);
        List<Task> taskList = mongoTemplate.find(query,Task.class,Constants.COLLECTION_WORKFLOW_TASK);
        //查找出上一个节点且未完成的
        //先查找当前节点ID获取工作流节点
        List<String> ids = new ArrayList<>();
        for(Task task:taskList){
            ids.add(task.getCurrentNodeId());
        }
        List<WorkflowNode> nodeList = workflowService.findByList(ids);
        List<String> idsPrv = new ArrayList<>();
        //得到上一个工作流节点
        for(WorkflowNode node:nodeList){
            idsPrv.add(node.getPreNodeId());
        }

        Query queryPrvTask = new Query();
        Criteria criteriaPrvTask = new Criteria();
        criteriaPrvTask.andOperator(Criteria.where("appId").is(appId),
            Criteria.where("operateResult").is(Constants.WORKFLOW_OPERRATION_P),
            Criteria.where("currentNodeId").in(idsPrv));
        List<Task> unprefectTask = mongoTemplate.find(queryPrvTask.addCriteria(criteriaPrvTask),Task.class,Constants.COLLECTION_WORKFLOW_TASK);
        return unprefectTask;
    }
    private Date transferTime(Date date){
        SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     // 北京
        bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置北京时区
        try {
            return bjSdf.parse(bjSdf.format(date));
        } catch (ParseException e) {
            //e.printStackTrace();
        }
        return date;
    }
    //转交任务——将自身节点删除，为列表添加
    public void transferTask(String appId, Task task, List<AuditUserInfo> userIds) throws Exception{
        //判断节点类型
        WorkflowNode workflowNode = workflowService.findById(task.getCurrentNodeId());
        if(!workflowNode.isTransfer()){
            return;
        }
        if (Constants.WORKFLOW_TYPE_APPROVAL != workflowNode.getType())
            return;
        //为人员列表创建任务，删除自身任务
        task.setOperateResult(Constants.WORKFLOW_OPERRATION_TRANSFER);
        mongoTemplate.save(task);
        for (AuditUserInfo userInfo : userIds) {
            workflowNode.getAuditUserInfos().add(userInfo);
        }
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(workflowNode);
        formDataService.createTask(appId, task.getProcessInstanceId(),nodes, task.getDataId(),Constants.WORKFLOW_STATUS_Y);

        //记录日志
        workflowLogService.saveLog(appId, task.getProcessInstanceId(), SecurityUtils.getCurrentUserId(), Constants.WORKFLOW_OPERRATION_TRANSFER, null, task.getCurrentNodeId(), task.getDataId());
    }

    //拒绝任务——将当前节点设置为已拒绝，更新单据状态
    public void unpassTask(String appId, Task task, List<UpdateDataDto> updateDataDtoList)throws Exception {
        //修改配置
        updateData(appId,task.getDataId(),updateDataDtoList);
        WorkflowNode workflowNode = workflowService.findById(task.getCurrentNodeId());
        task.setOperateResult(Constants.WORKFLOW_OPERRATION_NO);
        mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
        workflowLogService.saveLog(appId, task.getProcessInstanceId(), SecurityUtils.getCurrentUserId(), Constants.WORKFLOW_OPERRATION_NO, null, task.getCurrentNodeId(), task.getDataId());
        formDataService.saveTaskVariable(appId, task.getProcessInstanceId(), task.getDataId(), task.getCurrentNodeId());
    }

    //通过任务——将当前节点设置为已通过，更新单据状态
    public void passTask(String appId, Task task, List<UpdateDataDto> updateDataDtoList) throws Exception{
        //修改配置
        updateData(appId,task.getDataId(),updateDataDtoList);
        task.setOperateResult(Constants.WORKFLOW_OPERRATION_YES);
        mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
        workflowLogService.saveLog(appId, task.getProcessInstanceId(), SecurityUtils.getCurrentUserId(), Constants.WORKFLOW_OPERRATION_YES, null, task.getCurrentNodeId(), task.getDataId());
        formDataService.saveTaskVariable(appId, task.getProcessInstanceId(), task.getDataId(), task.getCurrentNodeId());
    }
    //根据List<UpdateDataDto>更新字段
    public void updateData(String appId, String dataId, List<UpdateDataDto> updateDataDtoList){
        if(updateDataDtoList==null||updateDataDtoList.size()==0)
            return;
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("appId").is(appId),
            Criteria.where("dataId").is(dataId));
        //含有各种表单，以及申请人、申请时间、更新时间
        List<DBObject> objects = mongoTemplate.find(query.addCriteria(criteria), DBObject.class, Constants.COLLECTION_APP_DATA);
        for (DBObject object : objects) {
            for(UpdateDataDto updateDataDto:updateDataDtoList){
                if(updateDataDto.getQueId().equals(object.get("queId").toString())){
                    String oldValue = object.get("value").toString();
                    if(!oldValue.equals(updateDataDto.getValue())){
                        object.put("value",updateDataDto.getValue());
                        mongoTemplate.save(object);
                    }
                }
            }
        }
    }
//    public boolean checkTaskPremmsion(Task taks,String accId,String userId){
//        if(taks.getOpeartorAccId()==null&&taks.getOperatorUserId()==null){
//            return false;
//        }
//        if((accId!=null&&accId.equals(taks.getOpeartorAccId()))
//            ||(userId!=null&&userId.equals(taks.getOperatorUserId()))){
//            return true;
//        }
//        return false;
//    }
    public boolean checkRevertPermission(Task task){
        //验证任务状态
        if(!Constants.WORKFLOW_OPERRATION_P.equals(task.getOperateResult()))
            return false;
        String currentNodeId = task.getCurrentNodeId();
        WorkflowNode workflowNode = workflowService.findById(currentNodeId);
        WorkflowNode prvNode = workflowService.findById(workflowNode.getPreNodeId());
        //如果不是审批节点连在一起不能撤回
        if(Constants.WORKFLOW_TYPE_APPROVAL != workflowNode.getType()||
            Constants.WORKFLOW_TYPE_APPROVAL != prvNode.getType()){
            return false;
        }
        //如果存在会签标识，那么也不能撤回。必须是单个审批节点的相连
        if(workflowNode.getCountersign()!=Constants.COUNTERSIGN_ONE
            ||prvNode.getCountersign()!=Constants.COUNTERSIGN_ONE){
            return false;
        }
        //其他情况，删除任务，设置上一个任务状态
        return true;
    }
}
