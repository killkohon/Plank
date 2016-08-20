/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceCore;

/**
 *
 * @author han
 */
public interface IRPCServer extends AutoCloseable {

    public void addService(String servicename, String version, Object service) throws Exception;

    public void removeService(String servicename, String version);

    public String RPCServiceAddr();

    public void open();

    public boolean isServing();
}
