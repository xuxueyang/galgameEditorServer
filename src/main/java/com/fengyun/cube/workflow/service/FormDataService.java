package com.fengyun.cube.workflow.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import com.fengyun.cube.rpc.uaa.dto.UserCacheInfo;
import com.fengyun.cube.workflow.config.ApplicationProperties;
import com.fengyun.cube.workflow.domain.*;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.UUIDGenerator;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.redis.dto.RedisObjectDTO;
import com.fengyun.cube.redis.service.CubeRedisClient;
import com.fengyun.cube.rpc.uaa.client.CubeuaaClient;
import com.fengyun.cube.rpc.uaa.dto.CubeuaaRPCDto;
import com.fengyun.cube.rpc.uaa.dto.UserInfo;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.service.dto.request.ConditionDto;
import com.fengyun.cube.workflow.service.dto.request.DuplicateFiledDto;
import com.fengyun.cube.workflow.service.dto.request.OverlapFiledDto;
import com.fengyun.cube.workflow.service.dto.request.RelateDataDto;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Service
@Transactional
public class FormDataService {

	@Autowired
    private MongoTemplate mongoTemplate;

	@Autowired
	private CubeRedisClient cubeRedisClient;

	@Autowired
	private WorkflowLogService workflowLogService;
	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private AppService appService;
	@Autowired
	private CubeuaaClient cubeuaaClient;
    @Autowired
    private PushService pushService;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private RelateOtherService relateOtherService;
	/**
	 *
	 * @param appId 应用id
	 * @param pwd 密码
	 * @param processInstanceId 流程实例id
	 * @return
	 */
	public boolean checkApplyPwd(String appId, String pwd, String processInstanceId)  throws Exception{
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
				Criteria.where("processInstanceId").is(processInstanceId));
		query.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		if(applyNode!= null && applyNode.getApplyConfig() != null){
			if(pwd.equals(applyNode.getApplyConfig().getApplyPass())){
				return true;
			}
		}
		return false;
	}
	/**
	 * 验证申请人是否有权限申请单据
	 * @param appId
	 * @param processInstanceId
	 * @return
	 */
	public boolean checkApplyPermission(String appId, String processInstanceId)  throws Exception{
		String acctId = SecurityUtils.getCurrentAcctId();
		String userId = SecurityUtils.getCurrentUserId();
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
				Criteria.where("processInstanceId").is(processInstanceId));
		query.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		if(applyNode != null && applyNode.getApplyConfig() != null ){
			if(Constants.APPLY_TYPE_2 ==  applyNode.getApplyConfig().getApplyType()){
				if(applyNode.getAuditUserInfos()!= null && applyNode.getAuditUserInfos().size()>0){
					for (AuditUserInfo auditUserInfo : applyNode.getAuditUserInfos()) {
						if(acctId.equals(auditUserInfo.getAcctId()) || userId.equals(auditUserInfo.getUserId()) ){
							return true;
						}
					}
					return false;
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public WorkflowNode checkNode(String appId, String processInstanceId, String workflowNodeId)  throws Exception{
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("auditNodeId").is(workflowNodeId),
				Criteria.where("processInstanceId").is(processInstanceId));
		query.addCriteria(criteria);
		WorkflowNode node = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		return node;
	}


	public Map<String,Object> getFormDataByNode(App app, WorkflowNode node)  throws Exception{
		Map<String,Object> result = new HashMap<>();
		List<AuditEdit> editQues = node.getAuditEdits(); //可编辑字段
		List<AuditEdit> hideQues = node.getAuditHides(); //隐藏字段
		Query queryData = new Query(Criteria.where("appId").is(app.getId()));
		List<DBObject> objects = mongoTemplate.find(queryData, DBObject.class, Constants.COLLECTION_PUBLISH_FORM_DATA);
		List<DBObject> list = new ArrayList<>();
		for (DBObject object : objects) {
			//去掉隐藏字段
			List<DBObject> children = (List<DBObject>) object.get("children");//存在子节点信息
			if(hideQues!= null && hideQues.size()>0){
				for (AuditEdit hideQue : hideQues) {
					if(hideQue.getQueId().equals(object.get("queId").toString())){
						object.put("canHide", true);
						break;
					}
					if(children != null && children.size()>0){
						for (DBObject dbObject : children) {
							if (hideQue.getQueId().equals(dbObject.get("queId").toString())) {
								dbObject.put("canHide", true);
								break;
							}
						}
					}
				}
				object.put("children", children);
			}
		}
		//list中添加可编辑字段
		for (DBObject dbObject : objects) {
			List<DBObject> children = (List<DBObject>) dbObject.get("children");//存在子节点信息
			if(editQues!= null && editQues.size()>0){
				for (AuditEdit editQue : editQues) {
					if(editQue.getQueId().equals(dbObject.get("queId").toString())){
						dbObject.put("canEdit", true);
						break;
					}
					if(children != null && children.size()>0){
						for (DBObject child : children) {
							if (editQue.getQueId().equals(child.get("queId").toString())) {
								child.put("canEdit", true);
								break;
							}
						}
					}

				}
				dbObject.put("children", children);
			}
		}
		result.put("formData", objects);
		result.put("app", appService.prepareAppDto(app));

		return result;
	}


	public Map<String,Object> getFormDataForApplyNode(App app)  throws Exception{
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(app.getId()),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
				Criteria.where("processInstanceId").is(app.getProcessInstanceId()));
		query.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		if(applyNode!= null){
			return getFormDataByNode(app, applyNode);
		}
		return null;
	}
	public List<String> getAllDataId(String appId,String processInstanceId,Date dateMin,Date dateMax ){
	    Query query = new Query();
	    Criteria criteria = new Criteria();
	    criteria.andOperator(Criteria.where("appId").is(appId),
            Criteria.where("processInstanceId").is(processInstanceId));
	    List<String> dataIds = new ArrayList<>();
        query.addCriteria(criteria);
        List<DBObject> dbObjectList = mongoTemplate.find(query,DBObject.class,Constants.COLLECTION_APP_DATA);
        for(DBObject dbObject:dbObjectList){
	        if(!dataIds.contains(dbObject.get("dataId"))){
                Date date = (Date)dbObject.get("date");
                if(dateMin!=null&&date.before(dateMin)){
	                continue;
                }
                if(dateMax!=null&&date.after(dateMax)){
                    continue;
                }
                dataIds.add(dbObject.get("dataId").toString());
            }
        }
	    return  dataIds;
    }
    //提交草稿——删除原来的，新建单据
    public void submitData(App app, JSONArray data, WorkflowNode applyNode, String dataId) throws Exception{
        //删除草稿——;
		Query query = new Query(Criteria.where("dataId").is(dataId)
				.and("appId").is(app.getId())
				.and("status").is(Constants.WORKFLOW_STATUS_D));
        mongoTemplate.remove(query,DBObject.class,Constants.COLLECTION_APP_DATA);
        //创建新数据
        saveData(app,data,applyNode,Constants.WORKFLOW_STATUS_P);
    }
	public void saveData(App app, JSONArray data, WorkflowNode applyNode, String status) throws Exception{
		String processInstanceId = app.getProcessInstanceId();
		DBCollection collection = null;
		if(!mongoTemplate.collectionExists(Constants.COLLECTION_APP_DATA)){
			collection = mongoTemplate.createCollection(Constants.COLLECTION_APP_DATA);
		}else{
			collection = mongoTemplate.getCollection(Constants.COLLECTION_APP_DATA);
		}

		String dataId =UUIDGenerator.getUUID();//单据id
		RedisObjectDTO redisObjectDTO = new RedisObjectDTO(SecurityUtils.getCurrentTenantCode(), null, null, "workflow", app.getId());
		redisObjectDTO.setValue(1);
		Long formNo = cubeRedisClient.increment(redisObjectDTO);//单据编号
		for (int i = 0; i < data.size(); i++) {
			JSONArray col = data.getJSONArray(i);
			for (int j = 0; j < col.size(); j++) {
				JSONObject dbObject = col.getJSONObject(j);
				dbObject.put("appId", app.getId());
                dbObject.put("processInstanceId", processInstanceId);
				dbObject.put("status", status);
				dbObject.put("dataId", dataId);
				dbObject.remove("canEdit");
				dbObject.remove("canHide");
				//保留null字段,dbObject.toJSONString()会忽视null字段
                String str = JSONObject.toJSONString(dbObject,SerializerFeature.WriteMapNullValue);
				collection.insert((DBObject)com.mongodb.util.JSON.parse(str));
			}
		}
		//保存编号、申请人、创建时间、更新时间
		initAddData(app.getId(),processInstanceId,status,dataId,formNo,SecurityUtils.getCurrentUserId());

		if(Constants.WORKFLOW_STATUS_P.equals(status)){//如果是提交表单，需要创建任务
            //记录日志
            workflowLogService.saveLog(app.getId(),processInstanceId,SecurityUtils.getCurrentUserId(),Constants.WORKFLOW_OPERRATION_APPLY, null, applyNode.getAuditNodeId(),dataId);
            //分配任务到下一个节点
			// 查询下一个节点
			List<WorkflowNode> nextNodes = new ArrayList<>();
			workflowService.searchNextNode(applyNode, app.getId(), Constants.COLLECTION_PUBLISH_WORKFLOW, nextNodes,processInstanceId,dataId);
			//创建任务
			createTask(app.getId(),processInstanceId,nextNodes,dataId,Constants.WORKFLOW_OPERRATION_YES);
		}
	}

	private void initAddData(String appId, String processInstanceId, String status, String dataId, Long formNo,String createdId) {
		List<DBObject> list = new ArrayList<>();
		DBObject updateObject = new BasicDBObject();
		updateObject.put("appId", appId);
		updateObject.put("dataId", dataId);
		updateObject.put("status", status);
		updateObject.put("processInstanceId", processInstanceId);
		updateObject.put("title", "更新时间");
		updateObject.put("queId", Constants.QUE_ID_UPDATED_DATE);
		updateObject.put("value", ""+ZonedDateTime.now());
		list.add(updateObject);

		DBObject createObject = new BasicDBObject();
		createObject.put("appId", appId);
		createObject.put("dataId", dataId);
		createObject.put("status", status);
		createObject.put("processInstanceId", processInstanceId);
		createObject.put("title", "申请时间");
		createObject.put("queId", Constants.QUE_ID_CREATED_DATE);
		createObject.put("value", ""+ZonedDateTime.now());
		list.add(createObject);

		DBObject applyObject = new BasicDBObject();
		applyObject.put("appId", appId);
		applyObject.put("dataId", dataId);
		applyObject.put("status", status);
		applyObject.put("processInstanceId", processInstanceId);
		applyObject.put("title", "申请人");
		applyObject.put("queId", Constants.QUE_ID_CREATED_ID);
		applyObject.put("value", createdId);
		//调用获取到申请人的用户信息
		CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto(createdId, SecurityUtils.getCurrentTenantCode(), SecurityUtils.getCurrentSpaceCode());
		UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
		applyObject.put("tel",userInfo.getAcctInfo().getTel());
		applyObject.put("email",userInfo.getAcctInfo().getEmail());
		list.add(applyObject);

		DBObject noObject = new BasicDBObject();
		noObject.put("appId", appId);
		noObject.put("dataId", dataId);
		noObject.put("status", status);
		noObject.put("processInstanceId", processInstanceId);
		noObject.put("title", "编号");
		noObject.put("queId", Constants.QUE_ID_NO);
		noObject.put("value", formNo);
		list.add(noObject);
		mongoTemplate.insert(list, Constants.COLLECTION_APP_DATA);
	}
	/**
	 *
	 * @param appId
	 * @param processInstanceId
	 * @param nextNodes
	 * @param dataId
	 * @param lastOperationResult 上一个节点审批结果
	 */
	public void createTask(String appId, String processInstanceId, List<WorkflowNode> nextNodes, String dataId,String lastOperationResult)throws Exception{
		if(nextNodes == null || nextNodes.size()==0){
			LogUtil.debug(null, null, null, null, "沒有找到下级节点，审批结束"+appId);
			//流程结束，修改单据状态
			Query query = new Query(Criteria.where("dataId").is(dataId)
					.and("processInstanceId").is(processInstanceId)
					.and("appId").is(appId));
			Update update = new Update();
			update.set("status", Constants.WORKFLOW_STATUS_Y);
			mongoTemplate.updateMulti(query, update, DBObject.class, Constants.COLLECTION_APP_DATA);
        	//修改单据更新时间

			Query updateDateQuery = new Query(Criteria.where("dataId").is(dataId)
					.and("processInstanceId").is(processInstanceId)
					.and("appId").is(appId)
					.and("queId").is(Constants.QUE_ID_UPDATED_DATE));
			Update updateDate = new Update();
			updateDate.set("value",""+ZonedDateTime.now());
			mongoTemplate.updateFirst(updateDateQuery, updateDate, DBObject.class, Constants.COLLECTION_APP_DATA);
			//发送处理结果
            // 获取到数据抄送到邮件
			Query createQuery = new Query(Criteria.where("dataId").is(dataId)
					.and("processInstanceId").is(processInstanceId)
					.and("appId").is(appId)
					.and("queId").is(Constants.QUE_ID_CREATED_ID));
            DBObject dbObject = mongoTemplate.findOne(createQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
            String createId = dbObject.get("value").toString();
            pushService.createPushInfo(appId,dataId,processInstanceId,Constants.PUSH_TYPE_MAIL,
                getEmail(createId,null),
                Constants.PUSH_DATA_TYPE_RESULT,"");
		}else{
			//获取当前应用单据信息
        	Query query = new Query(Criteria.where("dataId").is(dataId)
        			.and("processInstanceId").is(processInstanceId)
        			.and("appId").is(appId));
        	List<DBObject> currentDataList = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_APP_DATA);

        	if(currentDataList != null && currentDataList.size()>0){
        		//创建下个节点任务，如果是QRobot节点，需要自动添加、更新数据
    			for (WorkflowNode workflowNode : nextNodes) {
//    			    WorkflowNode nextNode = workflowNode;
    			    //递归，添加数据。对于自动添加、更新、抄送的节点，自动处理
    				if(Constants.WORKFLOW_TYPE_QROBOT_ADD == workflowNode.getType()){
    					//到达QRobot 添加节点时，向目标应用添加一条数据，直接审批通过
    					QRobotConfig qRobotConfig = workflowNode.getqRobotConfig();

    		        	//创建目标应用数据
    		        	String relatedDataId = UUIDGenerator.getUUID();
    		        	RedisObjectDTO redisObjectDTO = new RedisObjectDTO(SecurityUtils.getCurrentTenantCode(), null, null, "workflow", qRobotConfig.getRelatedAppId());
    		        	redisObjectDTO.setValue(1);
    		        	Long dataNo= cubeRedisClient.increment(redisObjectDTO);

    		        	String createdId = SecurityUtils.getCurrentUserId();
    		        	List<AuditUserInfo> users = workflowNode.getAuditUserInfos();
    		        	if(users!= null && users.size()>0){ //QRobot节点只能有一个审批人
    		        		AuditUserInfo user = users.get(0);
    		        		if(user!= null){
    		        			if(Validators.fieldNotBlank(user.getUserId())){
    		        				createdId= user.getUserId();
    		        			}else if(Validators.fieldNotBlank(user.getAcctId())){
    		        				CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto(user.getAcctId(),SecurityUtils.getCurrentTenantCode(),SecurityUtils.getCurrentSpaceCode());
    		        				UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
    		        				if(userInfo!= null){
    		        					createdId = userInfo.getUserId();
    		        				}
    		        			}
    		        		}
    		        	}
    		        	initAddData(qRobotConfig.getRelatedAppId(), Constants.WORKFLOW_QROBOT_ADD_INSTANCEID, Constants.WORKFLOW_STATUS_Y, relatedDataId, dataNo,createdId);
    		        	saveRelatedData(currentDataList,qRobotConfig.getRelatedAppId(),qRobotConfig.getQueRelation(), relatedDataId);
    		        	for (QueRelation queRelation : qRobotConfig.getQueRelation()) {
    		        		Object currentValue = getCurrentAppQueValue(currentDataList, queRelation.getQueId());
    					}

    				}else if(Constants.WORKFLOW_TYPE_QROBOT_UPDATE == workflowNode.getType()){
    					//到达QRobot 节点时，向满足条件的目标应用数据更新数据，直接审批通过
    					QRobotConfig qRobotConfig = workflowNode.getqRobotConfig();
    					if(qRobotConfig != null ){
    						List<QueRelation> filterRelation = qRobotConfig.getFilterCondition();
    						if(filterRelation != null && filterRelation.size()>0){
    							//更改符合筛选条件的的数据
    							updateConditionRelatedData(qRobotConfig.getRelatedAppId(),filterRelation,currentDataList,qRobotConfig.getQueRelation());
    						}else{
    							//没有筛选条件，将所有表单数据更改
    							for (QueRelation queRelation : qRobotConfig.getQueRelation()) {
    	    		        		Object currentValue = getCurrentAppQueValue(currentDataList, queRelation.getQueId());
    	    		        		updateRelatedData(currentValue,qRobotConfig.getRelatedAppId(),queRelation.getRelatedQueId());
    	    					}
    						}
    					}
    				}else if(Constants.WORKFLOW_TYPE_MULTI_AUDIT==workflowNode.getType()){

    					//为审批人的上个领导创建任务
						List<MultiLeader> supiorByMultiAuditNode = workflowService.getSupiorByMultiAuditNode(workflowNode, dataId);
						if(supiorByMultiAuditNode!=null&&supiorByMultiAuditNode.size()>0){
							//对于逐级审批节点，只会走这里一次，所以task的操作者直接设为第一个领导ID即可
							String tmpId = supiorByMultiAuditNode.get(0).getSuperiorId();
							if(workflowNode.isCanAgentAudit()
									&&supiorByMultiAuditNode.get(0).getAuditingAgent()!=null
									&&!"".equals(supiorByMultiAuditNode.get(0).getAuditingAgent().getAgentUserId())){
								tmpId = supiorByMultiAuditNode.get(0).getAuditingAgent().getAgentUserId();
							}
							Task task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),tmpId,"",appId);
							task.setOperateResult(Constants.WORKFLOW_OPERRATION_P);
							if(!tmpId.equals(supiorByMultiAuditNode.get(0).getSuperiorId())&&!"".equals(supiorByMultiAuditNode.get(0).getSuperiorId())){
								//抄送给领导
								Task taskToLeader = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),supiorByMultiAuditNode.get(0).getSuperiorId(),"",appId);
								taskToLeader.setOperateResult(Constants.WORKFLOW_OPERRATION_CC);
								mongoTemplate.save(taskToLeader, Constants.COLLECTION_WORKFLOW_TASK);
								// 获取到数据抄送到邮件
								pushService.createPushInfo(appId,taskToLeader.getDataId(),taskToLeader.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
											getEmail(taskToLeader.getOperatorUserId(),taskToLeader.getOpeartorAccId()),
											Constants.PUSH_DATA_TYPE_CARBON,"");
								// 记录日志
//								workflowLogService.saveLog(appId,processInstanceId,SecurityUtils.getCurrentUserId(),Constants.WORKFLOW_OPERRATION_CC, null, workflowNode.getAuditNodeId(),dataId);
							}
							// 获取到数据抄送到邮件
							pushService.createPushInfo(appId,task.getDataId(),task.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
									getEmail(task.getOperatorUserId(),task.getOpeartorAccId()),
									Constants.PUSH_DATA_TYPE_SOLVE,"");
							mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
						}

					} else{
                        //创建任务
                        List<AuditUserInfo> users = workflowNode.getAuditUserInfos();
                        List<Task> tasks = new ArrayList<>();
                        String specialId = Constants.WORKFLOW_PERMISSION_USER_ID;
                        for (AuditUserInfo auditUserInfo : users) {
                            Task task = null;
                            if(Validators.fieldNotBlank(auditUserInfo.getUserId())){
                                task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(), auditUserInfo.getUserId(), null,appId);
                                tasks.add(task);
                            }else if(Validators.fieldNotBlank(auditUserInfo.getAcctId())){
                                task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),null, auditUserInfo.getAcctId(),appId);
                                tasks.add(task);
                            }else
                            //申请人部门负责人
                            if(auditUserInfo.isCreateIdForLeader()){
                            	Set<String> leaderIds =getCreateUserLeader(currentDataList);
                            	if(leaderIds != null && leaderIds.size()>0){
                            		for (String leaderId : leaderIds) {
                            			task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),leaderId, null,appId);
                            			tasks.add(task);
                            		}
                            	}
                            }else
							//是否申请人
							if(auditUserInfo.isNeedApplyUser()){
								Object createId = getCreateId(currentDataList);
								if(Validators.fieldNotBlank(createId)){
									task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),createId.toString(), null,appId);
									tasks.add(task);
								}
							}else
							//TODO 上级领导——
							if(auditUserInfo.isGroupSuperior()){
								//从表里特殊处理到
								// 如果代理审批人的话，就需要处理创建者ID获取到代理审批人的上级领导等————逐级审批应该不需要做
								Object createId = getCreateId(currentDataList);
								if(Validators.fieldNotBlank(createId)){
									String superiorId = workflowService.getSuperiorId(createId.toString());
									if(Validators.fieldNotBlank(superiorId)){
										task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),superiorId, null,appId);
										tasks.add(task);
									}
								}
							}else
							//TODO  审批代理人：由该关联人的部门审批代理人处理；——先得到代理人，然后代理人的部门，然后部门的审判代理人（不然就部门领导）
							if(auditUserInfo.isGroupSuperior()){
								//从表里特殊处理到
								// 如果是上级那么就需要创建者ID获取到部门，再获取到上级领导————逐级审批应该不需要做
								Object createId = getCreateId(currentDataList);
								if(Validators.fieldNotBlank(createId)){
									String auditAgentId = workflowService.getAuditAgentId(createId.toString());
									if(Validators.fieldNotBlank(auditAgentId)){
										task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),auditAgentId, null,appId);
										tasks.add(task);
									}
								}
							}
                        }

						//如果是抄送节点、QRobot，那么任务自动设置为完成，并创建下一个
                        if(Constants.WORKFLOW_TYPE_CARBON_COPY == workflowNode.getType()){
                            for(Task task:tasks){
                                task.setOperateResult(Constants.WORKFLOW_OPERRATION_CC);
                                mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
                                // 获取到数据抄送到邮件
                                pushService.createPushInfo(appId,task.getDataId(),task.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
                                    getEmail(task.getOperatorUserId(),task.getOpeartorAccId()),
                                    Constants.PUSH_DATA_TYPE_CARBON,"");
                                // 记录日志
                                workflowLogService.saveLog(appId,processInstanceId,SecurityUtils.getCurrentUserId(),Constants.WORKFLOW_OPERRATION_CC, null, workflowNode.getAuditNodeId(),dataId);
                            }
                        }else{
                            for(Task task:tasks){
								//特殊审批——如只有一个人，那么没办法，如果多个人且含有特殊的那个人ID（戴总）那么如果不是最后的节点自动忽视
								boolean isSpecial = false;
								if(tasks.size()>1&&task.getOperatorUserId().equals(specialId)){
									//如果只有戴总一个还是得让它审批，只有多个才能忽略
									WorkflowNode lastNodeByUserId = workflowService.getLastNodeByUserId(appId, specialId, processInstanceId, dataId);
									if(lastNodeByUserId!=null&&workflowNode.getAuditNodeId()!=lastNodeByUserId.getAuditNodeId()){
										//说明不是最后一个节点，忽视
										isSpecial = true;
									}
								}
								if(!isSpecial){
									task.setOperateResult(Constants.WORKFLOW_OPERRATION_P);
									// 获取到数据抄送到邮件
									pushService.createPushInfo(appId,task.getDataId(),task.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
											getEmail(task.getOperatorUserId(),task.getOpeartorAccId()),
											Constants.PUSH_DATA_TYPE_SOLVE,"");
									mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
								}
                            }
                        }
                    }
                    //统计节点任务数量
    				saveTaskVariable(appId,processInstanceId,dataId,workflowNode.getAuditNodeId());
    			}
        	}
		}
	}
    private String  getEmail(String usrId,String accId){
        List<String> usrIds = new ArrayList<>();
        if(Validators.fieldNotBlank(usrId)){
            usrIds.add(usrId);
        }else if (Validators.fieldNotBlank(accId)) {
            CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto(accId, SecurityUtils.getCurrentTenantCode(), SecurityUtils.getCurrentSpaceCode());
            UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
            usrId = userInfo.getUserId();
            usrIds.add(usrId);
        }
        //得到email
        if(usrIds.size()>0){
            List<UserCacheInfo> usersByDb = cubeuaaClient.findUsersByDb(SecurityUtils.getCurrentTenantCode(), SecurityUtils.getCurrentSpaceCode(), usrIds);
            for(UserCacheInfo userCacheInfo:usersByDb){
                if(Validators.fieldNotBlank(userCacheInfo.getAcctEmail())){
                    return  userCacheInfo.getAcctEmail();
                }
            }
        }
        return "";
    }
    private Set<String> getCreateUserLeader(List<DBObject> currentDataList) {
    	for (DBObject dbObject : currentDataList) {
    		if(Validators.fieldNotBlank(dbObject.get("queId"))){
    			if(Constants.QUE_ID_CREATED_ID.equals(dbObject.get("queId").toString())){
    				Object creatId = dbObject.get("value");
    				if(Validators.fieldNotBlank(creatId)){
    					Set<String> leaderIds = cubeuaaClient.findLeaderByUser(creatId.toString());
    					return leaderIds;
    				}
    			}
    		}
		}
		return null;
	}
	private Object getCreateId(List<DBObject> currentDataList) {
    	for (DBObject dbObject : currentDataList) {
    		if(Validators.fieldNotBlank(dbObject.get("queId"))){
    			if(Constants.QUE_ID_CREATED_ID.equals(dbObject.get("queId").toString())){
    				return dbObject.get("value");
    			}
    		}
		}
		return null;
	}
	public void saveTaskVariable(String appId,String processInstanceId, String dataId, String auditNodeId)throws Exception {
        Query query = new Query(Criteria.where("dataId").is(dataId)
            .and("processInstanceId").is(processInstanceId)
            .and("workflowNodeId").is(auditNodeId));
        Variable variable = mongoTemplate.findOne(query, Variable.class, Constants.COLLECTION_WORKFLOW_VARIABLE);
        if(variable==null){
            variable = new Variable();
            variable.setDataId(dataId);
            variable.setProcessInstanceId(processInstanceId);
            variable.setWorkflowNodeId(auditNodeId);
            variable.setCompleteNumN(-1);
            variable.setCompleteNumY(-1);
            variable.setCompleteNum(-1);
        }
        query = new Query(Criteria.where("dataId").is(dataId)
            .and("processInstanceId").is(processInstanceId)
            .and("operateResult").is(Constants.WORKFLOW_OPERRATION_YES)
            .and("currentNodeId").is(auditNodeId));
        long completeNum_Y = mongoTemplate.count(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
        query = new Query(Criteria.where("dataId").is(dataId)
            .and("processInstanceId").is(processInstanceId)
            .and("operateResult").is(Constants.WORKFLOW_OPERRATION_NO)
            .and("currentNodeId").is(auditNodeId));
        long completeNum_N = mongoTemplate.count(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
        query = new Query(Criteria.where("dataId").is(dataId)
            .and("processInstanceId").is(processInstanceId)
            .and("currentNodeId").is(auditNodeId)
            .and("operateResult").in(Constants.WORKFLOW_OPERRATION_P));
        long p = mongoTemplate.count(query, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
        long count = completeNum_N+completeNum_Y+p;
        //判断策略
        int pass = -1;
        if(count!=variable.getTotalNum()||
            completeNum_N!=variable.getCompleteNumN()||
            completeNum_Y!=variable.getCompleteNumY()){
            WorkflowNode workflowNode = workflowService.findById(appId,auditNodeId,processInstanceId);;
            //如果是其他节点等待任务。如果是审批节点需要判断
            if (Constants.WORKFLOW_TYPE_APPROVAL == workflowNode.getType()) {
                if (Constants.COUNTERSIGN_OTHER == workflowNode.getCountersign()&&count == completeNum_N + completeNum_Y) {
                    if (completeNum_N > completeNum_Y) {
                        pass = 0;
                    } else {
                        pass = 1;
                    }
                } else if (Constants.COUNTERSIGN_ALL == workflowNode.getType()&&count == completeNum_N + completeNum_Y) {
                    //需要所有人同意
                    if (completeNum_N > 0) {
                        pass = 0;
                    } else {
                        pass = 1;
                    }
                } else if (Constants.COUNTERSIGN_OTHER != workflowNode.getType()&&
                            Constants.COUNTERSIGN_ALL != workflowNode.getType()
                    ) {
                    //默认是单人审核COUNTERSIGN_ONE
                    //且这样情况下需要直接删除其他任务
                    if (completeNum_N > 0 && completeNum_Y <= 0) {
                        //拒绝
                        pass = 0;
                    } else if (completeNum_N <= 0 && completeNum_Y > 0) {
                        //同意
                        pass = 1;
                    }
                    if(completeNum_N+completeNum_Y>0){
                        //删除其他状态为P的任务
                        Query queryTask = new Query();
                        Criteria criteria = new Criteria();
                        criteria.andOperator(Criteria.where("appId").is(appId),
                            Criteria.where("processInstanceId").is(processInstanceId),
                            Criteria.where("dataId").is(dataId),
                            Criteria.where("operateResult").is(Constants.WORKFLOW_OPERRATION_P),
                            Criteria.where("currentNodeId").is(auditNodeId)
                            );
                        queryTask.addCriteria(criteria);
                        mongoTemplate.remove(queryTask,Task.class,Constants.COLLECTION_WORKFLOW_TASK);
                        //因为删除了。p为0.修改p
                        p=0;
                        count = completeNum_N+completeNum_Y;
                    }
                }
            } else if(Constants.WORKFLOW_TYPE_QROBOT_UPDATE==workflowNode.getType()
                ||Constants.WORKFLOW_TYPE_QROBOT_ADD==workflowNode.getType()
                ||Constants.WORKFLOW_TYPE_CARBON_COPY==workflowNode.getType()){
                //特殊的三种节点直接算通过
                pass=1;
            }else if(Constants.WORKFLOW_TYPE_MULTI_AUDIT==workflowNode.getType()){
				//需要判断：现在处于第几层领导、审批结果如何、如果上一层已通过为下一层创建新的任务，如果都已通过那么算已通过
				//如果上一层已拒绝，那么视为已拒绝
				Query queryTask = new Query();
				queryTask.addCriteria(Criteria.where("appId").is(appId)
						.and("processInstanceId").is(processInstanceId)
						.and("dataId").is(dataId)
						.and("currentNodeId").is(auditNodeId));
				List<Task> tasks = mongoTemplate.find(queryTask, Task.class, Constants.COLLECTION_WORKFLOW_TASK);
				//还要得到逐级审批节点的领导们
				List<MultiLeader> supiorByMultiAuditNode = workflowService.getSupiorByMultiAuditNode(workflowNode, dataId);
				if(tasks!=null&&tasks.size()>0&&supiorByMultiAuditNode!=null&&supiorByMultiAuditNode.size()>0){
					int i = 0;
					for(;i<supiorByMultiAuditNode.size();i++){
						//从下往上依次判断审批状态
						Task myTask = null;
						for(Task task:tasks){
							//得到操作人是自己的
							//这里需要根据配置
							if(workflowNode.isCanAgentAudit()){
								if(task.getOperatorUserId().equals(supiorByMultiAuditNode.get(i).getAuditingAgent().getAgentUserId())){
									myTask = task;
									break;
								}
							}else{
								if(task.getOperatorUserId().equals(supiorByMultiAuditNode.get(i).getSuperiorId())){
									myTask = task;
									break;
								}
							}
						}
						//TODO
						if(myTask==null){
							//说明下一个领导没有任务，前面的领导有任务，所以需要创建
							String tmpId = supiorByMultiAuditNode.get(i).getSuperiorId();
							if(workflowNode.isCanAgentAudit()
									&&supiorByMultiAuditNode.get(i).getAuditingAgent()!=null
									&&!"".equals(supiorByMultiAuditNode.get(i).getAuditingAgent().getAgentUserId())){
								tmpId=supiorByMultiAuditNode.get(i).getAuditingAgent().getAgentUserId();
							}
							Task task = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),tmpId,"",appId);
							task.setOperateResult(Constants.WORKFLOW_OPERRATION_P);
							//抄送给领导
							if(!tmpId.equals(supiorByMultiAuditNode.get(0).getSuperiorId())&&!"".equals(supiorByMultiAuditNode.get(0).getSuperiorId())){
								//抄送给领导
								Task taskToLeader = createTaskForUser(processInstanceId,dataId,workflowNode.getAuditNodeId(),supiorByMultiAuditNode.get(0).getSuperiorId(),"",appId);
								taskToLeader.setOperateResult(Constants.WORKFLOW_OPERRATION_CC);
								mongoTemplate.save(taskToLeader, Constants.COLLECTION_WORKFLOW_TASK);
								// 获取到数据抄送到邮件
								pushService.createPushInfo(appId,taskToLeader.getDataId(),taskToLeader.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
										getEmail(taskToLeader.getOperatorUserId(),taskToLeader.getOpeartorAccId()),
										Constants.PUSH_DATA_TYPE_CARBON,"");
								// 记录日志
//								workflowLogService.saveLog(appId,processInstanceId,SecurityUtils.getCurrentUserId(),Constants.WORKFLOW_OPERRATION_CC, null, workflowNode.getAuditNodeId(),dataId);
							}

							// 获取到数据抄送到邮件
							pushService.createPushInfo(appId,task.getDataId(),task.getProcessInstanceId(),Constants.PUSH_TYPE_MAIL,
									getEmail(task.getOperatorUserId(),task.getOpeartorAccId()),
									Constants.PUSH_DATA_TYPE_SOLVE,"");
							mongoTemplate.save(task, Constants.COLLECTION_WORKFLOW_TASK);
							myTask = task;
						}
						//判断任务状态
						if(Constants.WORKFLOW_OPERRATION_NO.equals(myTask.getOperateResult())){
							pass = 0;
							break;
						}else if(Constants.WORKFLOW_OPERRATION_YES.equals(myTask.getOperateResult())){
							if(i==supiorByMultiAuditNode.size()-1){
								//说明可以进去下一个阶段
								pass = 1;
								break;
							}else{
								//说明现在完成，其他还没有
							}

						}else{
							//其他情况不做改变
							//且当前没通过，不创建下个任务
							break;
						}

					}
				}else{
					//说明没有任务、没有上级领导
					pass = 1;
				}

			} else{
                //不是审批节点需要等待都完成
                if (count == completeNum_N + completeNum_Y) {
                    //如果不是审批节点，没有会签
                    if (completeNum_N > 0)
                        pass = 0;
                    else
                        pass = 1;
                }
            }
            if (pass == 1) {
                //同意，创建下一个节点
                //FormDataService的下一个任务添加应该在这里比较好，需要修改
                List<WorkflowNode> nodes = new ArrayList<>();
                workflowService.searchNextNode(workflowNode, appId, Constants.COLLECTION_PUBLISH_WORKFLOW, nodes, processInstanceId, dataId);
                createTask(appId, processInstanceId,nodes, dataId,Constants.WORKFLOW_STATUS_Y);
            } else if (pass == 0) {
                //设置为拒绝
                //流程结束，修改单据状态——在saveTaskVariable修改
                query = new Query(Criteria.where("dataId").is(dataId)
                    .and("processInstanceId").is(processInstanceId)
                    .and("appId").is(appId));
                Update update = new Update();
                update.set("status", Constants.WORKFLOW_STATUS_N);
                mongoTemplate.updateMulti(query, update, DBObject.class, Constants.COLLECTION_APP_DATA);
                //发送结果
                //发送处理结果
                // 获取到数据抄送到邮件
				Query createQuery = new Query(Criteria.where("dataId").is(dataId)
						.and("processInstanceId").is(processInstanceId)
						.and("appId").is(appId)
						.and("queId").is(Constants.QUE_ID_CREATED_ID));
                DBObject dbObject = mongoTemplate.findOne(createQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
                String createId = dbObject.get("value").toString();
                pushService.createPushInfo(appId,dataId,processInstanceId,Constants.PUSH_TYPE_MAIL,
                    getEmail(createId,null),
                    Constants.PUSH_DATA_TYPE_RESULT,"");
            } else {
                //存在带处理的任务，不做处理，保存
            }
            variable.setCompleteNumN(completeNum_N);
            variable.setCompleteNumY(completeNum_Y);
            variable.setCompleteNum(completeNum_Y + completeNum_N);
            variable.setTotalNum(count);
            mongoTemplate.save(variable, Constants.COLLECTION_WORKFLOW_VARIABLE);
            //修改单据更新时间
            Query updateDateQuery = new Query(Criteria.where("dataId").is(dataId)
                .and("processInstanceId").is(processInstanceId)
                .and("appId").is(appId)
                .and("queId").is(Constants.QUE_ID_UPDATED_DATE));
            Update updateDate = new Update();
            updateDate.set("value",""+ZonedDateTime.now());
            mongoTemplate.updateFirst(updateDateQuery, updateDate, DBObject.class, Constants.COLLECTION_APP_DATA);
        }
    }
	private Task createTaskForUser(String processInstanceId, String dataId, String auditNodeId,String userId,String acctId,String appId){
		Task task = new Task();
		task.setCreatedDate(""+ZonedDateTime.now());
		task.setCurrentNodeId(auditNodeId);
		task.setDataId(dataId);
		task.setAppId(appId);
		task.setOperateResult(Constants.WORKFLOW_OPERRATION_P);
//		task.setOperatorUserId(userId);
		task.setOpeartorAccId(acctId);
		task.setOperatorUserId(acctId);
		if(acctId==null||"".equals(acctId)){
			task.setOpeartorAccId(userId);
			task.setOperatorUserId(userId);
		}
		task.setProcessInstanceId(processInstanceId);
		return task;
	}
	/**
	 * 更改符合条件的字段
	 * @param relatedAppId
	 * @param filterRelation
	 * @param currentDataList
	 * @param queRelations
	 */
	private void updateConditionRelatedData(String relatedAppId,List<QueRelation> filterRelation, List<DBObject> currentDataList, List<QueRelation> queRelations) {
		//获取目标应用所有单据ID
		Query allQuery = new Query(Criteria.where("appId").is(relatedAppId));
		List<DBObject> allObjects = mongoTemplate.find(allQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
		Set<String> dataIds = new HashSet<>();
		for (DBObject dBObject : allObjects) {
			if(dBObject.get("dataId") != null){
				dataIds.add(dBObject.get("dataId").toString());
			}
		}
		//获取所有满足条件的单据
		List<String> filterDataIds = new ArrayList<>();
		for (String dataId : dataIds) {
			//查询单据信息
			Query dataQuery = new Query(Criteria.where("appId").is(relatedAppId).and("dataId").is(dataId));
			List<DBObject> dataObjects = mongoTemplate.find(dataQuery, DBObject.class, Constants.COLLECTION_APP_DATA);

			int i = 0;
			for (DBObject dbObject : dataObjects) {
				for (QueRelation queFilter : filterRelation) {
					if(queFilter.getRelatedQueId().equals(dbObject.get("queId").toString())){
						Object currentfilterValue = getCurrentAppQueValue(currentDataList, queFilter.getQueId());
						if(currentfilterValue == null && dbObject.get("value") == null){
							i=i+1;
						}
						if(dbObject.get("value")!= null && currentfilterValue != null){
							if(dbObject.get("value").toString().equals(currentfilterValue.toString())){
								i=i+1;//如果满足条件就将筛选条件去掉，当筛选条件为空时，表示满足所有条件
							}
						}
					}
				}
			}
			if(i==filterRelation.size()){
				filterDataIds.add(dataId);
			}
		}
		//更新符合条件的单据信息
		for (String id : filterDataIds) {
			for (QueRelation queRelation : queRelations) {
				Object currentValue = getCurrentAppQueValue(currentDataList, queRelation.getQueId());
				Query query = new Query(Criteria.where("appId").is(relatedAppId)
						.and("queId").is(queRelation.getRelatedQueId())
						.and("dataId").is(id));
				Update update = new Update();
				update.set("value", currentValue);
				mongoTemplate.updateFirst(query,update, DBObject.class,Constants.COLLECTION_APP_DATA);
			}
		}
	}
	private void updateRelatedData(Object currentValue, String relatedAppId, String relatedQueId) {
		Query query = new Query(Criteria.where("appId").is(relatedAppId).and("queId").is(relatedQueId));
		Update update = new Update();
		update.set("value", currentValue);
		mongoTemplate.updateMulti(query,update, DBObject.class,Constants.COLLECTION_APP_DATA);
	}
	/**
	 * 保存关联的应用QueId对应的值+
	 * @param relatedAppId
	 * @param relatedDataId
	 */
	private void saveRelatedData(List<DBObject> currentDataList, String relatedAppId, List<QueRelation>  queRelations, String relatedDataId) {
		//获取关联应用的表单信息
		Query formDataQuery = new Query(Criteria.where("appId").is(relatedAppId));
		List<DBObject> relateFormData = mongoTemplate.find(formDataQuery, DBObject.class, Constants.COLLECTION_PUBLISH_FORM_DATA);
		if(relateFormData!= null && relateFormData.size()>0){
			for (DBObject dbObject : relateFormData) {
				dbObject.put("dataId", relatedDataId);
				dbObject.put("status", Constants.WORKFLOW_STATUS_Y);
				dbObject.put("processInstanceId", Constants.WORKFLOW_QROBOT_ADD_INSTANCEID);
				dbObject.put("appId", relatedAppId);
				for (QueRelation queRelation : queRelations) {
					Object currentValue = getCurrentAppQueValue(currentDataList, queRelation.getQueId());
					if(Validators.fieldNotBlank(dbObject.get("queId")) && queRelation.getRelatedQueId().equals(dbObject.get("queId").toString())){
						dbObject.put("value", currentValue);
						break;
					}
				}
				mongoTemplate.save(dbObject, Constants.COLLECTION_APP_DATA);
			}
		}

	}
	/**
	 * 获取当前应用QueId对应的值
	 * @param currentDataList
	 * @param queId
	 * @return
	 */
	private Object getCurrentAppQueValue(List<DBObject> currentDataList, String queId) {
		for (DBObject dbObject : currentDataList) {
			if(queId.equals(dbObject.get("queId").toString())){
				return dbObject.get("value");
			}else{
				List<DBObject> children = (List<DBObject>) dbObject.get("children");
				if(children != null && children.size()>0){
					for (DBObject child : children) {
						if(queId.equals(child.get("queId").toString())){
							return child.get("value");
						}
					}
				}
			}
		}
		return null;
	}
	public List<Object> getFormDataByAppId(String appId, boolean isPublish) {
		DBCollection collection = null;
		if(isPublish){
			collection = mongoTemplate.getCollection(Constants.COLLECTION_PUBLISH_FORM_DATA);
		}else{
			collection = mongoTemplate.getCollection(Constants.COLLECTION_UN_PUBLISH_FORM_DATA);
		}
		DBObject query = new BasicDBObject();
		query.put("appId",appId);
		DBCursor dbc = collection.find(query);
		List<Object> list = new ArrayList<>();
		while(dbc.hasNext()){
			list.add(JSONObject.toJSON(dbc.next()));
		}
		return list;
	}
	public Set<String> test() {
//		CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto("10000","1501062189892","1501122770351");
//		UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);

		Set<String> list =cubeuaaClient.findLeaderByUser("10064");

//		Query query = new Query();
//		query.addCriteria(Criteria.where("cacheApp.name").is("测试应用"));
//		List<DBObject> list = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_APP);
		return list;
	}
	public Set<Object> getRelateData(RelateDataDto relateDataDto, App relateApp) {
	    //因为前端显示需要去重，现在在后端处理（比如两个单例的申请都是K,现在只会显示一个K
        Set<Object> result = new HashSet<>();
//		List<Object> result = new ArrayList<>();
		if(relateDataDto.getConditions()!= null && relateDataDto.getConditions().size()>0){//筛选条件不为空
			//根据条件查询出符合条件的单据
			Query allQuery = new Query(Criteria.where("appId").is(relateDataDto.getRelateAppId()));
			List<DBObject> allObjects = mongoTemplate.find(allQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
			Set<String> dataIds = new HashSet<>();
			for (DBObject dBObject : allObjects) {
				if(dBObject.get("dataId") != null){
					dataIds.add(dBObject.get("dataId").toString());
				}
			}
			//获取所有满足条件的单据
			List<String> filterDataIds = new ArrayList<>();
			for (String dataId : dataIds) {
				//查询单据信息
				Query dataQuery = new Query(Criteria.where("appId").is(relateDataDto.getRelateAppId()).and("dataId").is(dataId));
				List<DBObject> dataObjects = mongoTemplate.find(dataQuery, DBObject.class, Constants.COLLECTION_APP_DATA);

//				List<ConditionDto> filter = new ArrayList<>();
//				for (ConditionDto queRelation : relateDataDto.getConditions()) {
//					filter.add(queRelation);
//				}
				int i = 0;
				for (DBObject dbObject : dataObjects) {
					for (ConditionDto queFilter :  relateDataDto.getConditions()) {
						if(queFilter.getRelatedQueId().equals(dbObject.get("queId").toString())){
							if(dbObject.get("value") != null ){
								if(dbObject.get("value").toString().equals(queFilter.getQueValue())){
									i = i+1;//如果满足条件就将筛选条件去掉，当筛选条件为空时，表示满足所有条件
								}
							}
						}
					}
				}
				if(i == relateDataDto.getConditions().size()){
					filterDataIds.add(dataId);
				}
			}
			for (String dataId : filterDataIds) {
				//取出符合条件单据的值
				Query valueQuery = new Query(Criteria.where("appId").is(relateApp.getId())
						.and("queId").is(relateDataDto.getRelateQueId())
						.and("dataId").is(dataId));
				DBObject dbObject =  mongoTemplate.findOne(valueQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
				if(dbObject!= null){
					result.add(dbObject.get("value"));
				}
			}
		}else{ //筛选条件为空
			Query valueQuery = new Query(Criteria.where("appId").is(relateApp.getId())
					.and("queId").is(relateDataDto.getRelateQueId()));
			List<DBObject> dbObjects =  mongoTemplate.find(valueQuery, DBObject.class, Constants.COLLECTION_APP_DATA);
			for (DBObject dbObject : dbObjects) {
                result.add(dbObject.get("value"));
			}
		}
		return result;
	}
	/**
	 * 重复性验证
	 * @param duplicateFiledDto
	 * @param app
	 */
	public boolean validateDuplicate(DuplicateFiledDto duplicateFiledDto, App app) {
		Query query = new Query(Criteria.where("appId").is(app.getId())
				.and("queId").is(duplicateFiledDto.getQueId())
				.and("value").is(duplicateFiledDto.getValue()));
		long count = mongoTemplate.count(query, DBObject.class, Constants.COLLECTION_APP_DATA);
		if(count>0){
			return true;
		}
		return false;
	}
	/**
	 *
	 * @param overlapFiledDto
	 * @param app
	 * @return
	 */
	public boolean validateOverlap(OverlapFiledDto overlapFiledDto, App app) throws Exception{
		Query query = new Query(Criteria.where("appId").is(app.getId())
				.and("queId").is(overlapFiledDto.getQueId()));
		List<DBObject> ques = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_APP_DATA);
		for (DBObject dbObject : ques) {
			List<Long> values =  (List<Long>) dbObject.get("value");
			if(values == null || values.size()==0){
				continue;
			}
			if(values.size()!=2){
				continue;
			}else{
				Long targetStartTime = values.get(0);
				Long targetEndTime = values.get(1);
				if(Validators.fieldBlank(targetStartTime) || Validators.fieldBlank(targetEndTime) ){
					continue;
				}
				if(overlapFiledDto.getStartValue().longValue()>=targetStartTime || overlapFiledDto.getEndValue()<= targetEndTime){
					return true;
				}

			}
		}
		return false;
	}
	/**
	 * 验证是否需要密码验证
	 * @param appId
	 * @param processInstanceId
	 * @return
	 */
	public boolean isNeedPwd(String appId, String processInstanceId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
				Criteria.where("processInstanceId").is(processInstanceId));
		query.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		if(applyNode == null){
			return false;
		}
		if(applyNode.getApplyConfig()==null){
			return false;
		}
		if(Validators.fieldNotBlank(applyNode.getApplyConfig().getApplyPass())){
			return true;
		}
		return false;
	}
	public List<DBObject> getAppDataById(String dataId, String processInstanceId, String appId, String workflowNodeId) {
	    if(workflowNodeId!=null){
            Query nodeQuery = new Query(Criteria.where("appId").is(appId)
                .and("processInstanceId").is(processInstanceId)
                .and("auditNodeId").is(workflowNodeId));
            WorkflowNode node = mongoTemplate.findOne(nodeQuery, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
            Query query = new Query(Criteria.where("dataId").is(dataId).and("processInstanceId").is(processInstanceId).and("appId").is(appId));
            List<DBObject> list = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_APP_DATA);
            if(node!= null && list!= null && list.size()>0){
                List<AuditEdit> hides = node.getAuditHides();
                List<AuditEdit> edits = node.getAuditEdits();
                if(hides != null && hides.size()>0){
                    for (DBObject dbObject : list) {
                        //去掉隐藏字段
                        List<DBObject> children = (List<DBObject>) dbObject.get("children");//存在子节点信息
                        for (AuditEdit hideQue : hides) {
                            if(hideQue.getQueId().equals(dbObject.get("queId").toString())){
                                dbObject.put("canHide", true);
                                break;
                            }
                            if(children != null && children.size()>0){
                                for (DBObject child : children) {
                                    if (hideQue.getQueId().equals(child.get("queId").toString())) {
                                        child.put("canHide", true);
                                        break;
                                    }
                                }
                            }
                        }
                        dbObject.put("children", children);
                    }
                }
                if(edits!= null && edits.size()>0){
                    //list中添加可编辑字段
                    for (DBObject dbObject : list) {
                        List<DBObject> children = (List<DBObject>) dbObject.get("children");//存在子节点信息
                        for (AuditEdit editQue : edits) {
                            if(editQue.getQueId().equals(dbObject.get("queId").toString())){
                                dbObject.put("canEdit", true);
                                break;
                            }
                            if(children != null && children.size()>0){
                                for (DBObject child : children) {
                                    if (editQue.getQueId().equals(child.get("queId").toString())) {
                                        child.put("canEdit", true);
                                        break;
                                    }
                                }
                            }

                        }
                        dbObject.put("children", children);
                    }
                }
            }
            return list;
        }else{
            Query query = new Query(Criteria.where("dataId").is(dataId).and("processInstanceId").is(processInstanceId).and("appId").is(appId));
            List<DBObject> list = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_APP_DATA);
	        return list;
        }

	}

}
