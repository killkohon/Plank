/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class TestCase2 {

    private static final Logger log = LoggerFactory.getLogger(TestCase2.class);
    private static EtcdClient etcd;

    @BeforeClass
    public static void setUp() throws Exception {
        TestCase2.etcd = new EtcdClient(URI.create("http://192.168.1.8:4001"));
        TestCase2.etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
    }

    @AfterClass
    public static void remove() {
        try {
            TestCase2.etcd.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(EtcdOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private Map<String, String> getleafnode(Map<String, String> leaves, EtcdNode node) {
        if (node.dir) {
            if (node.nodes != null && !node.nodes.isEmpty()) {
                Iterator iterator = node.nodes.iterator();
                while (iterator.hasNext()) {
                    EtcdNode item = (EtcdNode) iterator.next();
                    leaves = getleafnode(leaves, item);
                }
            }
            return leaves;
        } else {
            if (leaves == null) {
                leaves = new HashMap<>();
            }
            leaves.put(node.key, node.value);
            return leaves;
        }
    }
}
