/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.util.logging.Level;
import org.junit.Test;
import org.ponder.serviceframework.FrameworkContract.ServiceNotDefinedException;
import org.ponder.serviceframework.ServiceInvoker.Thrift.ThriftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class TestServicePool {
    private static final Logger log = LoggerFactory.getLogger(TestServicePool.class);
    @Test
    public void test1() throws NoSuchMethodException, ServiceNotDefinedException{
        ThriftClient pool=new ThriftClient("Thrift","servicename","1.0.0");
        for(int i=1;i<6;i++){
            WrapService svr=new WrapService("Thrift","servicename","1.0."+i,"192.168.1.9",i,i);
            pool.add(svr);
        }
        for(int i=1;i<6;i++){
            WrapService svr=new WrapService("Thrift","servicename","1.0."+i*2,"192.168.1.9",i*2,i*2);
            pool.add(svr);
        }
        pool.show();
        WrapService svr=new WrapService("Thrift","servicename","1.0.3","192.168.1.9",3,3);
        pool.remove(svr);
        pool.show();
        

    }
}
