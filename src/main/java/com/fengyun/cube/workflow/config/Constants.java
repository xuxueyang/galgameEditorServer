package com.fengyun.cube.workflow.config;

/**
 * Application constants.
 */
public final class Constants {
    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String MODULE_NAME="workflow";
    /*
     * 应用状态
     */
    public static final String APP_STATUS_PUBLISH = "P";//已发布
    public static final String APP_STATUS_UN_PUBLISH = "UP";//未发布
    public static final String APP_STATUS_SAVE_UN_PUBLISH = "SP";//有未发布的保存
    public static final String APP_STATUS_DELETE = "D";//删除

    public static final String APP_INIT_NAME = "未命名应用";

    /*
     * collection
     */
    public static final String COLLECTION_UN_PUBLISH_FORM_DATA="un_publish_form_data";//未发布表单组件数据
    public static final String COLLECTION_PUBLISH_FORM_DATA="publish_form_data";//已发布表单组件数据
    public static final String COLLECTION_APP="app";//应用数据
    public static final String COLLECTION_UN_PUBLISH_WORKFLOW="un_publish_workflow";//未发布工作流数据
    public static final String COLLECTION_PUBLISH_WORKFLOW="publish_workflow";//已发布工作流数据
    public static final String COLLECTION_PUBLISH_SETTING="publish_setting";//已发布全局设置数据
    public static final String COLLECTION_UN_PUBLISH_SETTING="un_publish_setting";//未发布全局设置数据
    public static final String COLLECTION_APP_DATA = "app_data";//申请表单数据
    public static final String COLLECTION_WORKFLOW_LOG = "workflow_log";//审批日志

    public static final String COLLECTION_WORKFLOW_TASK = "workflow_task";//任务
    public static final String COLLECTION_WORKFLOW_VARIABLE = "workflow_varible";//变量

    public static final String COLLECTION_WORKFLOW_PUSH = "workflow_push";//推送
    /**
     * 节点类型
     */
	public static final int WORKFLOW_TYPE_APPLY = 0; // 申请节点
	public static final int WORKFLOW_TYPE_APPROVAL = 1;// 审批节点
	public static final int WORKFLOW_TYPE_FILL_IN = 2;// 填写节点
	public static final int WORKFLOW_TYPE_CARBON_COPY = 3;// 抄送节点
	public static final int WORKFLOW_TYPE_BRANCH_NODE = 4;// 分支节点
	public static final int WORKFLOW_TYPE_QROBOT_ADD = 5;// q-robot 添加
	public static final int WORKFLOW_TYPE_QROBOT_UPDATE = 6;// q-robot 更新
	public static final int WORKFLOW_TYPE_BRANCH = 7;// 分支信息
    public static final int WORKFLOW_TYPE_MULTI_AUDIT = 8;//逐级审批节点

    /**
     * JudgeType
     * queValue JudgeType judgeValue
     */
    public static final int WORKFLOW_JUDGETYPE_EQ = 0;//等于
    public static final int WORKFLOW_JUDGETYPE_NQ = 1;//不等于
    public static final int WORKFLOW_JUDGETYPE_IN = 2;//包含
    public static final int WORKFLOW_JUDGETYPE_EX = 3;//不包含
    public static final int WORKFLOW_JUDGETYPE_LT = 4;//小于
    public static final int WORKFLOW_JUDGETYPE_LTE = 5;//小于等于
    public static final int WORKFLOW_JUDGETYPE_GT = 6;//大于
    public static final int WORKFLOW_JUDGETYPE_GTE = 7;//大于等于

    /**
     * 申请人可选类型
     */
	public static final int APPLY_TYPE_1 = 1; //工作区可填
	public static final int APPLY_TYPE_2 = 2; //指定用户可填
	public static final int APPLY_TYPE_3 = 3; //所有人可填

	/**
	 * 会签标识
	 */
	public static final int COUNTERSIGN_ONE = 1; //一个人审批
	public static final int COUNTERSIGN_OTHER = 2; //少数服从多数
	public static final int COUNTERSIGN_ALL = 3; //所有人审批

