/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ponder.serviceframework.ServiceRegister;

import java.util.HashMap;
import java.util.Map;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author han
 */
public class CustomizedThriftMultipleProcessor implements TProcessor {
    private static final Logger Log = LoggerFactory.getLogger(CustomizedThriftMultipleProcessor.class);
    private static final String SEPARATOR=":";
    private static final String DEFAULT_SUBJECT="default";
    
    private final Map<String,TProcessor> SVRPROCESSORMAP=new HashMap<>();
    
    CustomizedThriftMultipleProcessor(){

    }

    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
         TMessage message = in.readMessageBegin();

        if (message.type != TMessageType.CALL && message.type != TMessageType.ONEWAY) {
            // TODO Apache Guys - Can the server ever get an EXCEPTION or REPLY?
            // TODO Should we check for this here?
            throw new TException("This should not have happened!?");
        }

        String[] headers = message.name.split(SEPARATOR);

        String subject;
        String serviceName;
        String method;

        // Extract the service name
        if (headers.length < 2) {
            throw new TException("Service name not found in message name [" + message.name
                    + "] Did you forget to use a TMultiplexProtocol in your client?");
        } else if (headers.length == 3) {
            subject = headers[0];
            serviceName = headers[1];
            method = headers[2];
        } else {
            subject = DEFAULT_SUBJECT;
            serviceName = headers[0];
            method = headers[1];
        }

        // Create a new TMessage, something that can be consumed by any
        // TProtocol
        TProcessor actualProcessor = SVRPROCESSORMAP.get(serviceName);
        if (actualProcessor == null) {
            throw new TException("Service name not found: " + serviceName
                    + ".  Did you forget to call registerProcessor()?");
        }

        // Create a new TMessage, removing the service name
        TMessage standardMessage = new TMessage(method, message.type, message.seqid);

        // Dispatch processing to the stored processor
        boolean result = false;
        boolean successed = true;
        try {
            result = actualProcessor.process(new StoredMessageProtocol(in, standardMessage), out);
        } catch (TException e) {
            successed = false;
            throw e;
        } finally {
            // 统计
        }

        return result;
    }
    
    protected void registerProcessor(String servicename,String version,TProcessor processor){
        if(processor!=null){
            SVRPROCESSORMAP.put(servicename+"@"+version, processor);
        }
    }
    
    protected void unregisterProcessor(String servicename,String version){
        SVRPROCESSORMAP.remove(servicename+"@"+version);
    }
    
    private static class StoredMessageProtocol extends TProtocolDecorator {

        TMessage messageBegin;

        public StoredMessageProtocol(TProtocol protocol, TMessage messageBegin) {
            super(protocol);
            this.messageBegin = messageBegin;
        }

        @Override
        public TMessage readMessageBegin() throws TException {
            return messageBegin;
        }
    }

}
