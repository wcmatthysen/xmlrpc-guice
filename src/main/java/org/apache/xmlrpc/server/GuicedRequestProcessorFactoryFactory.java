package org.apache.xmlrpc.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;

/**
 *
 * @author Wiehann Matthysen
 */
public class GuicedRequestProcessorFactoryFactory implements RequestProcessorFactoryFactory {

    private final Injector injector;
    
    @Inject
    private GuicedRequestProcessorFactoryFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public RequestProcessorFactory getRequestProcessorFactory(final Class clazz) throws XmlRpcException {
        return new RequestProcessorFactory() {
            @Override
            public Object getRequestProcessor(XmlRpcRequest request) throws XmlRpcException {
                return injector.getInstance(clazz);
            }
        };
    }
}
