package com.fengyun.cube.workflow.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fengyun.cube.core.security.SecurityUtils;
import com.fengyun.cube.core.util.UUIDGenerator;
import com.fengyun.cube.core.util.Validators;
import com.fengyun.cube.logger.LogUtil;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.App;
import com.fengyun.cube.workflow.domain.ApplySetting;
import com.fengyun.cube.workflow.domain.AuditEdit;
import com.fengyun.cube.workflow.domain.CacheApp;
import com.fengyun.cube.workflow.domain.PushType;
import com.fengyun.cube.workflow.domain.WorkflowNode;
import com.fengyun.cube.workflow.service.dto.request.CreateAppDto;
import com.fengyun.cube.workflow.service.dto.request.QueryAppDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateAppDto;
import com.fengyun.cube.workflow.service.dto.request.UpdateSettingDto;
import com.fengyun.cube.workflow.service.dto.response.AppDto;
import com.fengyun.cube.workflow.service.dto.response.CacheAppDto;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Service
@Transactional
public class AppService {

	@Autowired
    private MongoTemplate mongoTemplate;

	@Autowired
	private WorkflowService workflowService;

    /**
     * 创建应用
     * @param createAppDto
     */
	public String save(CreateAppDto createAppDto) throws Exception{
		App app = new App();
		app.setId(UUIDGenerator.getUUID());
		if(Validators.fieldBlank(createAppDto.getName())){
			app.setName(Constants.APP_INIT_NAME);
		}else{
			app.setName(createAppDto.getName());
		}
		app.setDescription(createAppDto.getDescriptin());
		app.setAttachment(createAppDto.getAttachment());
		app.setStatus(Constants.APP_STATUS_UN_PUBLISH);
		app.setCreatedId(SecurityUtils.getCurrentUserIdStr());
		app.setCreatedDate(""+ZonedDateTime.now());
		app.setUpdatedId(SecurityUtils.getCurrentUserIdStr());
		app.setUpdatedDate(""+ZonedDateTime.now());
		CacheApp cacheApp = new CacheApp();
		cacheApp.setName(app.getName());
		cacheApp.setAttachment(app.getAttachment());
		cacheApp.setDescription(app.getDescription());

		app.setCacheApp(cacheApp);
		DBCollection collection = null;
		if(!mongoTemplate.collectionExists(Constants.COLLECTION_APP)){
			collection = mongoTemplate.createCollection(Constants.COLLECTION_APP);
		}else{
			collection = mongoTemplate.getCollection(Constants.COLLECTION_APP);
		}
		mongoTemplate.save(app, collection.getName());

		//初始化工作流数据——创建应用时默认有一个申请节点
		workflowService.initApplyWorkflow(app.getId());
		//初始化全局设置
		initAppSetting(app.getId());

		return app.getId();
	}

	private void initAppSetting(String appId) {
		if(!mongoTemplate.collectionExists(Constants.COLLECTION_UN_PUBLISH_SETTING)){
			mongoTemplate.createCollection(Constants.COLLECTION_UN_PUBLISH_SETTING);
		}
		ApplySetting setting = new ApplySetting();
		setting.setAppId(appId);
		setting.setApplyCallback(Constants.SETTING_CALBACK_DEFAULT);
		setting.setApplyCallbackContent(null);
		setting.setApplyCallbackLink(null);
		setting.setApplyLimitMonthly(0);
		setting.setApplyLimitTotal(0);
		setting.setCanApplyTotal(300000);
		setting.setCurrentApplyMonth(0);
		setting.setCurrentApplyTotal(0);
		setting.setCanApplyLimitMonthly(false);
		setting.setCanApplyLimitTotal(false);
		//推送的默认配置
		PushType pushTypes = new PushType();
		//设置可以被推送的数据类型——申请结果的数据、抄送的数据、待处理的数据，（以邮件的方式推送）
		pushTypes.setApplyResult(false);
		pushTypes.setCcData(false);
		pushTypes.setDealData(false);
		setting.setPushTypes(pushTypes);
		//是不是即时推送
		setting.setRealTimePush(false);
		//是不是固定时间推送
		setting.setRegularPush(false);
		List<String> regularPushPeriods = new ArrayList<>();
//		regularPushPeriods.add(Constants.SETTING_PUSH_9);
		//设置固定推送的时间
		setting.setRegularPushPeriods(regularPushPeriods);
		mongoTemplate.save(setting, Constants.COLLECTION_UN_PUBLISH_SETTING);
	}

