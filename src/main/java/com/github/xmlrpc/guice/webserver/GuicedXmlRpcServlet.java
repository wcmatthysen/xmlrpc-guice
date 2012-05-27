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
package com.github.xmlrpc.guice.webserver;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

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
