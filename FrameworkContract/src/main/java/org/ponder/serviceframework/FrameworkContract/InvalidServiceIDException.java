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
public class InvalidServiceIDException extends Exception{
    public InvalidServiceIDException(String msg){
        super(msg);
    }
}
