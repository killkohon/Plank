/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceTester;

import org.apache.thrift.TException;
import org.ponder.samples.Thrift.calculate;
import org.ponder.serviceframework.FrameworkContract.ServiceNotDefinedException;
import org.ponder.serviceframework.ServiceInvoker.InvokerProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class MainTest {

    private static final Logger log = LoggerFactory.getLogger(MainTest.class);

    public static void main(String[] argv) {
        InvokerProxyFactory factory = InvokerProxyFactory.getFactory("http://192.168.1.8:4001", null, null, "founder");
//        try {
//            calculate.Iface cal = (calculate.Iface) factory.CreateThriftProxy("org.ponder.samples.Thrift.calculate$Iface", "1.0.0");
//            for(int i=0;i<20;i++){
//            cal.add(0, 0);}
//        } catch (ServiceNotDefinedException ex) {
//
//        } catch (TException ex) {
//            
//        }
          long start=System.currentTimeMillis();
          for(int i=0;i<100;i++){
              Thread k=new Thread(){
                  @Override
                  public void run() {
                      try {
                          calculate.Iface cal=(calculate.Iface)factory.CreateThriftProxy("org.ponder.samples.Thrift.calculate$Iface", "1.0.0");
                          for(long j=0;j<200;j++){
                              for(long k=0;k<500;k++){
                                  try {
                                      long l=cal.add(j, k);
                                      if(l!=j+k){
                                          log.warn("error:"+j+"+"+k+"->"+l);
                                      }
                                  } catch (TException ex) {
                                     log.warn("",ex);
                                  }
                                  
                              }
                          }
                          log.info(""+(System.currentTimeMillis()-start));
                      } catch (ServiceNotDefinedException | IllegalArgumentException | NoSuchMethodException ex) {
                         
                      }
                  }
              };
            k.start();        
        }

    }
}
