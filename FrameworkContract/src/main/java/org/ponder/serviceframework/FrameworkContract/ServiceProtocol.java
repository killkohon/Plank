/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.FrameworkContract;

/**
 *
 * @author han
 */
public enum ServiceProtocol {
    Thrift("Thrift"), GRPC("GRPC");
    private String type;

    private ServiceProtocol(String stype) {
        type = stype;
    }
    public String getName(){
        return this.type;
    }
}
