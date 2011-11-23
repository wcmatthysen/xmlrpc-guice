package org.apache.xmlrpc.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;

/**
 *
 * @author Wiehann Matthysen
 */
public class GuicedXmlRpcHandlerMapping implements XmlRpcHandlerMapping {

    private final Injector injector;
    private final Map<String, Method> mapping;

    @Inject
    private GuicedXmlRpcHandlerMapping(Injector injector, Map<String, Method> mappings) {
        this.injector = injector;
        this.mapping = mappings;
    }

    @Override
    public XmlRpcHandler getHandler(String operation) throws XmlRpcNoSuchHandlerException, XmlRpcException {
        if (this.mapping.containsKey(operation)) {
            PropertyHandlerMapping phm = this.injector.getInstance(PropertyHandlerMapping.class);
            phm.setRequestProcessorFactoryFactory(this.injector.getInstance(RequestProcessorFactoryFactory.class));
            Method method = this.mapping.get(operation);
            Class<?> clazz = method.getDeclaringClass();
            phm.addHandler(clazz.getSimpleName(), clazz);
            //AuthenticationHandler handler = this.injector.getInstance(AuthenticationHandler.class);
            //phm.setAuthenticationHandler(handler);
            return phm.getHandler(String.format("%s.%s", clazz.getSimpleName(), method.getName()));
        } else {
            throw new XmlRpcNoSuchHandlerException(String.format("No handler mapping defined for %s", operation)) ;
        }
    }
}
