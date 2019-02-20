package com.fengyun.cube.workflow.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.rpc.uaa.client.CubeuaaClient;
import com.fengyun.cube.rpc.uaa.dto.CubeuaaRPCDto;
import com.fengyun.cube.rpc.uaa.dto.UserInfo;
import com.fengyun.cube.workflow.domain.*;
import com.fengyun.cube.workflow.repository.OaGroupAuditingAgentMapper;
import com.fengyun.cube.workflow.repository.OaGroupSuperiorsMapper;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.fengyun.cube.core.util.UUIDGenerator;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.service.dto.request.WorkflowDto;
import com.fengyun.cube.workflow.service.dto.request.WorkflowNodeDto;
import com.mongodb.DBObject;

@Service
@Transactional
public class WorkflowService {
	@Autowired
    private MongoTemplate mongoTemplate;

	@Autowired
	private BranchService branchService;
	@Autowired
	private AppService appService;

	@Autowired
	private OaGroupSuperiorsMapper oaGroupSuperiorsMapper;

	@Autowired
	private OaGroupAuditingAgentMapper oaGroupAuditingAgentMapper;

//	@Autowired
//	private OaGroupChargerMapper oaGroupChargerMapper;

	@Autowired
	private CubeuaaClient cubeuaaClient;
	/*
	 * 初始化申请人节点
	 */
	public void initApplyWorkflow(String appId){
		WorkflowNode workflowNode = new WorkflowNode();
		workflowNode.setAppId(appId);
		workflowNode.setAuditNodeId(UUIDGenerator.getUUID());
		workflowNode.setAuditNodeName("申请人");
		workflowNode.setType(Constants.WORKFLOW_TYPE_APPLY);
		workflowNode.setAuditFeedback(true);
		workflowNode.setRevert(true);
		workflowNode.setTransfer(true);
		ApplyConfig applyConfig = new ApplyConfig();
		applyConfig.setApplyType(1);
		applyConfig.setCanApplyRevert(false);
		applyConfig.setApplyPass(null);
		applyConfig.setCanApplyNum(0);
		workflowNode.setApplyConfig(applyConfig);
//		workflowNode.setIndex(0);
		workflowNode.setRoot(true);

		mongoTemplate.save(workflowNode, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
	}

    /**
     *根据App复制工作流节点——根据已有的App的流程节点，查找出来，然后全部重新设置ID，复制
     */
    public void copyWorkFlowByApp(App oldApp,App newApp){
        //复制未发布的数据
        List<WorkflowNode> oldNodes = getWorkflow(oldApp);
        List<WorkflowNode> newNodes = new ArrayList<>();
        for(int i=0;i<oldNodes.size();i++){
        	if(i>0)
	            newNodes.add(copyNode(newApp.getId(),newNodes.get(i-1),oldNodes.get(i),null));
        	else
				newNodes.add(copyNode(newApp.getId(),null,oldNodes.get(0),null));
		}
		//最外层是一个数组，为了保持和原来一样nextAuditNode为null，前端是根据这个判断的，如果不为null会显示两次
		for(int i=0;i<newNodes.size();i++){
        	newNodes.get(i).setNextAuditNode(null);
		}
        for(WorkflowNode workflowNode:newNodes){
            mongoTemplate.insert(workflowNode,Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
        }
//        //复制已发布的
//		for(WorkflowNode workflowNode:newNodes){
//			workflowNode.setProcessInstanceId(newApp.getProcessInstanceId());
//			mongoTemplate.insert(workflowNode,Constants.COLLECTION_PUBLISH_WORKFLOW);
//		}
    }

    private WorkflowNode copyNode(String appId,WorkflowNode preNode,WorkflowNode oldNode,String processInstanceId){
        WorkflowNode workflowNode = new WorkflowNode();

        workflowNode.setAppId(appId);
//        private String auditNodeId;//节点id
        workflowNode.setAuditNodeId(UUIDGenerator.getUUID());
//        private String processInstanceId;//流程实例id，发布时使用
        if(processInstanceId!=null){
            workflowNode.setProcessInstanceId(processInstanceId);
        }

        workflowNode.setApplyConfig(oldNode.getApplyConfig());
        if(oldNode.getAuditEdits()!=null){
			workflowNode.setAuditEdits(Lists.newArrayList(oldNode.getAuditEdits()));
		}
        workflowNode.setAuditFeedback(oldNode.isAuditFeedback());
		if(oldNode.getAuditHides()!=null){
			workflowNode.setAuditHides(Lists.newArrayList(oldNode.getAuditHides()));
		}
        workflowNode.setAuditNodeName(oldNode.getAuditNodeName());
		if(oldNode.getAuditUserInfos()!=null){
			workflowNode.setAuditUserInfos(Lists.newArrayList(oldNode.getAuditUserInfos()));
		}
		if(oldNode.getAutoJudges()!=null){
			workflowNode.setAutoJudges(Lists.newArrayList(oldNode.getAutoJudges()));
		}
        workflowNode.setRevert(oldNode.isRevert());
        workflowNode.setTransfer(oldNode.isTransfer());
        workflowNode.setType(oldNode.getType());
        workflowNode.setRoot(oldNode.isRoot());
        workflowNode.setCanAgentAudit(oldNode.isCanAgentAudit());
        workflowNode.setMultiAudit(oldNode.getMultiAudit());
        workflowNode.setNeedMergeMultiAudit(oldNode.isNeedMergeMultiAudit());
        workflowNode.setBranchEnd(oldNode.isBranchEnd());
        QRobotConfig newConfig = new QRobotConfig();
        if(oldNode.getqRobotConfig()!=null){
            newConfig.setFilterCondition(oldNode.getqRobotConfig().getFilterCondition());
            newConfig.setQueRelation(oldNode.getqRobotConfig().getQueRelation());
            newConfig.setRelatedAppId(oldNode.getqRobotConfig().getRelatedAppId());
        }
        workflowNode.setqRobotConfig(newConfig);
        workflowNode.setCountersign(oldNode.getCountersign());

//        private String preNodeId;//上一个节点id
//        private String nextNodeId;
//        private WorkflowNode nextAuditNode; //下个节点
        if(preNode!=null){
            workflowNode.setPreNodeId(preNode.getAuditNodeId());
            preNode.setNextNodeId(workflowNode.getAuditNodeId());
            preNode.setNextAuditNode(workflowNode);
        }
//        private List<WorkflowNode> branches;//分支
        if(oldNode.getBranches()!=null&&oldNode.getBranches().size()>0){
            List<WorkflowNode> branches = new ArrayList<>();
            for(WorkflowNode branch:oldNode.getBranches()){
                branches.add(copyNode(appId,workflowNode,branch,processInstanceId));

            }
            workflowNode.setBranches(branches);
        }
        return workflowNode;
    }

    /**
     *
     * @param app
     * @return 最外层的节点
     */
	public List<WorkflowNode> getWorkflow(App app){
		WorkflowNode workflowNode = new WorkflowNode();
		List<WorkflowNode> list = new ArrayList<>();
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(app.getId()),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY), Criteria.where("root").is(true));
		query.addCriteria(criteria);
		workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		if(workflowNode != null){
			list.add(workflowNode);
			list = getNextRootWorkflowNode(workflowNode,app.getId(),list,Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		}
		list = getChildWorkflow(app.getId(), list, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);

//		if(Constants.APP_STATUS_PUBLISH.equals(app.getStatus())){
//			Query query = new Query();
//			Criteria criteria = new Criteria();
//			criteria.andOperator(Criteria.where("appId").is(app.getId()),
//					Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY), Criteria.where("root").is(true),
//					Criteria.where("processInstanceId").is(app.getProcessInstanceId()));
//			query.addCriteria(criteria);
//			workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
//			if (workflowNode != null) {
//				list.add(workflowNode);
//				list = getNextRootWorkflowNode(workflowNode, app.getId(), list, Constants.COLLECTION_PUBLISH_WORKFLOW);
//			}
//
//			list = getChildWorkflow(app.getId(), list, Constants.COLLECTION_PUBLISH_WORKFLOW);
//
//		}else{
//			Query query = new Query();
//			Criteria criteria = new Criteria();
//			criteria.andOperator(Criteria.where("appId").is(app.getId()),
//					Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY), Criteria.where("root").is(true));
//			query.addCriteria(criteria);
//			workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
//			if(workflowNode != null){
//				list.add(workflowNode);
//				list = getNextRootWorkflowNode(workflowNode,app.getId(),list,Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
//			}
//			list = getChildWorkflow(app.getId(), list, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
//		}
		return list;
	}
	/**
	 * 获取子节点信息
	 * @param appId
	 * @param list
	 * @param collection
	 * @return
	 */
	private List<WorkflowNode> getChildWorkflow(String appId, List<WorkflowNode> list, String collection) {
		for (WorkflowNode workflowNode : list) {
			if(Constants.WORKFLOW_TYPE_BRANCH_NODE==workflowNode.getType()){ //分支节点，需要加载分支信息
				Query query = new Query();
				Criteria criteria = new Criteria();
				criteria.andOperator(Criteria.where("appId").is(appId),
						Criteria.where("preNodeId").is(workflowNode.getAuditNodeId()), Criteria.where("root").is(false));
				query.addCriteria(criteria);
				List<WorkflowNode> branches = mongoTemplate.find(query, WorkflowNode.class, collection);
				if (branches != null && branches.size() > 0) {
					workflowNode.setBranches(branches);
					for (WorkflowNode branch : branches) {
						getNextChildWorkflowNode(branch, appId, collection);
					}
				}

			}
		}

		return list;
	}

	private void getNextChildWorkflowNode(WorkflowNode branch, String appId, String collection) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("preNodeId").is(branch.getAuditNodeId()), Criteria.where("root").is(false));
		query.addCriteria(criteria);
		WorkflowNode nextAuditNode = mongoTemplate.findOne(query, WorkflowNode.class, collection);
		if(nextAuditNode!= null ){
			branch.setNextAuditNode(nextAuditNode);
			gextNextAuditNode(nextAuditNode, appId, collection);
		}
	}

	private void gextNextAuditNode(WorkflowNode nextAuditNode,String appId,String collection){
		if(Constants.WORKFLOW_TYPE_BRANCH_NODE==nextAuditNode.getType()){//分支节点
			Query branchQuery = new Query();
			Criteria branchCriteria = new Criteria();
			branchCriteria.andOperator(Criteria.where("appId").is(appId),
					Criteria.where("preNodeId").is(nextAuditNode.getAuditNodeId()),
					Criteria.where("root").is(false),
					Criteria.where("type").is(Constants.WORKFLOW_TYPE_BRANCH));
			branchQuery.addCriteria(branchCriteria);
			List<WorkflowNode> branches = mongoTemplate.find(branchQuery, WorkflowNode.class, collection);
			if(branches!= null && branches.size()>0){
				nextAuditNode.setBranches(branches);
				for (WorkflowNode branch : branches) {
					getNextChildWorkflowNode(branch, appId, collection);
				}
			}
			Query nextNodeQuery = new Query();
			Criteria nextNodeCriteria = new Criteria();
			nextNodeCriteria.andOperator(Criteria.where("appId").is(appId),
					Criteria.where("preNodeId").is(nextAuditNode.getAuditNodeId()),
					Criteria.where("root").is(false),
					Criteria.where("type").ne(Constants.WORKFLOW_TYPE_BRANCH));

			nextNodeQuery.addCriteria(nextNodeCriteria);
			WorkflowNode nextNode = mongoTemplate.findOne(nextNodeQuery, WorkflowNode.class, collection);
			if(nextNode!= null ){
				nextAuditNode.setNextAuditNode(nextNode);
				gextNextAuditNode(nextNode, appId, collection);
			}
		}else {// 其它类型节点
			getNextChildWorkflowNode(nextAuditNode, appId, collection);
		}

	}

	/**
	 * 获取第一层节点信息
	 * @param workflowNode
	 * @param appId
	 * @param list
	 * @param collection
	 * @return
	 */
	private List<WorkflowNode> getNextRootWorkflowNode(WorkflowNode workflowNode, String appId, List<WorkflowNode> list, String collection) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),Criteria.where("preNodeId").is(workflowNode.getAuditNodeId()),Criteria.where("root").is(true));
		query.addCriteria(criteria);
		workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, collection);
		if(workflowNode != null){
			list.add(workflowNode);
			getNextRootWorkflowNode(workflowNode, appId, list,collection);
		}
		return list;
	}

	/**
	 * 创建节点
	 * @param workflowDto
	 * @param isRoot
	 * @param app
	 * @return
	 */
	public WorkflowNode createWorkflowNode(WorkflowDto workflowDto,boolean isRoot, App app) {
		WorkflowNode workflow = new WorkflowNode();

		String currentNodeId = UUIDGenerator.getUUID();
		String nextNodeId = null;
		//第一步，先找出workflowDto.getPreNodeId()对应的preWorkflowNode                           节点
		Query preQuery = new Query();
		Criteria preCriteria = new Criteria();
		preCriteria.andOperator(Criteria.where("auditNodeId").is(workflowDto.getPreNodeId()),Criteria.where("root").is(isRoot));
		preQuery.addCriteria(preCriteria);
		WorkflowNode preWorkflowNode = mongoTemplate.findOne(preQuery, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		if(preWorkflowNode != null){
			nextNodeId = preWorkflowNode.getNextNodeId();
		}
		//第二步，修改preWorkflowNode的nextNodeId，为新增节点的nodeId；
		Update preUpdate = new Update();
		preUpdate.set("nextNodeId", currentNodeId);
		mongoTemplate.updateFirst(preQuery, preUpdate, WorkflowNode.class,Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		//第三步，找出preNodeId为preWorkflowNode节点id的节点，将preNodeId改为新增节点的nodeId
		if(preWorkflowNode != null){
			Query nextQuery = new Query();
			Criteria nextCriteria = new Criteria();
			Update nextUpdate = new Update();
			if(Constants.WORKFLOW_TYPE_BRANCH_NODE == preWorkflowNode.getType()){
				nextCriteria.andOperator(Criteria.where("preNodeId").is(preWorkflowNode.getAuditNodeId()),
						Criteria.where("root").is(isRoot),Criteria.where("type").ne(Constants.WORKFLOW_TYPE_BRANCH));
				nextQuery.addCriteria(nextCriteria);
			}else{
				nextCriteria.andOperator(Criteria.where("preNodeId").is(preWorkflowNode.getAuditNodeId()),
						Criteria.where("root").is(isRoot));
				nextQuery.addCriteria(nextCriteria);
			}
			nextUpdate.set("preNodeId", currentNodeId);
			mongoTemplate.updateFirst(nextQuery,nextUpdate, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		}
		workflow.setApplyConfig(null);
		workflow.setAuditEdits(null);
		workflow.setAuditFeedback(true);
		workflow.setAuditHides(null);
		workflow.setAuditNodeId(currentNodeId);
		workflow.setAuditNodeName(workflowDto.getAuditNodeName());
		workflow.setAuditUserInfos(workflowDto.getAuditUserInfos());
		workflow.setAutoJudges(null);
		workflow.setBranches(null);
		workflow.setBranchEnd(false);
		workflow.setMultiAudit(workflowDto.getMultiAudit());
		workflow.setNextAuditNode(null);
		workflow.setNextNodeId(nextNodeId);
		workflow.setPreNodeId(workflowDto.getPreNodeId());
		workflow.setqRobotConfig(null);
		workflow.setAppId(workflowDto.getAppId());
		workflow.setRevert(true);
		workflow.setTransfer(true);
		workflow.setType(workflowDto.getType());
		//如果是审批节点，默认会签为1，单人
        if(workflow.getType()==Constants.WORKFLOW_TYPE_APPROVAL){
            workflow.setCountersign(Constants.COUNTERSIGN_ONE);
        }
        //如果是逐级审批节点，是不允许任务的转交等操作的
		if(workflow.getType()==Constants.WORKFLOW_TYPE_MULTI_AUDIT){
        	workflow.setTransfer(false);
		}
		workflow.setRoot(isRoot);
		mongoTemplate.save(workflow, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		if(Constants.WORKFLOW_TYPE_BRANCH_NODE == workflowDto.getType()){//分支节点类型，创建两个分支
			List<WorkflowNode> branches = initBranches(workflow,workflowDto.getAppId());
			workflow.setBranches(branches);
		}
		appService.updateAppStatus(app);
		return workflow;

	}

	/**
	 * 初始化分支
	 * @param workflow
	 * @param appId
	 * @return
	 */
	private List<WorkflowNode> initBranches(WorkflowNode workflow,String appId) {
		List<WorkflowNode> list = new ArrayList<>();
		WorkflowNode branch = null;
		for (int i = 0; i < 2; i++) {
			branch = new WorkflowNode();
			branch.setApplyConfig(null);
			branch.setAuditEdits(null);
			branch.setAuditFeedback(false);
			branch.setAuditHides(null);
			branch.setAuditNodeId(UUIDGenerator.getUUID());
			branch.setAuditNodeName(null);
			branch.setAuditUserInfos(null);
			branch.setAutoJudges(null);
			branch.setBranches(null);
			branch.setBranchEnd(false);
			branch.setNextAuditNode(null);
			branch.setNextNodeId(null);
			branch.setPreNodeId(workflow.getAuditNodeId());
			branch.setqRobotConfig(null);
			branch.setAppId(appId);
			branch.setRevert(false);
			branch.setTransfer(false);
			branch.setType(Constants.WORKFLOW_TYPE_BRANCH);
			mongoTemplate.save(branch, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
			list.add(branch);
		}
		return list;
	}

	public WorkflowNode findById(String auditNodeId) {
		Query query = new Query(Criteria.where("auditNodeId").is(auditNodeId));
		WorkflowNode workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		return workflowNode;
	}
	public List<WorkflowNode> findByList(List<String> ids){
        Query query = new Query(Criteria.where("auditNodeId").in(ids));
        List<WorkflowNode> list = mongoTemplate.find(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
        return list;
    }

	public WorkflowNode updateNode(WorkflowNodeDto workflowNodeDto, App app) {
		Query query = new Query(Criteria.where("auditNodeId").is(workflowNodeDto.getAuditNodeId()));
		WorkflowNode workflowNode  =  mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		Update update = new Update();
		update.set("applyConfig", workflowNodeDto.getApplyConfig());
		update.set("auditEdits", workflowNodeDto.getAuditEdits() );
		update.set("auditFeedback", workflowNodeDto.isAuditFeedback() );
		update.set("auditHides", workflowNodeDto.getAuditHides() );
		update.set("auditNodeId", workflowNodeDto.getAuditNodeId() );
		update.set("auditNodeName", workflowNodeDto.getAuditNodeName() );
		update.set("auditUserInfos", workflowNodeDto.getAuditUserInfos() );
		update.set("autoJudges", workflowNodeDto.getAutoJudges() );
		update.set("branches",null);
		update.set("nextAuditNode", null );
		update.set("revert", workflowNodeDto.isRevert() );
		//如果是逐级审批节点，是不允许任务的转交等操作的————不然回出现：领导A的领导B转交给了其他人，根据当前逐级审批节点又回创建一次任务逐级上交，然后无限循环了
		if(workflowNode.getType()!=Constants.WORKFLOW_TYPE_MULTI_AUDIT){
			update.set("transfer", workflowNodeDto.isTransfer());
		}
		update.set("type", workflowNodeDto.getType() );
		update.set("multiAudit", workflowNodeDto.getMultiAudit() );
		update.set("appId", workflowNodeDto.getAppId() );
		update.set("index", workflowNodeDto.getIndex() );
		update.set("root", workflowNodeDto.isRoot() );
		update.set("isBranchEnd", workflowNodeDto.isBranchEnd() );
		update.set("qRobotConfig", workflowNodeDto .getqRobotConfig());
		mongoTemplate.updateFirst(query, update, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);

		appService.updateAppStatus(app);

		return mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
	}

	public void delete(WorkflowNode node,App app) {
		//获取当前节点的上一个节点id和下一个节点的id
		String preNodeId = node.getPreNodeId();
		String nextNodeId = node.getNextNodeId();
		//将preNodeId对应节点的nextNodeId设置为 node.getNextNodeId()
		Query preQuery  = new Query(Criteria.where("auditNodeId").is(preNodeId));
		Update preUpdate = new Update();
		preUpdate.set("nextNodeId", nextNodeId);
		mongoTemplate.updateFirst(preQuery, preUpdate, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		//将nextNodeId对应节点的preNodeId设置为node.getPreNodeId()
		Query nextQuery  = new Query(Criteria.where("auditNodeId").is(nextNodeId));
		Update nextUpdate = new Update();
		nextUpdate.set("preNodeId", preNodeId);
		mongoTemplate.updateFirst(nextQuery, nextUpdate, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		//删除当前需要删除的节点
		Query query = new Query(Criteria.where("auditNodeId").is(node.getAuditNodeId()));
		mongoTemplate.remove(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		//分支节点，需要递归删除分支
		if(Constants.WORKFLOW_TYPE_BRANCH_NODE == node.getType()){
			Query branchQuery = new Query();
			Criteria criteria = new Criteria();
			criteria.andOperator(Criteria.where("preNodeId").is(node.getAuditNodeId()),
					Criteria.where("type").is(Constants.WORKFLOW_TYPE_BRANCH));
			branchQuery.addCriteria(criteria);
			List<WorkflowNode> branchs = mongoTemplate.find(branchQuery, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
			if(branchs!= null && branchs.size()>0){
				for (WorkflowNode workflowNode : branchs) {
					branchService.delete(workflowNode.getAuditNodeId(),app);
				}
			}
		}

		appService.updateAppStatus(app);
	}

    /**
     * 根据当前节点Id，搜寻下一个节点。如果是分支需要判断出走哪一个节点
     * @param workflowNode
     * @param appId
     * @param collectionName
     * @param processInstanceId
     * @param dataId 单据Id
     */
    public void searchNextNode(WorkflowNode workflowNode,String appId,String collectionName,List<WorkflowNode> returnList, String processInstanceId, String dataId){
        //分支节点则查找分支信息，其他的（包含分支信息节点，直接查找下一个子节点）
        if(Constants.WORKFLOW_TYPE_BRANCH_NODE==workflowNode.getType()){
            //找分支信息
            Query branchQuery = new Query();
            Criteria branchCriteria = new Criteria();
            branchCriteria.andOperator(Criteria.where("appId").is(appId),
                Criteria.where("preNodeId").is(workflowNode.getAuditNodeId()),
                Criteria.where("type").is(Constants.WORKFLOW_TYPE_BRANCH),
                Criteria.where("processInstanceId").is(processInstanceId));
            branchQuery.addCriteria(branchCriteria);
            //找到分支信息
            List<WorkflowNode> branchNodeList = mongoTemplate.find(branchQuery,WorkflowNode.class,collectionName);
            for(WorkflowNode branch:branchNodeList){
                searchNextNode(branch, appId, collectionName,returnList,processInstanceId,dataId);
            }
        }else{
            //如果是分支节点信息，需要判断分支条件再找下一个
            //满足条件，查找下一个节点,否则返回。
            if(!judgeNode(workflowNode,dataId)){
                return;
            }
            Query query = new Query();
            Criteria criteria = new Criteria();
			criteria.andOperator(Criteria.where("appId").is(appId),
					Criteria.where("preNodeId").is(workflowNode.getAuditNodeId()),
					Criteria.where("processInstanceId").is(processInstanceId));
            query.addCriteria(criteria);
            List<WorkflowNode> childNodeList = mongoTemplate.find(query,WorkflowNode.class,collectionName);
            if(childNodeList.size()==0) {
                WorkflowNode nextRoot = searchNextRootNode(appId,workflowNode,collectionName,processInstanceId);
                if(nextRoot!=null)
                    childNodeList.add(nextRoot);
            }
            //因为childNode可能是分支节点，所以应该递归判断
            for(WorkflowNode node:childNodeList){
                if(Constants.WORKFLOW_TYPE_BRANCH_NODE!=node.getType()){
                    returnList.add(node);
                }else{
                    searchNextNode(node,appId,collectionName,returnList,processInstanceId,dataId);
                }
            }
        }
    }
    //判断节点是否满足条件，分支信息的节点需要判断，其他的默认为true
    private boolean judgeNode(WorkflowNode workflowNode,String dataId){
        boolean condition = true;
        if(Constants.WORKFLOW_TYPE_BRANCH==workflowNode.getType()){
            //判断分支信息，if(true),searchNext
            List<List<AutoJudge>> autoJudges = workflowNode.getAutoJudges();
            if(autoJudges==null)
                return condition;
            //第一层List是或的条件，第二层是且
            //TODO 现在因为没有数据，默认分支都是符合条件的
            for(List<AutoJudge> list:autoJudges){
                if(list==null)
                    continue;
                for(AutoJudge judge:list){
                    //利用queId找到值
					Query judgeQuery = new Query(Criteria.where("dataId").is(dataId)
							.and("queId").is(judge.getQueId())
							.and("appId").is(workflowNode.getAppId()));

					DBObject dbObject = mongoTemplate.findOne(judgeQuery, DBObject.class,Constants.COLLECTION_APP_DATA);
					LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(),
							Constants.MODULE_NAME, "分支节点的类", JSON.toJSONString(dbObject));

					if(dbObject == null){
                    	condition=false;
                    	break;
                    }
					Object queValue = dbObject.get("value");
//					LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(),
//							Constants.MODULE_NAME, "分支节点的值", JSON.toJSONString(queValue));
                    if(queValue==null){
                    	condition=false;
                    	break;
                    }
                    //根据类型做判断
                    //queValue JudgeType judgeValue，有不满足的就设置condition为false
//					LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(),
//							Constants.MODULE_NAME, "节点判断", "JSON.toJSONString(queValue)"+JSON.toJSONString(queValue)+":::"+judge.getJudgeValue());
					try {
                    	// 根据queType转换queValue，之前都是简单的queValue.toString
                        condition = judgeValue(JSON.toJSONString(queValue),judge.getJudgeType(),judge.getJudgeValue(),judge.getQueType());
                        //因为第二层是且的关系，当condition为false，那么跳出判断
                        if(!condition){
                            break;
                        }
                    } catch (Exception e) {
                        //可能字段转换错误
						LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", "转换JudgeValue时的日志打印", e);
						condition = false;
                        break;
                    }
                }
                if(condition){
                    //因为第一层是或，所以满足一个就可以跳出了
                    break;
                }
            }
        }
        return condition;
    }
//    public void test1(){
//    	Query query = new Query();
//    	query.addCriteria(Criteria.where("auditNodeId").is("36d9a0367e344ac0b56a2ca30ec36a26"));
//		WorkflowNode one = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
//		judgeNode(one,null,"fe034b06b0b040628f6e94cf0f92377f");
//	}
    //根据节点，搜索到下一个root节点
    private WorkflowNode searchNextRootNode(String appId,WorkflowNode workflowNode,String collectionName, String processInstanceId){
        if(workflowNode.isRoot()){
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.andOperator(Criteria.where("appId").is(appId),
                Criteria.where("preNodeId").is(workflowNode.getAuditNodeId()),
                Criteria.where("root").is(true),
                Criteria.where("processInstanceId").is(processInstanceId));
            query.addCriteria(criteria);
            WorkflowNode parent = mongoTemplate.findOne(query,WorkflowNode.class,collectionName);
            return parent;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("auditNodeId").is(workflowNode.getPreNodeId()),Criteria.where("processInstanceId").is(processInstanceId));
        query.addCriteria(criteria);
        //TODO 总会有个root节点
        WorkflowNode parent = mongoTemplate.findOne(query,WorkflowNode.class,collectionName);
        return searchNextRootNode(appId,parent,collectionName,processInstanceId);
    }
//    public static void main(String[] args){
//    	String queValue="[0143d222-3ec8-43c4-a198-601a93868de7,adad]";
//		List<String> ids = new ArrayList<>();
//			//直接字符串转数组
//			if(queValue.startsWith("["))
//				queValue = queValue.substring(1,queValue.length());
//			if(queValue.endsWith("]"))
//				queValue = queValue.substring(0,queValue.length()-1);
//			String[] values = queValue.split(",");
//			for(int i=0;i<values.length;i++){
//				ids.add(values[i]);
//			}
//		String a = "\"";
//		if(a.startsWith("\""))
//			a = a.substring(1,a.length());
//			System.out.println(a);
//	}
    //判断具体条件
    private boolean judgeValue(String queValue,int judgeType,String judgeValue,String queType) throws Exception{
		// 根据queType转换queValue.这三种字段的Value转为数组对象，提取其中的id字段与judgeValue判断
		if("member".equals(queType)
			||"role_member".equals(queType)
				||"department".equals(queType)){
			//对于成员来说，有时候类型是String，这时候需要做处理
			List<String> ids = new ArrayList<>();
//			LogUtil.error(SecurityUtils.getCurrentUserIdStr(), SecurityUtils.getCurrentTenantCode(), Constants.MODULE_NAME, "", queValue, queValue);
			if(!queValue.contains("{")||!queValue.contains("}")){
				//直接字符串转数组
				if(queValue.startsWith("["))
					queValue = queValue.substring(1,queValue.length());
				if(queValue.endsWith("]"))
					queValue = queValue.substring(0,queValue.length()-1);
				String[] values = queValue.split(",");
				for(int i=0;i<values.length;i++){
					String str = values[i];
					if(str.startsWith("\""))
						str = str.substring(1,str.length());
					if(str.endsWith("\""))
						str = str.substring(0,str.length()-1);
					ids.add(str);
				}
			}else{
				JSONArray data = JSON.parseArray(queValue);
				//对于成员只支持部分：等于、不等于、包含、未包含
				if(data!=null&&data.size()>0) {

					for (int i = 0; i < data.size(); i++) {
						JSONObject jsonObject = data.getJSONObject(i);
						if (jsonObject.get("id") != null && !"".equals(jsonObject.get("id"))) {
							ids.add(jsonObject.get("id").toString());
						}
					}
				}
			}
				//判断
				if(Constants.WORKFLOW_JUDGETYPE_EQ==judgeType||Constants.WORKFLOW_JUDGETYPE_IN==judgeType){
					if(ids.contains(judgeValue))
						return true;
					else
						return false;
				}else if(Constants.WORKFLOW_JUDGETYPE_EX==judgeType||Constants.WORKFLOW_JUDGETYPE_NQ==judgeType){
					if(ids.contains(judgeValue))
						return false;
					else
						return true;
				}
			return false;
		}else{
			//有些时候value会被添加上""
			if(queValue.startsWith("\"")){
				queValue = queValue.substring(1,queValue.length());
			}
			if(queValue.endsWith("\"")){
				queValue = queValue.substring(0,queValue.length()-1);
			}
			switch (judgeType){
				case Constants.WORKFLOW_JUDGETYPE_EQ:
					return queValue.equals(judgeValue);
				case Constants.WORKFLOW_JUDGETYPE_NQ:
					return !queValue.equals(judgeValue);
				case Constants.WORKFLOW_JUDGETYPE_LT:
					if(Integer.parseInt(queValue)<Integer.parseInt(judgeValue)){
						return true;
					}
					return false;
				case Constants.WORKFLOW_JUDGETYPE_LTE:
					if(Integer.parseInt(queValue)<=Integer.parseInt(judgeValue)){
						return true;
					}
					return false;
				case Constants.WORKFLOW_JUDGETYPE_GT:
					if(Integer.parseInt(queValue)>Integer.parseInt(judgeValue)){
						return true;
					}
					return false;
				case Constants.WORKFLOW_JUDGETYPE_GTE:
					if(Integer.parseInt(queValue)>=Integer.parseInt(judgeValue)){
						return true;
					}
					return false;
				case Constants.WORKFLOW_JUDGETYPE_IN:
					return judgeValue.contains(queValue);
				case Constants.WORKFLOW_JUDGETYPE_EX:
					return !judgeValue.contains(queValue);
				default:
					return false;
			}
		}
    }
    /**
     * 获取申请节点
     * @param processInstanceId
     * @return
     */
	public WorkflowNode findApplyNode(String appId, String processInstanceId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY),
				Criteria.where("processInstanceId").is(processInstanceId));
		query.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		return applyNode;
	}

	public WorkflowNode findById(String appId,String nodeId, String processInstanceId) {
		Query query = new Query(Criteria.where("auditNodeId").is(nodeId).and("processInstanceId").is(processInstanceId).and("appId").is(appId));
		WorkflowNode workflowNode = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		return workflowNode;
	}
	//删选审批节点和逐级审批节点——userId和acctId一样___逐级审批是动态的
	public WorkflowNode getLastNodeByUserId(String appId,String userId,String processInstanceId,String dataId){
		// 根据appId和userId获取到最后的处理的节点————用于特殊审批
		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId).and("processInstanceId").is(processInstanceId));
		List<WorkflowNode> tmpNodeList = mongoTemplate.find(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
		//判断节点
		if(tmpNodeList!=null&&tmpNodeList.size()>0){
			List<WorkflowNode> nodeList = new ArrayList<>();
			for(WorkflowNode workflowNode:tmpNodeList){
				if(Constants.WORKFLOW_TYPE_APPROVAL==workflowNode.getType()){
					if(workflowNode.getAuditUserInfos()!=null&&workflowNode.getAuditUserInfos().size()>0){
						for(AuditUserInfo auditUserInfo:workflowNode.getAuditUserInfos()){
							if(auditUserInfo.getUserId()==userId||auditUserInfo.getAcctId()==userId){
								nodeList.add(workflowNode);
								break;
							}
						}
					}
				}else if(Constants.WORKFLOW_TYPE_MULTI_AUDIT==workflowNode.getType()){
					//逐级审批必须有dataId
					//判断逐级中是不是有该UserId
					List<MultiLeader> supiors = getSupiorByMultiAuditNode(workflowNode, dataId);
					//还要判断节点是不是设置代理人来决定选人
					for(MultiLeader multiLeader:supiors){
						if(workflowNode.isCanAgentAudit()&&multiLeader.getAuditingAgent()!=null
								&&!"".equals(multiLeader.getAuditingAgent().getAgentUserId())){
							if(multiLeader.getAuditingAgent().getAgentUserId().equals(userId)){
								nodeList.add(workflowNode);
								break;
							}
						}else{
							if(multiLeader.getSuperiorId().equals(userId)){
								nodeList.add(workflowNode);
								break;
							}
						}
					}
					if(supiors.contains(userId)){
						nodeList.add(workflowNode);
					}
				}
			}
			//判断获取到的nodeList中哪个才是最后的节点
			List<Integer> ignoreNodeId = new ArrayList<>();
			for(int i=nodeList.size()-1;i>=0;i--){
				if(ignoreNodeId.contains(i))
					continue;
				List<WorkflowNode> returnList = new ArrayList<>();
				searchNextNode(nodeList.get(i),appId,Constants.COLLECTION_WORKFLOW_PUSH,returnList,processInstanceId,dataId);
				for(int ik=i-1;ik>=0;ik--){
					boolean has = false;
					for(int k=returnList.size()-1;k>=0;k--){
						if(returnList.get(k).getAuditNodeId().equals(nodeList.get(ik).getAuditNodeId())){
							//说明下面的节点含有no'deList的，当前的i代表的node不是最后的
							has = true;
							break;
						}
					}
					if(!has){
						//说明下面没有它，也就在上面，那么自动忽视
						ignoreNodeId.add(ik);
					}
				}
			}
			//不在ignorNodeId就是最后的节点
			for(int i=nodeList.size()-1;i>=0;i--){
				if(!ignoreNodeId.contains(i))
					return nodeList.get(i);
			}
			return null;
		}
		return null;
	}
	public List<MultiLeader> getSupiorByMultiAuditNode(WorkflowNode workflowNode,String dataId){
		//得到申请人的逐级领导们
		//先查申请
		//如果没有设置，默认是
		String QUEID = Constants.QUE_ID_CREATED_ID;
		String applyId = "";
		if(workflowNode.getAuditUserInfos()!=null&&workflowNode.getAuditUserInfos().size()>=0){
			AuditUserInfo auditUserInfo = workflowNode.getAuditUserInfos().get(0);
			if(auditUserInfo.getMemQueId()!=null&&!"".equals(auditUserInfo.getMemQueId())){
				//memQueId
				QUEID=auditUserInfo.getMemQueId();
			}else if(auditUserInfo.isGroupAuditAgent()){
				//TODO 如果代理审批人的话，就需要处理创建者ID获取到代理审批人的上级领导等————逐级审批应该不需要做
			}else if(auditUserInfo.isGroupSuperior()){
				//TODO 如果是上级那么就需要创建者ID获取到部门，再获取到上级领导————逐级审批应该不需要做
			}
		}

		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(workflowNode.getAppId())
				.and("processInstanceId").is(workflowNode.getProcessInstanceId())
				.and("dataId").is(dataId).and("queId").is(QUEID));
		BasicDBObject one = mongoTemplate.findOne(query, BasicDBObject.class, Constants.COLLECTION_APP_DATA);
		//TODO 逐级审批是可以配置关联人的
		if(one==null)
			return new ArrayList<>();
		applyId = one.get("value").toString();

		CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto();
		cubeuaaRPCDto.setAcctId(applyId);
		cubeuaaRPCDto.setSpaceCode(SecurityUtils.getCurrentSpaceCode());
		cubeuaaRPCDto.setTenantCode(SecurityUtils.getTokenTenantCode());
		UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);

		String departmentId = userInfo.getDepartment();
		if(departmentId==null||"".equals(departmentId))
			return null;
		//找到了申请人的部门，然后就通过部门，依次获取到上级领导（如果设置了代理审批人，那么设置即可）
		//从表里搜索到部门的上级
		//TODO 这里有个逻辑：是先获取到这个部门负责人来审批，而不是直接的上级。如果负责人和部门ID一样那么再取上级。
//		OaGroupCharger currentCharger = oaGroupChargerMapper.findOneByDepartmentId(departmentId);
//		if(oneByDepartmentId==null||oneByDepartmentId.getChargerId()==null||"".equals(oneByDepartmentId.getChargerId())){
//			//说明这个部门负责人有问题，找上级
//		}
		OaGroupSuperiors currentDepartment = oaGroupSuperiorsMapper.findOneByDepartmentId(departmentId);
		int i=0;
		//部门层级
		List<String> multiNumList = getMultiNum(departmentId);
		int multiNum = multiNumList.indexOf(departmentId)==-1 ?  -1:multiNumList.size()-multiNumList.indexOf(departmentId);
		List<MultiLeader> superiors  = new ArrayList<>();
		//先得到这个部门的负责人
		String chargerId = currentDepartment.getChargerId();
		if(chargerId!=null&&!"".equals(chargerId)&&!chargerId.equals(applyId)){
			//将该部门负责人加进来
			if(currentDepartment!=null) {
				//如果设置了代理人
				OaGroupAuditingAgent agent = oaGroupAuditingAgentMapper.findOneByDepartmentId(departmentId);
				MultiLeader multiLeader = new MultiLeader();
				multiLeader.setAuditingAgent(agent);
				multiLeader.setSuperiorId(currentDepartment.getChargerId());
				superiors.add(multiLeader);
			}
		}
		//随便设置了一个是为防止有人将上级领导设置为自己，会导致死循环
		if(workflowNode.getMultiAudit()<=0)
			workflowNode.setMultiAudit(Constants.MULTI_APPLY_NUM);
		boolean hasNextDepartment = true;
		//TODO workflowNode.getMultiAudit() 这个代表组织层级，1就是到尽头，2就是到第二级————只是我没法知道这个组织的层级啊！总不能每次这里都深度遍历啊..
		//TODO 所以先获取到层级，然后...风云科技是4级吧...那先写死好了...
		//TODO 去UAA的表里增加一个组织架构层级的吧...坑爹欸
//		while(currentDepartment!=null&&i<(4-workflowNode.getMultiAudit())&&i<Constants.MULTI_APPLY_NUM&&hasNextDepartment) {
		while(currentDepartment!=null&&multiNum>=workflowNode.getMultiAudit()&&i<Constants.MULTI_APPLY_NUM&&hasNextDepartment) {
			i++;
			String superiorId = currentDepartment.getSuperiorId();
			if(superiorId==null||"".equals(superiorId))
				break;
			cubeuaaRPCDto = new CubeuaaRPCDto();
			cubeuaaRPCDto.setAcctId(superiorId);
			cubeuaaRPCDto.setSpaceCode(SecurityUtils.getCurrentSpaceCode());
			cubeuaaRPCDto.setTenantCode(SecurityUtils.getTokenTenantCode());
			userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
			if(userInfo==null)
				break;
			departmentId = userInfo.getDepartment();
			if(departmentId==null||"".equals(departmentId))
				break;

			if(departmentId.equals(currentDepartment.getDepartmentId())){
				//说明领导的部门和当前部门一样，已经没有后续的上级了
				hasNextDepartment = false;
			}
			//上一级部门
			currentDepartment = oaGroupSuperiorsMapper.findOneByDepartmentId(departmentId);
			if(currentDepartment!=null) {
				//如果设置了代理人
				OaGroupAuditingAgent agent = oaGroupAuditingAgentMapper.findOneByDepartmentId(departmentId);
//				if(agent!=null&&!superiors.contains(agent.getAgentUserId())) {
//					superiors.add(agent.getAgentUserId());
//					//TODO 这里要求：如果设置了代理人、代理人和自己都可以审批，如果其中有一个人完成，那么就算完成（需要删除另一人的任务，且继续流程
//					//TODO 如果是这个要求，那么有个问题——当代理人设置为下一级或上一级领导，那么会导致（因为还会有合并审批，就很有问题了..)
//					//TODO 这里相当于返回的是一个处理人的链表，那么对于代理审批人不做判断，直接添加到数组中，可是审批人会受到影响。
//					//TODO 因此，这里之后修改，返回一个List<ID，代理审批人ID>
//					//TODO 合并审批和代理人完全没有关系——现在把代理审批人和领导放在一个链上是不符合1.0，2.0就是应该分开了。
//				}else {
//					if(!superiors.contains(superiorId)) {
//						superiors.add(superiorId);
//					}
//				}
				if(workflowNode.isNeedMergeMultiAudit()){
					boolean add = true;
					for(MultiLeader multiLeader:superiors){
						if(multiLeader.getSuperiorId().equals(superiorId)){
							add = false;
							break;
						}
					}
					if(add){
						MultiLeader multiLeader = new MultiLeader();
						multiLeader.setAuditingAgent(agent);
						multiLeader.setSuperiorId(superiorId);
						superiors.add(multiLeader);
					}
				}else{
					MultiLeader multiLeader = new MultiLeader();
					multiLeader.setAuditingAgent(agent);
					multiLeader.setSuperiorId(superiorId);
					superiors.add(multiLeader);
				}
			}
			multiNum = multiNumList.indexOf(departmentId)==-1 ?  -1:multiNumList.size()-multiNumList.indexOf(departmentId);
		}
		return superiors;
	}
	//得到部门层级
	private List<String>  getMultiNum(String departmentId){
		OaGroupSuperiors currentDepartment = oaGroupSuperiorsMapper.findOneByDepartmentId(departmentId);
		boolean hasNextDepartment = true;
		int i=0;
		List<String> departmentIds = new ArrayList<>();
		departmentIds.add(departmentId);
		while(currentDepartment!=null&&i<Constants.MULTI_APPLY_NUM&&hasNextDepartment) {
			i++;
			String superiorId = currentDepartment.getSuperiorId();
			if(superiorId==null||"".equals(superiorId))
				break;
			CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto();
			cubeuaaRPCDto.setAcctId(superiorId);
			cubeuaaRPCDto.setSpaceCode(SecurityUtils.getCurrentSpaceCode());
			cubeuaaRPCDto.setTenantCode(SecurityUtils.getTokenTenantCode());
			UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
			if(userInfo==null)
				break;
			departmentId = userInfo.getDepartment();
			if(departmentId==null||"".equals(departmentId))
				break;

			if(departmentId.equals(currentDepartment.getDepartmentId())){
				//说明领导的部门和当前部门一样，已经没有后续的上级了
				hasNextDepartment = false;
			}
			//上一级部门
			currentDepartment = oaGroupSuperiorsMapper.findOneByDepartmentId(departmentId);
		}
		return departmentIds;
	}
	//根据当前ID得到代理审批人
	public String getAuditAgentId(String userId){
		CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto();
		cubeuaaRPCDto.setAcctId(userId);
		cubeuaaRPCDto.setSpaceCode(SecurityUtils.getCurrentSpaceCode());
		cubeuaaRPCDto.setTenantCode(SecurityUtils.getTokenTenantCode());
		UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
		String departmentId = userInfo.getDepartment();
		if(departmentId==null||"".equals(departmentId))
			return null;
		//找到了申请人的部门，然后就通过部门，依次获取到上级领导（如果设置了代理审批人，那么设置即可）
		//从表里搜索到部门的上级
		OaGroupAuditingAgent agent = oaGroupAuditingAgentMapper.findOneByDepartmentId(departmentId);
		if(agent!=null){
			return agent.getAgentUserId();
		}
		return null;
	}
	//根据当前ID得到上级领导
	public String getSuperiorId(String userId){
		CubeuaaRPCDto cubeuaaRPCDto = new CubeuaaRPCDto();
		cubeuaaRPCDto.setAcctId(userId);
		cubeuaaRPCDto.setSpaceCode(SecurityUtils.getCurrentSpaceCode());
		cubeuaaRPCDto.setTenantCode(SecurityUtils.getTokenTenantCode());
		UserInfo userInfo = cubeuaaClient.getCurrentUserInfo(cubeuaaRPCDto);
		String departmentId = userInfo.getDepartment();
		if(departmentId==null||"".equals(departmentId))
			return null;
		//找到了申请人的部门，然后就通过部门，依次获取到上级领导（如果设置了代理审批人，那么设置即可）
		//从表里搜索到部门的上级
		OaGroupSuperiors currentDepartment = oaGroupSuperiorsMapper.findOneByDepartmentId(departmentId);
		if(currentDepartment!=null){
			return currentDepartment.getSuperiorId();
		}
		return null;
	}
	//TODO 得到最下面的一个审批节点（按广度搜索）
	public List<WorkflowNode> getNextAuditNode(List<WorkflowLog> workflowLogs) throws Exception{
		//得到最后的操作日志时间——它的
		if(workflowLogs!=null&&workflowLogs.size()>0){
			WorkflowLog workflowLog = workflowLogs.get(0);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

			for(WorkflowLog log:workflowLogs){
				if(sdf.parse(log.getApprovalDate()).after(sdf.parse(workflowLog.getApprovalDate()))){
					workflowLog = log;
				}
			}
			Query query = new Query(
					Criteria.where("auditNodeId").is(workflowLog.getNodeId()).andOperator(
							Criteria.where("processInstanceId").is(workflowLog.getProcessInstanceId()).andOperator(
									Criteria.where("appId").is(workflowLog.getAppId())
							)
					)
			);
			WorkflowNode one = mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_PUBLISH_WORKFLOW);
			List<WorkflowNode> nodes = new ArrayList<>();
			searchNextNode(one,workflowLog.getAppId(),Constants.COLLECTION_PUBLISH_WORKFLOW,nodes, workflowLog.getProcessInstanceId(), workflowLog.getDataId());
			return nodes;
		}
		return null;
	}
}
