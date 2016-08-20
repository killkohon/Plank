/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceRegister;

import org.ponder.serviceframework.FrameworkContract.ServiceProtocol;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class Register implements IRegister {

    private static final Logger log = LoggerFactory.getLogger(Register.class);
    private static final String SVRFRMWRK_REGDIR = "ponder/ServiceFramework/Services";
    private static final String SVRFRMWRK_SPLIITER = "@";
    private EtcdClient etcd;

    public EtcdClient getEtcd() {
        return etcd;
    }

    public void setEtcd(EtcdClient etcd) {
        this.etcd = etcd;
    }

    @Override
    public void register(ServiceProtocol protocol, String svrname, String version, String instanceaddr, int weight) throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
        String serviceid = protocol.getName() + SVRFRMWRK_SPLIITER + svrname + SVRFRMWRK_SPLIITER + version;
        try {
            EtcdKeysResponse result = etcd.put(SVRFRMWRK_REGDIR + "/" + serviceid + "/" + instanceaddr, "" + weight).send().get();
        } catch (IOException ex) {
            log.error("与Etcd通讯异常", ex);
            throw ex;
        } catch (EtcdException ex) {
            log.warn("Etcd异常", ex);
            throw ex;
        } catch (EtcdAuthenticationException ex) {
            log.warn("Etcd认证失败", ex);
            throw ex;
        } catch (TimeoutException ex) {
            log.warn("与Etcd通讯超时", ex);
            throw ex;
        }

    }

    @Override
    public void unregister(ServiceProtocol protocol, String svrname, String version, String address) throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
        String serviceid = protocol.getName() + SVRFRMWRK_SPLIITER + svrname + SVRFRMWRK_SPLIITER + version;
        try {
            EtcdKeysResponse result = etcd.delete(SVRFRMWRK_REGDIR + "/" + serviceid + "/" + address).send().get();
            if(etcd.get(SVRFRMWRK_REGDIR + "/" + serviceid).send().get().node.nodes.isEmpty()){
                etcd.deleteDir(SVRFRMWRK_REGDIR + "/" + serviceid).send().get();
            }
        } catch (IOException ex) {
            log.error("与Etcd通讯异常", ex);
            throw ex;
        } catch (EtcdException ex) {
            log.warn("Etcd异常", ex);
            throw ex;
        } catch (EtcdAuthenticationException ex) {
            log.warn("Etcd认证失败", ex);
            throw ex;
        } catch (TimeoutException ex) {
            log.warn("与Etcd通讯超时", ex);
            throw ex;
        }
    }

    @Override
    public void Weight(ServiceProtocol protocol, String svrname, String version, String instanceaddr, int weight) throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
        String serviceid = protocol.getName() + SVRFRMWRK_SPLIITER + svrname + SVRFRMWRK_SPLIITER + version;
        try {
            EtcdKeysResponse result = etcd.post(SVRFRMWRK_REGDIR + "/" + serviceid + "/" + instanceaddr, "" + weight).send().get();
        } catch (IOException ex) {
            log.error("与Etcd通讯异常", ex);
            throw ex;
        } catch (EtcdException ex) {
            log.warn("Etcd异常", ex);
            throw ex;
        } catch (EtcdAuthenticationException ex) {
            log.warn("Etcd认证失败", ex);
            throw ex;
        } catch (TimeoutException ex) {
            log.warn("与Etcd通讯超时", ex);
            throw ex;
        }
    }

}
