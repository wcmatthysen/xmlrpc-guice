package org.apache.xmlrpc.webserver;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;

/**
 *
 * @author Wiehann Matthysen
 */
@Singleton
public class GuicedXmlRpcServlet extends XmlRpcServlet {

    private final XmlRpcHandlerMapping xmlRpcHandlerMapping;

    @Inject
    private GuicedXmlRpcServlet(XmlRpcHandlerMapping xmlRpcHandlerMapping) {
        this.xmlRpcHandlerMapping = xmlRpcHandlerMapping;
    }
    
    @Override
    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
        return this.xmlRpcHandlerMapping;
    }
    
    public static Key<GuicedXmlRpcServlet> named(String name) {
        return Key.get(GuicedXmlRpcServlet.class, Names.named(name));
    }
}
