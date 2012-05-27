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
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

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
