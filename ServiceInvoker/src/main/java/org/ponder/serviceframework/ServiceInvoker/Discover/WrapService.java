/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceInvoker.Discover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class WrapService {
    private static final Logger log = LoggerFactory.getLogger(WrapService.class);
    private String protocol;
    private String host;
    private int port;
    private String uri;
    private String servicename;
    private String version;
    private int weight;

    private WrapService(){

    }
    
    public WrapService(String proto,String sname,String ver,String host,int port,int weight){
        this.protocol=proto;
        this.servicename=sname;
        this.version=ver;
        this.host=host;
        this.port=port;
        this.uri=host+":"+port;
        this.weight=weight;
    }
    
    public boolean match(WrapService service){
        if(service==null)return false;
        if(protocol==null || !protocol.equalsIgnoreCase(service.getProtocol()))return false;
        if(servicename==null || !servicename.equalsIgnoreCase(service.getServicename()))return false;
       if(version==null || !version.equalsIgnoreCase(service.getVersion()))return false;
       if(uri==null || !uri.equalsIgnoreCase(service.getUri()))return false;
        return true;
    }
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getUri(){
        return uri;
    }
}
