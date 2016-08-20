/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Thrift;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.ponder.serviceframework.ServiceInvoker.Discover.NoServiceInstanceException;
import org.ponder.serviceframework.FrameworkContract.ServiceNotDefinedException;
import org.ponder.serviceframework.ServiceInvoker.Discover.ServicePool;
import org.ponder.serviceframework.ServiceInvoker.Discover.WrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class ThriftClient extends ServicePool implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ThriftClient.class);
    private static final GenericKeyedObjectPool<String, TSocket> pool = new GenericKeyedObjectPool<>(
            new TSocketFactory(30000));

    static {
        pool.setMaxIdlePerKey(10);
        pool.setMaxTotal(4000);
        pool.setMaxTotalPerKey(400);
        pool.setMaxIdlePerKey(20);
        pool.setMaxWaitMillis(30000); // 获取不到等待30s
        pool.setMinIdlePerKey(0);
        pool.setBlockWhenExhausted(true);
        pool.setTestOnBorrow(true);
        pool.setTestOnCreate(false);
        pool.setTestOnReturn(false);
        pool.setTestWhileIdle(false);
        pool.setTimeBetweenEvictionRunsMillis(120000L);
        pool.setMinEvictableIdleTimeMillis(600000L);
    }

    private Constructor<?> cons;
    private Class<?> clazz;
    private TSocket tsocket = null;
    private String poolkey = null;

    public ThriftClient(String proto, String sname, String ver) throws ServiceNotDefinedException, NoSuchMethodException {
        super(proto, sname, ver);
        try {
            String clientname = servicename.replace("$Iface", "$Client");
            clazz = Class.forName(clientname);
            cons = clazz.getConstructor(TProtocol.class);
        } catch (ClassNotFoundException ex) {
            throw new ServiceNotDefinedException("服务未定义，可能是没有引入service stub的包，该包可由Thrift IDL定义并生成。");
        }
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public void close() throws Exception {
        if (tsocket != null && poolkey != null) {
            pool.returnObject(poolkey, tsocket);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WrapService instance = pickone();
        if (null == instance) {
            throw new NoServiceInstanceException(this.protocol + "@" + this.servicename + "@" + this.version + "无可用的服务实例");
        }
        try {
            poolkey = instance.getUri();
            tsocket = pool.borrowObject(poolkey);
            if (null != tsocket) {
                if (null != tsocket && !tsocket.isOpen()) {
                    tsocket.open();
                }
            }

//            System.out.println("Cp3:"+System.nanoTime());
            TProtocol protocol = new TMultiplexedProtocol(new TCompactProtocol(tsocket), instance.getServicename() + "@" + instance.getVersion());
            //System.out.println("Cp4:"+System.nanoTime());
            Object client = cons.newInstance(protocol);
            //System.out.println("Cp5:"+System.nanoTime());
            if (client == null) {
                throw new NoServiceInstanceException(this.protocol + "@" + this.servicename + "@" + this.version + "没有可用的服务实例");
            }
            return method.invoke(client, args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            throw ex;
        } catch (TTransportException | InvocationTargetException ex) {
            try {
                pool.invalidateObject(poolkey, tsocket);
            } catch (Exception e1) {
            } finally {
                tsocket = null;
                poolkey = null;
            }
            if (ex.getCause() != null) {
                // 原始的错误(服务器端返回的错误)
                throw ex.getCause();
            } else {
                throw ex;
            }
        } catch (Exception ex) {
            log.warn("ThriftClient invoke Exception");
            throw ex;
        } finally {
            if (tsocket != null && poolkey != null) {
                if (null != tsocket) {
                    tsocket.flush();
                }
                try {
                    pool.returnObject(poolkey, tsocket);
                } catch (IllegalStateException ex) {
                    log.debug("无法返回对象，可能服务端已主动断开连接", ex);
                }
            }
            //System.out.println("Cp6:"+System.nanoTime());
        }
    }
}
