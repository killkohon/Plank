/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;
import org.ponder.samples.Thrift.calculate;
import org.ponder.serviceframework.ServiceInvoker.InvokerProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class TestInvoker {
    private static final Logger log = LoggerFactory.getLogger(TestInvoker.class);
    @Test
    public void test1(){
        InvokerProxyFactory factory=InvokerProxyFactory.getFactory("http://192.168.1.8:4001", null, null, "founder");
        calculate.Client cal=null;
        try {
            cal = (calculate.Client)factory.CreateThriftProxy("org.ponder.samples.Thrift.calculate$Iface", "1.0.0");
        } catch (Throwable ex) {
            log.warn("",ex);
        }
        Assert.assertNotNull(cal);
    }
}
