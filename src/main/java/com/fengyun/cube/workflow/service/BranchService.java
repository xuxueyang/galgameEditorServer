package com.fengyun.cube.workflow.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.fengyun.cube.core.util.UUIDGenerator;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.App;
import com.fengyun.cube.workflow.domain.WorkflowNode;
import com.fengyun.cube.workflow.service.dto.request.CreateBranchDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateBranchDto;

@Service
@Transactional
public class BranchService {
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
	private AppService appService;
	/**
	 * 分支节点添加分支—分支其实是视为一个分支节点，下面带着类型为分支节点信息的节点。创建分支，是在已有的分支类型的节点下，添加类型为分支节点信息的节点
	 * @param createBranchDto
	 * @param app 
	 * @return
	 */
	public WorkflowNode createBranch(CreateBranchDto createBranchDto, App app) {
		WorkflowNode branch = new WorkflowNode();
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
		branch.setPreNodeId(createBranchDto.getAuditNodeId());
		branch.setqRobotConfig(null);
		branch.setAppId(createBranchDto.getAppId());
		branch.setRevert(false);
		branch.setTransfer(false);
		branch.setType(Constants.WORKFLOW_TYPE_BRANCH);
		mongoTemplate.save(branch, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		
		appService.updateAppStatus(app);
		return branch;
	}
	public WorkflowNode updateBranch(UpdateBranchDto updateBranchDto, App app) {
		Query query = new Query(Criteria.where("auditNodeId").is(updateBranchDto.getAuditNodeId()));
		Update update = new Update();
		update.set("autoJudges", updateBranchDto.getAutoJudges());
		mongoTemplate.updateFirst(query, update, WorkflowNode.class,Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		
		appService.updateAppStatus(app);
		return mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
	}
	
	public void delete(String auditNodeId, App app) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("auditNodeId").is(auditNodeId),Criteria.where("type").is(Constants.WORKFLOW_TYPE_BRANCH));
		query.addCriteria(criteria);
		mongoTemplate.remove(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		deleteBranch(auditNodeId);
		
		appService.updateAppStatus(app);
	}
	private void deleteBranch(String auditNodeId){
		Query query = new Query(Criteria.where("preNodeId").is(auditNodeId));
		List<WorkflowNode> list = mongoTemplate.find(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		if(list!= null && list.size()>0){
			mongoTemplate.remove(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
			for (WorkflowNode workflowNode : list) {
				deleteBranch(workflowNode.getAuditNodeId());
			}
		}	
	}

	public WorkflowNode findBranchById(String auditNodeId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("auditNodeId").is(auditNodeId),Criteria.where("type").is(Constants.WORKFLOW_TYPE_BRANCH));
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
	}
}
