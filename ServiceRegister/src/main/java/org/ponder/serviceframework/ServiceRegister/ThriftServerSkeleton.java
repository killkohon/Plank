/*
 * 
 */
package org.ponder.serviceframework.ServiceRegister;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.ponder.serviceframework.ServiceCore.IRPCServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class ThriftServerSkeleton implements IRPCServer {

    private static final Logger log = LoggerFactory.getLogger(ThriftServerSkeleton.class);
    private static final String INTERFACE_PROCESSOR = "Processor";
    private TServer server = null;
    private TServerTransport serverTransport = null;
    private String host = null;
    private final int port;
    CustomizedThriftMultipleProcessor mp;

    private ThriftServerSkeleton(String host, int port) throws TTransportException {
        this.host = host;
        this.port = port;
        serverTransport = new TServerSocket(port);
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        mp = new CustomizedThriftMultipleProcessor();
        server = new TThreadPoolServer(new Args(serverTransport).protocolFactory(protocolFactory).processor(mp));
    }

    public static ThriftServerSkeleton newinstance(String host, int port) throws TTransportException {

        try {
            ThriftServerSkeleton theserver = new ThriftServerSkeleton(host, port);
            return theserver;
        } catch (TTransportException ex) {
            log.error("无法创建Thrift服务实例，可能服务端口 {} 已被占用", port);
            throw ex;
        }
    }

    @Override
    public void open() {
        new Thread(() -> {
            try {
                server.serve();
                log.info("Thrift Server start at {}:{}", host, port);
            } catch (Exception e) {
                log.warn("Thrift Server FAIL to start at {}:{}", host, port, e);
            }
        }).start();

    }

    @Override
    public void close() throws Exception {

         if (server != null) {
            server.stop();
        }

        if (serverTransport != null) {
            serverTransport.close();
        }
            log.info("Thrift Server[{}:{}] stopped", host, port);

    }

    @Override
    public void addService(String servicename, String version, Object service) throws Exception {
        TProcessor processor = createTProcessor(servicename, service);
        mp.registerProcessor(servicename, version, processor);
    }

    @Override
    public void removeService(String servicename, String version) {
        mp.unregisterProcessor(servicename, version);
    }

    private TProcessor createTProcessor(String serviceName, Object service) throws Exception {
        int dex = serviceName.lastIndexOf('$');
        String iface = serviceName.substring(dex + 1, serviceName.length());
        String processorClazz = serviceName.replace(iface, INTERFACE_PROCESSOR);
        ThriftProxyHandler<?> ProxyHandler = new ThriftProxyHandler<>(service);
        return (TProcessor) Class.forName(processorClazz).getConstructors()[0].newInstance(ProxyHandler
                .createProxy());
    }

    @Override
    public boolean isServing() {
        if (server == null) {
            return false;
        }
        return server.isServing();
    }

    @Override
    public String RPCServiceAddr() {
        return host + ":" + port;
    }

    public static class ThriftProxyHandler<T> implements InvocationHandler {

        private T instance;

        public ThriftProxyHandler(T instance) {
            this.instance = instance;
        }

        public Object createProxy() {
            return Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Object res = method.invoke(instance, args);
                return res;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("服务调用失败，method：" + method.getName(), e);
            }
            return instance;
        }

    }

}
