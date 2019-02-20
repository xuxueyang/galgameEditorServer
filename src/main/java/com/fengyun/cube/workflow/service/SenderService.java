package com.fengyun.cube.workflow.service;

import com.fengyun.cube.email.SenderInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.*;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Component
public class SenderService {
    private static final String CHARSET = "UTF-8";

    /**
     * 以文本格式发送邮件
     *
     * @param senderInfo 待发送的邮件的信息
     */
    @Async
    public void sendTextMail(SenderInfo senderInfo) throws EmailException, IOException {
        initSender(senderInfo);
        Email email = new SimpleEmail();
        initEmailBasic(senderInfo, email);
        //设置邮件内容
        email.setMsg(senderInfo.getMessage());
        email.send();
    }

    /**
     * 发送包含附件的邮件
     *
     * @throws EmailException
     */
    @Async
    public void sendEmailsWithAttachments(SenderInfo senderInfo) throws EmailException, IOException {
        //创建附件对象
        List<EmailAttachment> emailAttachments = prepareEmailAttachments(senderInfo);

        initSender(senderInfo);
        MultiPartEmail email = new MultiPartEmail();
        initEmailBasic(senderInfo, email);
        //设置邮件内容
        email.setMsg(senderInfo.getMessage());
        //添加附件到邮件
        for (EmailAttachment attachment : emailAttachments) {
            email.attach(attachment);
        }
        email.send();
    }

    private List<EmailAttachment> prepareEmailAttachments(SenderInfo senderInfo) throws MalformedURLException {
        List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
        //在线资源附件
        String[] urls = senderInfo.getAttachmentUrls();
        if (urls != null && urls.length > 0) {
            for (String url : urls) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setURL(new URL(url));
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription(senderInfo.getAttachmentDescription());
                attachment.setName(senderInfo.getAttachmentName());
                emailAttachments.add(attachment);
            }
        }

        //本地资源附件
        String[] paths = senderInfo.getAttachmentPath();
        if (paths != null && paths.length > 0) {
            for (String path : paths) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(path);
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription(senderInfo.getAttachmentDescription());
                attachment.setName(senderInfo.getAttachmentName());
                emailAttachments.add(attachment);
            }
        }
        return emailAttachments;
    }

    @Async
    public void sendHTMLFormattedEmail(SenderInfo senderInfo) throws IOException,
        EmailException {
        initSender(senderInfo);
        ImageHtmlEmail email = new ImageHtmlEmail();
        initEmailBasic(senderInfo, email);
        //创建附件对象
        List<EmailAttachment> emailAttachments = prepareEmailAttachments(senderInfo);
        //添加附件到邮件
        for (EmailAttachment attachment : emailAttachments) {
            email.attach(attachment);
        }

        if (StringUtils.isNotBlank(senderInfo.getUrl())) {
            URL url = new URL(senderInfo.getUrl());
            email.setDataSourceResolver(new DataSourceUrlResolver(url));
        }
        // 设置邮件HTML内容
        email.setHtmlMsg(senderInfo.getHtmlMessage());
        // 设置邮件备用内容
        email.setTextMsg(senderInfo.getTextMessage());
        email.send();
    }

    /**
     * 初始化发送邮件的信息，如果未传值使用默认值.这里的配置是引用cube-email里的配置文件
     *
     * @param senderInfo
     */
    private void initSender(SenderInfo senderInfo) throws IOException {
        Properties props = new Properties();
        props.load(new InputStreamReader(com.fengyun.cube.email.Sender.class.getClassLoader().getResourceAsStream("email.properties"),
            CHARSET));
        //初始化服务器主机
        if (StringUtils.isBlank(senderInfo.getMailServerHost())) {
            senderInfo.setMailServerHost(props.getProperty("email.default.host.name"));
        }

        //初始化服务器端口
        if (senderInfo.getMailServerPort() == 0) {
            senderInfo.setMailServerPort(
                Integer.parseInt(props.getProperty("email.default.smtp.port", "25")));
        }

        //初始化发送服务器的用户名和密码
        if (StringUtils.isBlank(senderInfo.getUserName()) && StringUtils.isBlank(senderInfo.getPassword())) {
            senderInfo.setUserName(props.getProperty("email.default.username"));
            senderInfo.setPassword(props.getProperty("email.default.password"));
        }

        //初始化发件人地址
        if (StringUtils.isBlank(senderInfo.getFromAddress())) {
            senderInfo.setFromAddress(props.getProperty("email.default.from.address"));
        }

        //初始化发件人名称
        if (StringUtils.isBlank(senderInfo.getFromUserName())) {
            senderInfo.setFromUserName(props.getProperty("email.default.from.user.name"));
        }
    }

    private void initEmailBasic(SenderInfo senderInfo, Email email) throws EmailException {
        //设置邮件的字符集为UTF-8防止乱码
        email.setCharset(CHARSET);
        //设置邮件服务器主机
        email.setHostName(senderInfo.getMailServerHost());
        //设置邮件服务器端口
        email.setSmtpPort(senderInfo.getMailServerPort());
        //创建一个密码验证器
        email.setAuthenticator(new DefaultAuthenticator(senderInfo.getUserName(), senderInfo.getPassword()));
        //设置发件人地址
        email.setFrom(senderInfo.getFromAddress(), senderInfo.getFromUserName());
        //设置收件人地址
        email.addTo(senderInfo.getToAddress());
        //遍历用户名及对应的邮箱地址组成的map
        if (!senderInfo.getToAddressMap().isEmpty()) {
            for (Map.Entry<String, String> map : senderInfo.getToAddressMap().entrySet()) {
                email.addTo(map.getValue(), map.getKey());//接收方邮箱和用户名
            }
        }
        //设置邮件标题
        email.setSubject(senderInfo.getSubject());
    }
}
