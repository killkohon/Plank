package org.ponder.serviceframework.ServiceInvoker.Thrift;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KillkoHon
 */
public class TSocketFactory extends BaseKeyedPooledObjectFactory<String, TSocket> {

    private static final Logger LOG = LoggerFactory.getLogger(TSocketFactory.class);

    private int timeout;

    public TSocketFactory(int timeout) {
        this.timeout = timeout;
    }

    public TSocketFactory() {
        this(30000);
    }

    @Override
    public TSocket create(String key) throws Exception {
        if (null == key) {
            return null;
        }

        String[] endpoint = key.split(":");
        if (endpoint.length != 2) {
            return null;
        }

        String host = endpoint[0];
        if (host == null || host.trim().isEmpty()) {
            return null;
        }

        int port;
        try {
            port = Integer.parseInt(endpoint[1]);
        } catch (Exception ex) {
            return null;
        }
        TSocket tsocket=new TSocket(host, port, timeout);
        tsocket.open();
        return tsocket;
    }

    @Override
    public PooledObject<TSocket> wrap(TSocket value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public void destroyObject(String key, PooledObject<TSocket> p) throws Exception {
        if (null != p) {
            try {
                TSocket tsocket = p.getObject();
                if (null != tsocket) {
                    if(tsocket.isOpen()){
                        tsocket.flush();
                        tsocket.close();
                    }
                }
            } catch (Exception e) {
                LOG.info("连接池在销毁TSocket对象时异常，但不影响正常运作，留日志待查", e);
            }
        }
    }

    @Override
    public boolean validateObject(String key, PooledObject<TSocket> p) {
        return null != p.getObject();
    }
}
