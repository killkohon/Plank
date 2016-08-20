/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import mousio.etcd4j.EtcdClient;
import org.ponder.serviceframework.ServiceInvoker.Discover.ServiceInstanceProvider;
import org.ponder.serviceframework.FrameworkContract.ServiceNotDefinedException;
import org.ponder.serviceframework.ServiceInvoker.Discover.ServicePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class InvokerProxyFactory {

    private static final Logger log = LoggerFactory.getLogger(InvokerProxyFactory.class);
    private EtcdClient etcd;
    private String username;
    private String passwd;
    private String founder;
    private List<URI> uris = null;
    private ServiceInstanceProvider provider;

    private InvokerProxyFactory(String[] urlsarray, String username, String passwd, String founder) {
        this.username = username;
        this.passwd = passwd;
        for (String urlstr : urlsarray) {
            if (urlstr == null || urlstr.trim().isEmpty()) {
                continue;
            }
            try {
                URI uri = new URI(urlstr);
                if (uris == null) {
                    uris = new ArrayList<>();
                }
                uris.add(uri);
            } catch (URISyntaxException ex) {
                log.warn("指定的URL[" + urlstr + "]无效", ex);
            }
        }
        if (uris != null && !uris.isEmpty()) {
            URI[] ArrayURIs = new URI[uris.size()];
            uris.toArray(ArrayURIs);
            if (username != null && passwd != null) {
                etcd = new EtcdClient(username, passwd, ArrayURIs);
            } else {
                etcd = new EtcdClient(ArrayURIs);
            }
            provider = ServiceInstanceProvider.getProvider(etcd);
            if (provider == null) {
                log.warn("无法创建ServiceInstanceProvider！");
            }
        }
        if (founder == null || founder.trim().isEmpty()) {
            this.founder = "default";
        } else {
            this.founder = founder;
        }
    }

    private boolean isready() {
        return (uris != null && !uris.isEmpty());
    }

    public static InvokerProxyFactory getFactory(String etcdurls, String username, String passwd, String founder) {
        String[] urlsarray = etcdurls.split(",");
        if (urlsarray.length < 1) {
            log.error("未指定服务注册Etcd的url(s),无法创建RPC实例工厂。");
            return null;
        }
        InvokerProxyFactory factory = new InvokerProxyFactory(urlsarray, username, passwd, founder);
        if (factory.isready()) {
            return factory;
        } else {
            return null;

        }
    }

    public void close() {
        try {
            if (null != provider) {
                provider.close();
            }
        } catch (Exception ex) {
        }
    }

    public Object CreateThriftProxy(String clazzname, String version) throws ServiceNotDefinedException, IllegalArgumentException, NoSuchMethodException {
        ServicePool pool = provider.getServiceInstances("Thrift", clazzname, version);
        return Proxy.newProxyInstance(pool.getClazz().getClassLoader(), pool.getClazz().getInterfaces(), pool);
    }

    public Object CreateGRPCProxy(String clazzname, String version) {
        log.warn("暂未实现GRPC服务调用");
        return null;
    }
}
