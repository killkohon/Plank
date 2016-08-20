/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceRegister;

import org.ponder.serviceframework.FrameworkContract.ServiceProtocol;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;

/**
 *
 * @author han
 */
public interface IRegister {
    public void register(ServiceProtocol protocol,String svrname,String version,String instanceaddr,int weight)throws IOException,EtcdException,EtcdAuthenticationException,TimeoutException;
    public void unregister(ServiceProtocol protocol,String svrname,String version,String address)throws IOException,EtcdException,EtcdAuthenticationException,TimeoutException;
    public void Weight(ServiceProtocol protocol,String svrname,String version,String instanceaddr,int weight)throws IOException,EtcdException,EtcdAuthenticationException,TimeoutException;
}
