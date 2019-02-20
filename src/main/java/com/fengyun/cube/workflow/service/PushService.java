package com.fengyun.cube.workflow.service;

import com.fengyun.cube.core.util.UUIDGenerator;
import com.fengyun.cube.email.SenderInfo;
import com.fengyun.cube.workflow.config.ApplicationProperties;
import com.fengyun.cube.workflow.config.Constants;
import com.fengyun.cube.workflow.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@Service
public class PushService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AppService appService;
    //判断推送权限
    @Autowired
    private WorkflowService workflowService;
    @Autowired
    private SpringTemplateEngine templateEngine;
    @Autowired
    private SenderService sender;

    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     *
     * @param appId
     * @param dataId
     * @param processInstanceId
     * @param pushType 目前只支持邮件推送
     * @param pushAddress
     * @return
     */
    public boolean createPushInfo(String appId,String dataId,String processInstanceId,
                                  int pushType,String pushAddress,String pushDataType,String pushValue)throws Exception{
        if(!mongoTemplate.collectionExists(Constants.COLLECTION_WORKFLOW_PUSH)){
            mongoTemplate.createCollection(Constants.COLLECTION_WORKFLOW_PUSH);
        }
        //判断推送类型，以及  校验推送地址的字段
        switch (pushType){
            case Constants.PUSH_TYPE_MAIL:
                if(!checkEmaile(pushAddress)){
                    return false;
                }
                break;
            default:
                return false;
        }
        //获取到配置
        ApplySetting applySetting = appService.getSettingByAppId(appId,Constants.COLLECTION_PUBLISH_SETTING);
        if(applySetting!=null){
            //得到全局配置，如果符合条件，那么允许创建
            boolean canPush = false;
            PushType types = applySetting.getPushTypes();
            switch (pushDataType){
                case Constants.PUSH_DATA_TYPE_CARBON:
                    if(types.isCcData()){
                        canPush = true;
                        if(pushValue==null||"".equals(pushValue)){
                            pushValue = "有数据抄送给您，请查看";
                        }
                    }
                    break;
                case Constants.PUSH_DATA_TYPE_SOLVE:
                    if(types.isApplyResult()){
                        canPush = true;
                        if(pushValue==null||"".equals(pushValue)){
                            pushValue = "您有待处理的数据，请查看";
                        }
                    }
                    break;
                case Constants.PUSH_DATA_TYPE_RESULT:
                    if(types.isDealData()){
                        canPush = true;
                        if(pushValue==null||"".equals(pushValue)){
                            pushValue = "您申请的流程已有结果，请查看";
                        }
                    }
                    break;
            }
            if(!canPush){
                return false;
            }
            boolean realTimePush = applySetting.isRealTimePush();
            boolean regularPush = applySetting.isRegularPush();
            PushInfo pushInfo = null;
            if(realTimePush){
                pushInfo = initPushInfo(appId,dataId,processInstanceId,
                    pushType,pushAddress,pushValue,
                    new ArrayList<String>(){{add(Constants.PUSH_INFO_TIME_REAL);}});
                push(pushInfo);
            }else if(regularPush){
                List<String>  periods = applySetting.getRegularPushPeriods();
                if(periods!=null&&periods.size()>0){
                    List<String> pushTimeArr = new ArrayList<>();
                    for(String period:periods){
                        if(Constants.PUSH_INFO_TIME_9AM.equals(period)||
                            Constants.PUSH_INFO_TIME_1PM.equals(period)||
                            Constants.PUSH_INFO_TIME_5PM.equals(period)
                            )
                        {
                            pushTimeArr.add(period);
                        }
                    }
                    if(pushTimeArr.size()>0){
                        pushInfo = initPushInfo(appId,dataId,processInstanceId,
                            pushType,pushAddress,pushValue,pushTimeArr);
                    }
                }
            }
            if(pushInfo!=null){
                mongoTemplate.save(pushInfo,Constants.COLLECTION_WORKFLOW_PUSH);
                return true;
            }
        }
        return false;
    }
    private PushInfo initPushInfo(String appId,String dataId,String processInstanceId,int pushType,String pushAddress,String pushValue,List<String> pushTime){
        PushInfo pushInfo = new PushInfo();
        pushInfo.setId(UUIDGenerator.getUUID());
        pushInfo.setAppId(appId);
        pushInfo.setDataId(dataId);
        pushInfo.setPushResult(Constants.PUSH_INFO_RESULT_N);
        pushInfo.setProcessInstanceId(processInstanceId);
        pushInfo.setPushType(pushType);
        pushInfo.setPushTime(pushTime);
        pushInfo.setPushAddress(pushAddress);
        pushInfo.setPushValue(pushValue);
        pushInfo.setCreatedDate(""+ZonedDateTime.now());
        return pushInfo;
    }
    @Scheduled(cron = "0 0 9 ? * *")
    public void pushInfo_9AM()throws Exception {
        //执行代码
        pushInfo(Constants.PUSH_INFO_TIME_9AM,null,null);
    }
    @Scheduled(cron = "0 0 13 ? * *")
    public void pushInfo_1PM()throws Exception {
        //推送全部
        pushInfo(Constants.PUSH_INFO_TIME_1PM,null,null);

    }
    @Scheduled(cron = "0 0 17 ? * *")
    public void pushInfo_5PM()throws Exception {
        //执行代码
        pushInfo(Constants.PUSH_INFO_TIME_5PM,null,null);
    }



