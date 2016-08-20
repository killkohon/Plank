/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceInvoker.Discover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class NoServiceInstanceException extends Exception {
    private static final Logger log = LoggerFactory.getLogger(NoServiceInstanceException.class);
    public NoServiceInstanceException(String msg){
        super(msg);
    }
}
