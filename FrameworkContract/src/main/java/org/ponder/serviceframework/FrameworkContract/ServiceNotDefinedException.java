/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.FrameworkContract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class ServiceNotDefinedException extends Exception {
    private static final Logger log = LoggerFactory.getLogger(ServiceNotDefinedException.class);
    public ServiceNotDefinedException(String msg){
        super(msg);
    }
}
