/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceInvoker.Discover;


import org.junit.Assert;
import org.junit.Test;
import org.ponder.serviceframework.ServiceInvoker.InvokerProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class TestCase3 {
    private static final Logger log = LoggerFactory.getLogger(TestCase3.class);
    @Test
    public void test1(){

        InvokerProxyFactory factory=InvokerProxyFactory.getFactory("http://192.168.1.8:4001", null, null, "killko");
        Assert.assertNotNull(factory);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
           
        }
    }
}
