/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceCore.Thrift;

import java.util.HashMap;
import java.util.Map;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolDecorator;

/**
 *
 * @author han
 */
public class TRegisterableProcessor implements TProcessor{
    
    private final Map<String,TProcessor> SERVICE_PROCESSOR_MAP = new HashMap<String,TProcessor>();
    
    public void registerProcessor(String serviceName, String version,TProcessor processor) {
        SERVICE_PROCESSOR_MAP.put(serviceName, processor);
    }
    
    public void unregisterProcessor(String serviceName, String version) {

    }


    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
         TMessage message = in.readMessageBegin();

        if (message.type != TMessageType.CALL && message.type != TMessageType.ONEWAY) {
            // TODO Apache Guys - Can the server ever get an EXCEPTION or REPLY?
            // TODO Should we check for this here?
            throw new TException("This should not have happened!?");
        }

        // Extract the service name
        int index = message.name.indexOf(TMultiplexedProtocol.SEPARATOR);
        if (index < 0) {
            throw new TException("Service name not found in message name: " + message.name + ".  Did you " +
                    "forget to use a TMultiplexProtocol in your client?");
        }

        // Create a new TMessage, something that can be consumed by any TProtocol
        String serviceName = message.name.substring(0, index);
        TProcessor actualProcessor = SERVICE_PROCESSOR_MAP.get(serviceName);
        if (actualProcessor == null) {
            throw new TException("Service name not found: " + serviceName + ".  Did you forget " +
                    "to call registerProcessor()?");
        }

        // Create a new TMessage, removing the service name
        TMessage standardMessage = new TMessage(
                message.name.substring(serviceName.length()+TMultiplexedProtocol.SEPARATOR.length()),
                message.type,
                message.seqid
        );

        // Dispatch processing to the stored processor
        return actualProcessor.process(new TRegisterableProcessor.StoredMessageProtocol(in, standardMessage), out);
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