private void pushInfo(String pushTime,String appId,String dataId) throws Exception{
         if(!mongoTemplate.collectionExists(Constants.COLLECTION_WORKFLOW_PUSH)){
             mongoTemplate.createCollection(Constants.COLLECTION_WORKFLOW_PUSH);
         }
        //推动制定类型的消息
        //查找出制定类型的消息，推送，并将字段设为已推送
        Query query = new Query();
        Criteria criteria = Criteria.where("pushResult").is(Constants.PUSH_INFO_RESULT_N);
        if(appId!=null&&!"".equals(appId)){
            criteria.and("appId").is(appId);
        }
        if(dataId!=null&&!"".equals(dataId)){
            criteria.and("dataId").is(dataId);
        }
        List<PushInfo> infoList = mongoTemplate.find(query.addCriteria(criteria),PushInfo.class,Constants.COLLECTION_WORKFLOW_PUSH);
        if(infoList!=null&&infoList.size()>0){
            //发送邮件
            for(PushInfo pushInfo:infoList){
                //判断时间
                if(pushInfo.getPushTime().contains(pushTime)){
                    push(pushInfo);
                    mongoTemplate.save(pushInfo,Constants.COLLECTION_WORKFLOW_PUSH);
                }
            }

        }
    }
    //不做null
    public void push(PushInfo pushInfo) throws Exception{
        if(pushInfo!=null&&Constants.PUSH_INFO_RESULT_N.equals(pushInfo.getPushResult())){
            //选择模板
            switch (pushInfo.getPushType()){
                case Constants.PUSH_TYPE_MAIL:
                    //邮件模板
                    //发送邮件
                    //邮件主题
                    String emailContent=null;
                    String subject = null;
                    Context context = new Context(Locale.CHINA);
                    //设置context变量，替换html页面中的数据
                    switch (pushInfo.getPushDataType()){
                        case Constants.PUSH_DATA_TYPE_CARBON:
//                            subject = applicationProperties.getConfig().getEmail().getSubject().getCarbon();
                            subject = Constants.APPLICATION_CONFIG_EMAIL_SUBJECT_CARBON;
                            emailContent = templateEngine.process(Constants.EMAIL_TEMPLATE_NAME_PUSH_CARBON, context);
                            break;
                        case Constants.PUSH_DATA_TYPE_RESULT:
//                            subject = applicationProperties.getConfig().getEmail().getSubject().getResult();
                            subject = Constants.APPLICATION_CONFIG_EMAIL_SUBJECT_RESULT;
                            emailContent = templateEngine.process(Constants.EMAIL_TEMPLATE_NAME_PUSH_RESULT, context);
                            break;
                        case Constants.PUSH_DATA_TYPE_SOLVE:
//                            subject = applicationProperties.getConfig().getEmail().getSubject().getSolve();
                            subject = Constants.APPLICATION_CONFIG_EMAIL_SUBJECT_SOLVE;
                            emailContent = templateEngine.process(Constants.EMAIL_TEMPLATE_NAME_PUSH_SOLVE, context);
                            break;
                        default:
                            subject = "工作流";
                            emailContent=pushInfo.getPushValue();
                    }
                    SenderInfo senderInfo = new SenderInfo();
                    senderInfo.setSubject(subject);
                    senderInfo.setToAddress(pushInfo.getPushAddress());
                    senderInfo.setHtmlMessage(emailContent);
                    senderInfo.setTextMessage(pushInfo.getPushValue());
                    sender.sendHTMLFormattedEmail(senderInfo);
                    break;
            }
            pushInfo.setPushResult(Constants.PUSH_INFO_RESULT_Y);
        }
    }
    /**
     * 正则表达式校验邮箱
     * @param emaile 待匹配的邮箱
     * @return 匹配成功返回true 否则返回false;
     */
    private static boolean checkEmaile(String emaile){
        if(emaile==null||"".equals(emaile)){
            return false;
        }
        String RULE_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        //正则表达式的模式
        Pattern p = Pattern.compile(RULE_EMAIL);
        //正则表达式的匹配器
        Matcher m = p.matcher(emaile);
        //进行正则匹配
        return m.matches();
    }
}
