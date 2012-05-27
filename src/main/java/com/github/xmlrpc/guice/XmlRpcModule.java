/**
 * XMLRPC-Guice Library
 * Copyright (C) 2011 - 2012
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package com.github.xmlrpc.guice;

import com.github.xmlrpc.guice.server.GuicedRequestProcessorFactoryFactory;
import com.github.xmlrpc.guice.server.GuicedXmlRpcHandlerMapping;
import com.github.xmlrpc.guice.webserver.GuicedXmlRpcServlet;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import java.lang.reflect.Method;

import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;

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
