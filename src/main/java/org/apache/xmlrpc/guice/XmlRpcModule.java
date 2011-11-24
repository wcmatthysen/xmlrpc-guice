package org.apache.xmlrpc.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import java.lang.reflect.Method;
import org.apache.xmlrpc.server.GuicedRequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.GuicedXmlRpcHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.GuicedXmlRpcServlet;

/**
 *
 * @author Wiehann Matthysen
 */
public abstract class XmlRpcModule extends PrivateModule implements LinkedHandlerMappingBuilder {
    
    private MapBinder<String, Method> binder;

    @Override
    protected final void configure() {
        try {
            this.binder = MapBinder.newMapBinder(binder(), String.class, Method.class);
            bind(RequestProcessorFactoryFactory.class).to(GuicedRequestProcessorFactoryFactory.class);
            bind(XmlRpcHandlerMapping.class).to(GuicedXmlRpcHandlerMapping.class);
            configureHandlers();
        } catch (NoSuchMethodException exception) {
            
        }
    }
    
    protected final void exposeAs(String named) {
        bind(GuicedXmlRpcServlet.class).annotatedWith(Names.named(named)).to(GuicedXmlRpcServlet.class);
        expose(GuicedXmlRpcServlet.class).annotatedWith(Names.named(named));
    }

    protected abstract void configureHandlers() throws NoSuchMethodException;

    @Override
    public final HandlerMappingBuilderStub map(final String methodName) {
        return new HandlerMappingBuilderStub() {
            @Override
            public void to(Method method) {
                binder.addBinding(methodName).toInstance(method);
            }
        };
    }
}
