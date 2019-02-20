package com.fengyun.cube.workflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;

/**
 * Properties specific to JHipster.
 *
 * <p>
 *     Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
	@Bean
    public static AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter() {
        AutoJsonRpcServiceImplExporter exp = new AutoJsonRpcServiceImplExporter();
        return exp;
    }
//    private Config config = new Config();
//
//    public Config getConfig() {
//        return config;
//    }
//
//    public void setConfig(Config config) {
//        this.config = config;
//    }
//
//    public static class Config{
//        private  Email email  = new Email();
////        private  Flow flow  = new Flow();
//
//
//
//        public Email getEmail() {
//            return email;
//        }
//
//        public void setEmail(Email email) {
//            this.email = email;
//        }
//
////        public Flow getFlow() {
////            return flow;
////        }
////
////        public void setFlow(Flow flow) {
////            this.flow = flow;
////        }
//    }
////    public static class Flow{
////        private  Superior superior  = new Superior();
////
////        public Superior getSuperior() {
////            return superior;
////        }
////
////        public void setSuperior(Superior superior) {
////            this.superior = superior;
////        }
////    }
////    public static class Superior{
////        private String oaid;
////
////        public String getOaid() {
////            return oaid;
////        }
////
////        public void setOaid(String oaid) {
////            this.oaid = oaid;
////        }
////    }
//    public static class Email {
//        private  Subject subject = new Subject();
//
//        public Subject getSubject() {
//            return subject;
//        }
//
//        public void setSubject(Subject subject) {
//            this.subject = subject;
//        }
//    }
//
//    public  static class Subject {
//        private String carbon;
//        private String solve;
//        private String result;
//
//        public String getResult() {
//            return result;
//        }
//
//        public void setResult(String result) {
//            this.result = result;
//        }
//
//        public String getSolve() {
//            return solve;
//        }
//
//        public void setSolve(String solve) {
//            this.solve = solve;
//        }
//
//        public String getCarbon() {
//            return carbon;
//        }
//
//        public void setCarbon(String carbon) {
//            this.carbon = carbon;
//        }
//    }
}