    /**
     * 申请人填写表单后显示
     */
    public static final String SETTING_CALBACK_DEFAULT = "1";//默认文案
    public static final String SETTING_CALBACK_CONTENT = "2";//显示指定内容
    public static final String SETTING_CALBACK_LINK = "3";//显示指定链接

//    public static final String SETTING_PUSH_9 = "1";//早晨9点
//    public static final String SETTING_CALBACK_13 = "2";//下午1点
//    public static final String SETTING_CALBACK_17 = "3";//下午5点

    /**
     * 编号、申请人、申请时间、更新时间、部门主管 que字段id
     */
    public static final String QUE_ID_NO = "1";//编号
    public static final String QUE_ID_CREATED_ID = "2";//申请人
    public static final String QUE_ID_CREATED_DATE = "3";//申请时间
    public static final String QUE_ID_UPDATED_DATE = "4";//更新时间

    /**
     * 单据状态
     */
    public static final String WORKFLOW_STATUS_Y = "Y";//已通过
    public static final String WORKFLOW_STATUS_N = "N";//已拒绝
    public static final String WORKFLOW_STATUS_P = "P";//审批中
    public static final String WORKFLOW_STATUS_D = "D";//草稿

    /**
     * 任务状态
     */
    public static final String WORKFLOW_OPERRATION_P = "P";//审批中
    public static final String WORKFLOW_OPERRATION_APPLY = "apply";//申请
    public static final String WORKFLOW_OPERRATION_YES = "yes";//通过
    public static final String WORKFLOW_OPERRATION_NO = "no";//拒绝
    public static final String WORKFLOW_OPERRATION_TRANSFER = "transfer";//转交
    public static final String WORKFLOW_OPERRATION_REVERT = "revert";//撤回
    public static final String WORKFLOW_OPERRATION_CC = "cc";//抄送

    /**
     * 推送时间类型
     */
    public static final String PUSH_INFO_TIME_REAL = "1";//实时推送
    public static final String PUSH_INFO_TIME_9AM = "2";//定期推送，上午9点
    public static final String PUSH_INFO_TIME_1PM = "3";//定期推送，下午1点
    public static final String PUSH_INFO_TIME_5PM = "4";//定期推送，下午5点

    /**
     * 推送结果
     */
    public static final String PUSH_INFO_RESULT_Y = "Y";//已推送
    public static final String PUSH_INFO_RESULT_N = "N";//未推送
    /**
     * 推送方式类型
     */
    public static final int PUSH_TYPE_MAIL = 1;//邮件
//    public static final int PUSH_TYPE_PHONE = 2;//手机


    /**
     * 推送内容的类型——抄送数据、处理数据、通知结果
     */
    public static final String PUSH_DATA_TYPE_CARBON = "C";//抄送数据
    public static final String PUSH_DATA_TYPE_SOLVE = "S";//待处理数据
    public static final String PUSH_DATA_TYPE_RESULT = "R";//处理结果


    public static final String WORKFLOW_QROBOT_ADD_INSTANCEID = "QRobot";//QRobot自动添加的实例

    /**
     * 邮件模板
     */
    public static final String EMAIL_TEMPLATE_NAME_PUSH_CARBON = "pushCarbonEmail";
    public static final String EMAIL_TEMPLATE_NAME_PUSH_RESULT = "pushResultEmail";
    public static final String EMAIL_TEMPLATE_NAME_PUSH_SOLVE = "pushSolveEmail";
    /**
     * 邮件的配置
     */
    public static final String APPLICATION_CONFIG_EMAIL_SUBJECT_CARBON="抄送数据通知";
    public static final String APPLICATION_CONFIG_EMAIL_SUBJECT_RESULT="申请结果通知";
    public static final String APPLICATION_CONFIG_EMAIL_SUBJECT_SOLVE="待处理数据通知";


    /**
     * 公司逐级审批的最大层数
     */
    public static final int  MULTI_APPLY_NUM = 30;
    /**
     * 特殊审批
     */
    public static final String WORKFLOW_PERMISSION_USER_ID = "c21ac434-7838-4f78-b439-8eb5f8ea4103";
    private Constants() {
    }
}
