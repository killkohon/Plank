/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyPutRequest;
import mousio.etcd4j.requests.EtcdKeyRequest;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author han
 */
public class EtcdOperator {

    private static EtcdClient etcd;

    @BeforeClass
    public static void setUp() throws Exception {
        EtcdOperator.etcd = new EtcdClient(URI.create("http://192.168.1.8:4001"));
        EtcdOperator.etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
    }

    @AfterClass
    public static void remove() {
        try {
            etcd.deleteDir("etcd4j_test").recursive().send().get();
            etcd.deleteDir("abc").recursive().send().get();
            EtcdOperator.etcd.close();
        } catch (EtcdAuthenticationException | TimeoutException | IOException | EtcdException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void a() {
        try {
            System.out.println(etcd.getVersion());

            EtcdKeysResponse r = etcd.putDir("etcd4j_test/service").send().get();
            EtcdKeysResponse r2 = etcd.post("etcd4j_test/service", "service1").send().get();
            EtcdKeysResponse r3 = etcd.post("etcd4j_test/service", "service2").send().get();
            EtcdKeysResponse r4 = etcd.getDir("etcd4j_test/service").send().get();
            System.out.println(r4.action);
            EtcdKeysResponse r5 = etcd.getDir("etcd4j_test").send().get();
            System.out.println(r5.action);
        } catch (IOException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EtcdException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EtcdAuthenticationException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void b() {
        try {
            //EtcdKeysResponse r1 = etcd.putDir("abc/thrift@dir123.12.34@2.3.4").send().get();
            EtcdKeysResponse r=etcd.put("abc/thrift@dir123.12.34@2.3.4/192.168.1.1:9966","1").send().get();
             r = etcd.putDir("abc/thrift@dir123.12.34@2.3.4").send().get();
             r = etcd.putDir("abc/cde").send().get();
             System.out.println(r.action);
        } catch (EtcdException | EtcdAuthenticationException | TimeoutException | IOException ex) {
            Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
