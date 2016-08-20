/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class Activator implements BundleActivator{
    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) throws Exception {
//        Filter filter=context.createFilter("(&(service.exported.interfaces=*)(|(service.exported.configs=Thrift)(service.exported.configs=GRPC)))");
//        Tracker=new ServiceTracker<>(context,filter,new RemoteServiceTracker());
//        Tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
//        if(Tracker!=null){
//            Tracker.close();
//        }
    }
    
}
