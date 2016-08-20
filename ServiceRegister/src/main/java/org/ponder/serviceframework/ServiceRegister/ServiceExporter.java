/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceRegister;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ponder.serviceframework.FrameworkContract.ServiceProtocol;
import org.ponder.serviceframework.ServiceCore.IRPCServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class ServiceExporter {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceExporter.class);
    private static final String PROPSERVICENAME = "service.exported.interfaces";
    private static final String PROPSERVICETYPE = "service.exported.type";
    private static final String PROPSERVICEVERSION = "service.exported.version";
    private static final String PROPSERVICEWEIGHT = "service.exported.weight";
    private Set<RegItem> RegItems = new TreeSet<>();
    private IRegister register;
    private IRPCServer thriftserver;
    private BundleContext context;
    
    public BundleContext getContext() {
        return context;
    }
    
    public void setContext(BundleContext context) {
        this.context = context;
    }
    
    public IRPCServer getThriftserver() {
        return thriftserver;
    }
    
    public void setThriftserver(IRPCServer thriftserver) {
        this.thriftserver = thriftserver;
    }
    
    public IRegister getRegister() {
        return register;
    }
    
    public void setRegister(IRegister register) {
        this.register = register;
    }
    
    public void AddService(ServiceReference reference) {
        if (reference != null) {
            log.info("Adding Service:");
            String servicename = (String) reference.getProperty(PROPSERVICENAME);
            String servicetype = (String) reference.getProperty(PROPSERVICETYPE);
            String serviceversion = (String) reference.getProperty(PROPSERVICEVERSION);
            String serviceweight = (String) reference.getProperty(PROPSERVICEWEIGHT);
            int weight = 1;
            if (servicename == null || servicetype == null) {
                log.warn("服务名为空或服务类型为空，无法发布远程服务");
                return;
            }
            if (serviceversion == null || serviceversion.trim().isEmpty()) {
                log.info("服务[" + servicename + "]未指定服务版本，默认为0.0.0");
                serviceversion = "0.0.0";
            }
            if (serviceweight != null && !serviceweight.isEmpty()) {
                try {
                    weight = Integer.parseInt(serviceweight);
                } catch (NumberFormatException ex) {
                    weight = 1;
                }
            }
            switch (ServiceProtocol.valueOf(servicetype)) {
                case Thrift: {
                    if (thriftserver == null) {
                        log.warn("Thrift远程服务框架未注入");
                        return;
                    }
                    if (!thriftserver.isServing()) {
                        log.info("Thrift服务框架未启动，现在立即启动...");
                        thriftserver.open();
                    }
                    if (register == null) {
                        log.warn("服务注册器未注入");
                        return;
                    }
                    Object service = context.getService(reference);
                    if (service != null) {
                        try {
                            thriftserver.addService(servicename, serviceversion, service);
                        } catch (Exception ex) {
                            log.warn("将服务[" + servicename +"@"+serviceversion+ "]注册到Thrift远程服务框架失败", ex);
                            return;
                        }
                    } else {
                        log.warn("服务引用[" + servicename + "]为空，无法注册");
                        return;
                    }
                    try {
                        register.register(ServiceProtocol.Thrift, servicename, serviceversion, thriftserver.RPCServiceAddr(), weight);
                        RegItem item = RegItem.create(ServiceProtocol.Thrift, servicename, serviceversion);
                        RegItems.add(item);
                    } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException ex) {
                        log.warn("无法将Thrift服务[" + servicename+"@"+serviceversion + "]注册到服务注册器上", ex);
                        return;
                    }
                    log.info("注册Thrift服务[" + servicename+"@"+serviceversion + "]成功！");
                    break;
                }
                case GRPC: {
                    log.warn("GRPC远程服务注册尚未实现");
                    break;
                }
                default: {
                    log.warn("未知的远程服务类型[" + servicetype + "]，无法发布为远程服务");
                    break;
                }
            }
        }
    }
    
    public void RemoveService(ServiceReference reference) {
        if (reference != null) {
            log.info("Removing Service:");
            String servicename = (String) reference.getProperty(PROPSERVICENAME);
            String servicetype = (String) reference.getProperty(PROPSERVICETYPE);
            String serviceversion = (String) reference.getProperty(PROPSERVICEVERSION);
            if (servicename == null || servicetype == null) {
                return;
            }
            if (serviceversion == null || serviceversion.trim().isEmpty()) {
                serviceversion = "0.0.0";
            }
            switch (ServiceProtocol.valueOf(servicetype)) {
                case Thrift: {
                    RegItem item = RegItem.create(ServiceProtocol.Thrift, servicename, serviceversion);
                    RegItems.remove(item);
                    if (thriftserver == null) {
                        log.warn("Thrift远程服务框架未注入");
                        return;
                    }
                    if (register == null) {
                        log.warn("服务注册器未注入");
                        return;
                    }
                    try {
                        register.unregister(ServiceProtocol.Thrift, servicename, serviceversion, thriftserver.RPCServiceAddr());
                        try{
                            Thread.sleep(2000L);
                        }catch(Exception ex){
                            
                        }
                        thriftserver.removeService(servicename, serviceversion);
                        log.info("反注册Thrift服务[" + servicename +"@"+serviceversion+ "]成功！");
                    } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException ex) {
                        log.warn("无法将Thrift服务[" + servicename +"@"+serviceversion+ "]反注册", ex);
                    }
                    break;
                }
                case GRPC: {
                    break;
                }
                default: {
                    log.warn("未知的远程服务类型[" + servicetype + "]");
                    break;
                }
            }
        }
    }
    
    public static class RegItem implements Comparable {
        
        private ServiceProtocol protocol;
        private String servicename;
        private String version;
        
        private RegItem() {
            
        }
        
        public static RegItem create(ServiceProtocol proto, String sname, String sver) {
            RegItem item = new RegItem();
            item.protocol = proto;
            item.servicename = sname;
            item.version = sver;
            return item;
        }
        
        public ServiceProtocol getProtocol() {
            return protocol;
        }
        
        public void setProtocol(ServiceProtocol protocol) {
            this.protocol = protocol;
        }
        
        public String getServicename() {
            return servicename;
        }
        
        public void setServicename(String servicename) {
            this.servicename = servicename;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        @Override
        public int compareTo(Object o) {
            RegItem other = (RegItem) o;
            if (!this.protocol.getName().equalsIgnoreCase(other.protocol.getName())) {
                return this.protocol.getName().compareTo(other.protocol.getName());
            }
            if (!this.servicename.equalsIgnoreCase(other.getServicename())) {
                return this.servicename.compareTo(other.getServicename());
            }
            if (!this.version.equalsIgnoreCase(other.getVersion())) {
                return this.version.compareTo(other.getVersion());
            }
            return 0;
        }
        
    }
}
