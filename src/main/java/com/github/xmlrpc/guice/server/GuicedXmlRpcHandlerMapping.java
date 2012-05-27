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
package com.github.xmlrpc.guice.server;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;

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