	/**
     * 更新应用
     * @param updateAppDto
     * @param app
     */
	public void update(UpdateAppDto updateAppDto,App app) throws Exception{
		//更新APP信息
		Query query=new Query(Criteria.where("id").is(app.getId()));
		Update update= new Update();
		if(Constants.APP_STATUS_PUBLISH.equals(app.getStatus())){
			update.set("status", Constants.APP_STATUS_SAVE_UN_PUBLISH);
		}
		// 保存未生效的app信息
		CacheApp cacheAPP = new CacheApp();
		if (Validators.fieldBlank(updateAppDto.getName())) {
			cacheAPP.setName(Constants.APP_INIT_NAME);
		} else {
			cacheAPP.setName(updateAppDto.getName());
		}
		cacheAPP.setDescription(updateAppDto.getDescription());
		cacheAPP.setAttachment(updateAppDto.getAttachment());
		update.set("cacheApp", cacheAPP);
		mongoTemplate.upsert(query, update,App.class, Constants.COLLECTION_APP);
		//保存formdata
		//因为动态数据，所以不做校验，直接存储JSON数据
		if(Validators.fieldNotBlank(updateAppDto.getFormData())){
			createFromToUnPublishCollection(updateAppDto.getFormData(),app);
		}
	}

	private void createFromToUnPublishCollection(String formData, App app) {

		DBCollection collection = null;
		if(!mongoTemplate.collectionExists(Constants.COLLECTION_UN_PUBLISH_FORM_DATA)){
			collection = mongoTemplate.createCollection(Constants.COLLECTION_UN_PUBLISH_FORM_DATA);
		}else{
			collection = mongoTemplate.getCollection(Constants.COLLECTION_UN_PUBLISH_FORM_DATA);
		}
		//先删除旧的配置
		DBObject query = new BasicDBObject();
		query.put("appId", app.getId());
		collection.remove(query);
		//将传来的Json数据加入一些APPId、queId的字段，便于后续查找
		//和前段沟通好，这至少是数组：存储不同的表单
		JSONArray row = JSON.parseArray(formData);
		List<AuditEdit> newAuditEdits = new ArrayList<>();
		for (int i = 0; i < row.size(); i++) {
			//存储每个单个表单的配置
			JSONArray col = row.getJSONArray(i);
			for (int j = 0; j < col.size(); j++) {
				JSONObject obj = col.getJSONObject(j);
				if(Validators.fieldBlank(obj.get("appId"))){
					obj.put("appId", app.getId());
				}
				String queId = UUIDGenerator.getUUID();
				if(Validators.fieldBlank(obj.get("queId"))){
					obj.put("queId", queId);
					//将该字段放到可编辑字段里
					AuditEdit auditEdit = new AuditEdit();
					auditEdit.setQueId(queId);
					auditEdit.setQueTitle(obj.getString("title"));
					newAuditEdits.add(auditEdit);

				}
				//如果组件下面存在组件，如表格组件
				JSONArray children = obj.getJSONArray("children");
				if(children!= null && children.size()>0){
					for (int k = 0; k < children.size(); k++) {
						JSONObject child = children.getJSONObject(k);
						if(Validators.fieldBlank(obj.get("appId"))){
							child.put("appId", app.getId());
						}
						String childQueId = UUIDGenerator.getUUID();
						if(Validators.fieldBlank(child.get("queId"))){
							child.put("queId", childQueId);
							//将该字段放到可编辑字段里
							AuditEdit auditEdit = new AuditEdit();
							auditEdit.setQueId(childQueId);
							auditEdit.setQueTitle(child.getString("title"));
							newAuditEdits.add(auditEdit);
						}
					}
				}

//				System.out.println(obj.toJSONString());
				//如果不配置，那么obj转为Json时那些为空的字段就会被忽略，而前段需要保存这些字段，所以加上后续的这个配置
                String str = JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue);
                collection.insert((DBObject)com.mongodb.util.JSON.parse(str));
			}
		}
		if(newAuditEdits!= null && newAuditEdits.size()>0){
			updateApplyEditQues(newAuditEdits,app.getId());
		}
	}
	//对于应用有三种状态：已发布、发布但是存在更改、未发布（更改时不会影响现有的流程，需要点击重新发布）。同一时间一个应用的已发布只能一个
	private void updateApplyEditQues(List<AuditEdit> newAuditEdits, String appId) {
		// 修改申请节点可编辑字段
		Query applyQuery = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("appId").is(appId),
				Criteria.where("type").is(Constants.WORKFLOW_TYPE_APPLY), Criteria.where("root").is(true));
		applyQuery.addCriteria(criteria);
		WorkflowNode applyNode = mongoTemplate.findOne(applyQuery, WorkflowNode.class,
				Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		List<AuditEdit> auditEdits = new ArrayList<>();
		if(applyNode.getAuditEdits()!= null && applyNode.getAuditEdits().size()>0){
			for (AuditEdit auditEdit : applyNode.getAuditEdits()) {
				auditEdits.add(auditEdit);
			}
		}
		for (AuditEdit auditEdit : newAuditEdits) {
			auditEdits.add(auditEdit);
		}
		Update update = new Update();
		update.set("auditEdits", auditEdits);
		mongoTemplate.updateFirst(applyQuery, update, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
	}

	public App findById(String id) throws Exception{
		App app = mongoTemplate.findById(id, App.class, Constants.COLLECTION_APP);
		return app;
	}

	public AppDto findDetail(String id) throws Exception{

		App app = findById(id);
		if(app == null){
			return null;
		}
		return prepareAppDto(app);
	}
	//App实体转为前段的返回数据
	public AppDto prepareAppDto(App app){
		AppDto appDto = new AppDto();
		appDto.setId(app.getId());
		appDto.setName(app.getName());
		appDto.setStatus(app.getStatus());
		appDto.setDescription(app.getDescription());
		appDto.setAttachment(app.getAttachment());
		appDto.setCreatedId(app.getCreatedId());
		appDto.setCreatedDate(app.getCreatedDate());
		appDto.setUpdatedId(app.getUpdatedId());
		appDto.setUpdatedDate(app.getUpdatedDate());
		appDto.setLastPublishDate(app.getLastPublishDate());

		CacheAppDto cacheAppDto = new CacheAppDto();
		cacheAppDto.setName(app.getCacheApp().getName());
		cacheAppDto.setDescription(app.getCacheApp().getDescription());
		cacheAppDto.setAttachment(app.getCacheApp().getAttachment());
		appDto.setCacheAppDto(cacheAppDto);

		appDto.setFormData(getFormData(app));
		appDto.setWorkflowData(workflowService.getWorkflow(app));
		return appDto;
	}

	private List<Object> getFormData(App app) {
		DBCollection collection = null;
		if(Constants.APP_STATUS_PUBLISH.equals(app.getStatus())){
			collection = mongoTemplate.getCollection(Constants.COLLECTION_PUBLISH_FORM_DATA);
		}else{
			collection = mongoTemplate.getCollection(Constants.COLLECTION_UN_PUBLISH_FORM_DATA);
		}
		DBObject query = new BasicDBObject();
		query.put("appId", app.getId());
		DBCursor dbc = collection.find(query);
		List<Object> list = new ArrayList<>();
		while(dbc.hasNext()){
			list.add(JSONObject.toJSON(dbc.next()));
		}
		return list;

	}

	public List<AppDto> findAll(QueryAppDto queryAppDto) throws Exception{
		Query query = new Query();

		if(Validators.fieldNotBlank(queryAppDto.getSearchContent())){
			Criteria criteria = new Criteria();
			Pattern pattern = Pattern.compile("^.*"+queryAppDto.getSearchContent()+".*$", Pattern.CASE_INSENSITIVE);
			criteria.orOperator(Criteria.where("name").regex(pattern),Criteria.where("description").regex(pattern));
			query.addCriteria(criteria);
		}
		if(queryAppDto.getStatus()!= null && queryAppDto.getStatus().size()>0){
			query.addCriteria(Criteria.where("status").in(queryAppDto.getStatus()));
		}else{
			query.addCriteria(Criteria.where("status").ne(Constants.APP_STATUS_DELETE));
		}
		query.with(new Sort(Sort.Direction.DESC,"createdDate"));
		List<App> apps = mongoTemplate.find(query, App.class, Constants.COLLECTION_APP);

		if(apps!=null && apps.size()>0){
			List<AppDto> list = new ArrayList<>();
			for (App app : apps) {
				//只对于创建者才能看到未发布的APP
				if(Constants.APP_STATUS_UN_PUBLISH.equals(app.getStatus())){
					String userId = SecurityUtils.getCurrentUserIdStr();
					if(userId==null)
						continue;
					if(!userId.equals(app.getCreatedId())){
						continue;
					}
				}
				AppDto appDto = new AppDto();
				appDto.setId(app.getId());
				appDto.setName(app.getName());
				appDto.setStatus(app.getStatus());
				appDto.setDescription(app.getDescription());
				appDto.setAttachment(app.getAttachment());
				appDto.setCreatedId(app.getCreatedId());
				appDto.setCreatedDate(app.getCreatedDate());
				appDto.setUpdatedId(app.getUpdatedId());
				appDto.setUpdatedDate(app.getUpdatedDate());

				CacheAppDto cacheAppDto = new CacheAppDto();
				cacheAppDto.setName(app.getCacheApp().getName());
				cacheAppDto.setDescription(app.getCacheApp().getDescription());
				cacheAppDto.setAttachment(app.getCacheApp().getAttachment());
				appDto.setCacheAppDto(cacheAppDto);

				list.add(appDto);
			}
			return list;
		}
		return null;
	}

	public void delete(String id) {
		Query query=new Query(Criteria.where("id").is(id));
		Update update= new Update();
		update.set("status", Constants.APP_STATUS_DELETE);
		mongoTemplate.upsert(query, update,App.class, Constants.COLLECTION_APP);
	}

	public void updateSetting(UpdateSettingDto updateSettingDto) {
		Query query = new Query(Criteria.where("id").is(updateSettingDto.getId()));
		Update update = new Update();
		update.set("applyCallback", updateSettingDto.getApplyCallback());
		update.set("applyCallbackContent", updateSettingDto.getApplyCallbackContent());
		update.set("applyCallbackLink", updateSettingDto.getApplyCallbackLink());
		update.set("applyLimitMonthly", updateSettingDto.getApplyLimitMonthly());
		update.set("applyLimitTotal", updateSettingDto.getApplyLimitTotal());
		update.set("currentApplyMonth", updateSettingDto.getCurrentApplyMonth());
		update.set("currentApplyTotal", updateSettingDto.getCurrentApplyTotal());
		update.set("appId", updateSettingDto.getAppId());
		update.set("canApplyLimitMonthly", updateSettingDto.isCanApplyLimitMonthly());
		update.set("canApplyLimitTotal", updateSettingDto.isCanApplyLimitTotal());
		update.set("pushTypes", updateSettingDto.getPushTypes());
		update.set("realTimePush", updateSettingDto.isRealTimePush());
		update.set("regularPush", updateSettingDto.isRegularPush());
		update.set("regularPushPeriods", updateSettingDto.getRegularPushPeriods());
		mongoTemplate.updateFirst(query, update, ApplySetting.class, Constants.COLLECTION_UN_PUBLISH_SETTING);
	}

	public ApplySetting getSettingById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, ApplySetting.class, Constants.COLLECTION_UN_PUBLISH_SETTING);
	}

	public void publishApp(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		App app = mongoTemplate.findOne(query, App.class, Constants.COLLECTION_APP);
		Update update = new Update();
		update.set("name", app.getCacheApp().getName());
		update.set("status", Constants.APP_STATUS_PUBLISH);
		update.set("attachment", app.getCacheApp().getAttachment());
		update.set("description", app.getCacheApp().getDescription());
		update.set("updatedId", SecurityUtils.getCurrentUserId());
		update.set("updatedDate", new Date());
		String processInstantceId = UUIDGenerator.getUUID();
		update.set("processInstanceId", processInstantceId);//设置流程实例id
		update.set("lastPublishDate", new Date());//设置流程实例id
		mongoTemplate.updateFirst(query, update, App.class, Constants.COLLECTION_APP);
		//发布表单数据
		publishFormData(app.getId());
		//发布工作流数据
		publishWorkflowData(app.getId(),processInstantceId);
		//发布设置数据
		publishSetting(app.getId());

	}
	private void publishSetting(String appId) {
		Query settingQuery = new Query(Criteria.where("appId").is(appId));
		ApplySetting setting = mongoTemplate.findOne(settingQuery, ApplySetting.class, Constants.COLLECTION_UN_PUBLISH_SETTING);
		mongoTemplate.remove(settingQuery, ApplySetting.class, Constants.COLLECTION_PUBLISH_SETTING);
		mongoTemplate.save(setting, Constants.COLLECTION_PUBLISH_SETTING);
	}

	/**
	 *
	 * @param appId
	 * @param processInstantceId 流程实例id
	 */
	private void publishWorkflowData(String appId, String processInstantceId) {
		Query query = new Query(Criteria.where("appId").is(appId));
		List<WorkflowNode> list = mongoTemplate.find(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		list = addProcessInstanceId(list,processInstantceId);
		for (WorkflowNode workflowNode : list) {
			mongoTemplate.insert(workflowNode, Constants.COLLECTION_PUBLISH_WORKFLOW);
		}
	}

	private List<WorkflowNode> addProcessInstanceId(List<WorkflowNode> list, String processInstantceId) {
		for (WorkflowNode workflowNode : list) {
			workflowNode.setProcessInstanceId(processInstantceId);
		}
		return list;
	}

	private void publishFormData(String appId) {
		Query query = new Query(Criteria.where("appId").is(appId));
		List<DBObject> list = mongoTemplate.find(query, DBObject.class, Constants.COLLECTION_UN_PUBLISH_FORM_DATA);

		//先删除旧的formData
		mongoTemplate.remove(query, DBObject.class, Constants.COLLECTION_PUBLISH_FORM_DATA);
		//保存数据到已发布集合
		if(list!= null && list.size()>0){
			for (DBObject dbObject : list) {
				dbObject.put("id",UUIDGenerator.getUUID());
				mongoTemplate.save(dbObject, Constants.COLLECTION_PUBLISH_FORM_DATA);
			}
		}


	}

	public ApplySetting getSettingByAppId(String appId,String collectionName) {
		Query query = new Query(Criteria.where("appId").is(appId));
		ApplySetting setting = mongoTemplate.findOne(query, ApplySetting.class,collectionName);
		return setting;
	}

		/**
	 * 验证申请、审批、填写、抄送节点、QRobot节点配置——必须配置有负责人
	 * @param appId
	 * @return
	 */
	public boolean checkNodeUsers(String appId) {
		Query query = new Query(Criteria.where("appId").is(appId));
		List<WorkflowNode> list = mongoTemplate.find(query, WorkflowNode.class, Constants.COLLECTION_UN_PUBLISH_WORKFLOW);
		for (WorkflowNode workflowNode : list) {
			if(Constants.WORKFLOW_TYPE_APPLY != workflowNode.getType() && Constants.WORKFLOW_TYPE_BRANCH != workflowNode.getType() && Constants.WORKFLOW_TYPE_BRANCH_NODE != workflowNode.getType()){
				if(Constants.WORKFLOW_TYPE_MULTI_AUDIT!=workflowNode.getType()&&(workflowNode.getAuditUserInfos()== null || workflowNode.getAuditUserInfos().size()==0)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 修改应用状态
	 * @param app
	 */
	public void updateAppStatus(App app) {
		// 如果应为为已发布状态，需要修改为有未发布的保存
		if (Constants.APP_STATUS_PUBLISH.equals(app.getStatus())) {
			Query query = new Query(Criteria.where("id").is(app.getId()));
			Update update = new Update();
			update.set("status", Constants.APP_STATUS_SAVE_UN_PUBLISH);
			mongoTemplate.updateFirst(query, update, App.class,Constants.COLLECTION_APP);
			LogUtil.info(null, null, null, null, "修改应用状态"+app.getId());
		}

	}

    /**
     * 复制应用——复制应用。复制节点（发布的那些全复制，只有一些Id字段会重新设置）。//只有已发布的才能复制
     */
    public String copyAppByApp(App oldApp){
        String newAppId = UUIDGenerator.getUUID();
        App app = new App();
        app.setId(newAppId);
        app.setName("复制应用-"+oldApp.getName());
        app.setDescription(oldApp.getDescription());
        app.setAttachment(oldApp.getAttachment());
        app.setStatus(Constants.APP_STATUS_UN_PUBLISH);

		app.setProcessInstanceId(null);
        app.setCreatedId(SecurityUtils.getCurrentUserIdStr());
        app.setCreatedDate(""+ZonedDateTime.now());
        app.setUpdatedId(SecurityUtils.getCurrentUserIdStr());
        app.setUpdatedDate(""+ZonedDateTime.now());
        CacheApp cacheApp = new CacheApp();
        cacheApp.setName(app.getName());
        cacheApp.setAttachment(app.getAttachment());
        cacheApp.setDescription(app.getDescription());

        app.setCacheApp(cacheApp);
        DBCollection collection = null;
        if(!mongoTemplate.collectionExists(Constants.COLLECTION_APP)){
            collection = mongoTemplate.createCollection(Constants.COLLECTION_APP);
        }else{
            collection = mongoTemplate.getCollection(Constants.COLLECTION_APP);
        }
        mongoTemplate.save(app, collection.getName());
		//初始化工作流数据
        workflowService.copyWorkFlowByApp(oldApp,app);
        //初始化全局设置
        copyAppSetting(oldApp,app);
        //初始化表单数据
        copyFormData(oldApp,app);
        return app.getId();
    }
	//复制表单数据——因为必定已发布，所以复制已发布的数据。因为未发布一定会有数据，所以将已发布的配置才存储到已发布中
	private void copyFormData(App oldApp,App newApp){
        //复制发布的表单
		Query queryPublish = new Query(Criteria.where("appId").is(oldApp.getId()));
		List<DBObject> listPublish = mongoTemplate.find(queryPublish, DBObject.class, Constants.COLLECTION_PUBLISH_FORM_DATA);
		//保存数据到已发布集合
		if(listPublish!= null && listPublish.size()>0){
			for (DBObject dbObject : listPublish) {
				dbObject.put("appId",newApp.getId());
				dbObject.put("id",UUIDGenerator.getUUID());
//                mongoTemplate.save(dbObject,Constants.COLLECTION_PUBLISH_FORM_DATA);
			}
		}
		//将发布的数据塞到未发布中
		// 保存数据到已发布集合
        if(listPublish!= null && listPublish.size()>0){
            for (DBObject dbObject : listPublish) {
				dbObject.put("id",UUIDGenerator.getUUID());
                mongoTemplate.save(dbObject,Constants.COLLECTION_UN_PUBLISH_FORM_DATA);
            }
        }
    }

    //复制应用设置——因为必定已发布，所以复制已发布的数据。因为未发布一定会有数据，所以将已发布的配置才存储到未发布中
    private void copyAppSetting(App oldApp,App newApp) {
		if(!mongoTemplate.collectionExists(Constants.COLLECTION_PUBLISH_SETTING)){
			mongoTemplate.createCollection(Constants.COLLECTION_PUBLISH_SETTING);
		}
		// 全复制，包括设置的未设置的数据
		Query queryPublish = new Query();
		queryPublish.addCriteria(Criteria.where("appId").is(oldApp.getId()));
		ApplySetting oldAppsetting = mongoTemplate.findOne(queryPublish,ApplySetting.class,Constants.COLLECTION_PUBLISH_SETTING);
//		if(oldAppsetting!=null){
//			ApplySetting publishSetting = copyAppSettingByAppSetting(newApp,oldAppsetting);
//			mongoTemplate.save(publishSetting, Constants.COLLECTION_PUBLISH_SETTING);
//		}

        if(!mongoTemplate.collectionExists(Constants.COLLECTION_UN_PUBLISH_SETTING)){
            mongoTemplate.createCollection(Constants.COLLECTION_UN_PUBLISH_SETTING);
        }
        if(oldAppsetting!=null){
            ApplySetting publishSetting = copyAppSettingByAppSetting(newApp,oldAppsetting);
            mongoTemplate.save(publishSetting, Constants.COLLECTION_UN_PUBLISH_SETTING);
        }
    }
    private ApplySetting copyAppSettingByAppSetting(App newApp,ApplySetting oldAppsetting){
        ApplySetting setting = new ApplySetting();
        setting.setAppId(newApp.getId());
        setting.setApplyCallback(oldAppsetting.getApplyCallback());
        setting.setApplyCallbackContent(oldAppsetting.getApplyCallbackContent());
        setting.setApplyCallbackLink(oldAppsetting.getApplyCallbackLink());
        setting.setApplyLimitMonthly(oldAppsetting.getApplyLimitMonthly());
        setting.setApplyLimitTotal(oldAppsetting.getApplyLimitTotal());
        setting.setCanApplyTotal(oldAppsetting.getCanApplyTotal());
        setting.setCurrentApplyMonth(oldAppsetting.getCurrentApplyMonth());
        setting.setCurrentApplyTotal(oldAppsetting.getCurrentApplyTotal());
        setting.setCanApplyLimitMonthly(oldAppsetting.isCanApplyLimitMonthly());
        setting.setCanApplyLimitTotal(oldAppsetting.isCanApplyLimitTotal());
        PushType pushTypes = new PushType();
        if(oldAppsetting.getPushTypes()!=null){
			pushTypes.setApplyResult(oldAppsetting.getPushTypes().isApplyResult());
			pushTypes.setCcData(oldAppsetting.getPushTypes().isCcData());
			pushTypes.setDealData(oldAppsetting.getPushTypes().isDealData());
		}else{
			pushTypes.setApplyResult(false);
			pushTypes.setCcData(false);
			pushTypes.setDealData(false);
		}
        setting.setPushTypes(pushTypes);
        setting.setRealTimePush(oldAppsetting.isRealTimePush());
        setting.setRegularPush(oldAppsetting.isRegularPush());
        List<String> regularPushPeriods = new ArrayList<>();
        if(oldAppsetting.getRegularPushPeriods()!=null||oldAppsetting.getRegularPushPeriods().size()>0){
			for(String time:oldAppsetting.getRegularPushPeriods()){
				regularPushPeriods.add(time);
			}
		}
        setting.setRegularPushPeriods(regularPushPeriods);
        return setting;
    }
}
