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

import com.github.xmlrpc.guice.XmlRpcModule;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;

import net.sf.sojo.interchange.xmlrpc.XmlRpcSerializer;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Wiehann Matthysen
 */
public class GuicedXmlRpcServletTest extends ServletModule {
    
    private ServletTester tester;
    private HttpTester request;
    private HttpTester response;

    public static class TestHandler01 {
        
        private final String injectedString;
        
        @Inject
        private TestHandler01(@Named("string") String injectedString) {
            this.injectedString = injectedString;
        }
        
        public String testMethod01(Map<String, Object> parameters) {
            return parameters.get("string") + " " + this.injectedString;
        }
    }
    
    public static class TestHandler02 {
        
        private final int injectedInteger;
        
        @Inject
        private TestHandler02(@Named("integer") int injectedInteger) {
            this.injectedInteger = injectedInteger;
        }
        
        public int testMethod02(Map<String, Object> parameters) {
            return (Integer)parameters.get("integer") - this.injectedInteger;
        }
    }
    
    public static class TestHandler03 {
        
        private final double injectedDouble;
        private final Date injectedDate;
        
        @Inject
        private TestHandler03(
                @Named("double") double injectedDouble,
                @Named("milliseconds") long injectedMilliseconds) {
            this.injectedDouble = injectedDouble;
            this.injectedDate = new Date(injectedMilliseconds);
        }
        
        public double testMethod01(Map<String, Object> parameters) {
            return this.injectedDouble - (Double)parameters.get("double");
        }
        
        public boolean testMethod02(Map<String, Object> parameters) {
            return ((Date)parameters.get("date")).after(this.injectedDate);
        }
    }

    @Override
    protected void configureServlets() {
        bindConstant().annotatedWith(Names.named("string")).to("injected_string");
        bindConstant().annotatedWith(Names.named("integer")).to(12345);
        bindConstant().annotatedWith(Names.named("double")).to(1.2345);
        bindConstant().annotatedWith(Names.named("milliseconds")).to(1000L);
        
        install(new XmlRpcModule() {
            @Override
            public void configureHandlers() throws NoSuchMethodException {
                map("first_method").to(TestHandler01.class.getMethod("testMethod01", Map.class));
                map("second_method").to(TestHandler02.class.getMethod("testMethod02", Map.class));
                exposeAs("test_context_01");
            }
        });
        serve("/test_context_01").with(GuicedXmlRpcServlet.named("test_context_01"));
        
        install(new XmlRpcModule() {
            @Override
            protected void configureHandlers() throws NoSuchMethodException {
                map("first_method").to(TestHandler02.class.getMethod("testMethod02", Map.class));
                map("second_method").to(TestHandler03.class.getMethod("testMethod01", Map.class));
                exposeAs("test_context_02");
            }
        });
        serve("/test_context_02").with(GuicedXmlRpcServlet.named("test_context_02"));
        
        install(new XmlRpcModule() {
            @Override
            protected void configureHandlers() throws NoSuchMethodException {
                map("first_method").to(TestHandler03.class.getMethod("testMethod01", Map.class));
                map("second_method").to(TestHandler03.class.getMethod("testMethod02", Map.class));
                exposeAs("test_context_03");
            }
        });
        serve("/test_context_03").with(GuicedXmlRpcServlet.named("test_context_03"));
    }

    @Before
    public void setup() throws Exception {
        this.tester = new ServletTester();
        this.tester.setContextPath("/");
        this.tester.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        this.tester.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return Guice.createInjector(GuicedXmlRpcServletTest.this);
            }
        });
        this.tester.addServlet(DefaultServlet.class, "/");
        this.tester.start();
        this.request = new HttpTester();
        this.response = new HttpTester();
        this.request.setVersion("HTTP/1.0");
    }
    
    @After
    public void teardown() throws Exception {
        this.tester.stop();
    }

    @Test
    public void test_that_handler_01_test_method_01_returns_correct_result() throws Exception {
        // Build up XML-RPC request.
        XmlRpcSerializer serializer = new XmlRpcSerializer();
        Map<String, String> parameters = ImmutableMap.of("string", "parameter_string");
        String content = serializer.serializeXmlRpcRequest("first_method", parameters);
        
        // Send request to Servlet (served by Jetty).
        this.request.setMethod("POST");
        this.request.setURI("/test_context_01");
        this.request.setContent(content);
        this.response.parse(this.tester.getResponses(this.request.generate()));
        
        // Test that response is correct (should contain concatenation of injected-string and parameter-string).
        String result = (String)serializer.deserialize(this.response.getContent(), String.class);
        assertThat(result, is(equalTo("parameter_string injected_string")));
    }
    
    @Test
    public void test_that_handler_02_test_method_02_returns_correct_result() throws Exception {
        // Build up XML-RPC request.
        XmlRpcSerializer serializer = new XmlRpcSerializer();
        Map<String, Integer> parameters = ImmutableMap.of("integer", 12346);
        String content = serializer.serializeXmlRpcRequest("second_method", parameters);
        
        // Send request to Servlet (served by Jetty).
        this.request.setMethod("POST");
        this.request.setURI("/test_context_01");
        this.request.setContent(content);
        this.response.parse(this.tester.getResponses(this.request.generate()));
        
        // Test that response is correct (should contain result of subtracting parameter-integer from injected-integer).
        int returnInteger = (Integer)serializer.deserialize(this.response.getContent(), Integer.class);
        assertThat(returnInteger, is(1));
        
        content = serializer.serializeXmlRpcRequest("first_method", parameters);
        this.request.setURI("/test_context_02");
        this.request.setContent(content);
        
        // Test that response is correct (should contain result of subtracting parameter-integer from injected-integer).
        returnInteger = (Integer)serializer.deserialize(this.response.getContent(), Integer.class);
        assertThat(returnInteger, is(1));
    }
    
    @Test
    public void test_that_handler_03_test_method_01_returns_correct_result() throws Exception {
        // Build up XML-RPC request.
        XmlRpcSerializer serializer = new XmlRpcSerializer();
        Map<String, Double> parameters = ImmutableMap.of("double", 0.23);
        String content = serializer.serializeXmlRpcRequest("second_method", parameters);
        
        // Send request to Servlet (served by Jetty).
        this.request.setMethod("POST");
        this.request.setURI("/test_context_02");
        this.request.setContent(content);
        this.response.parse(this.tester.getResponses(this.request.generate()));
        
        // Test that response is correct (should contain result of subtracting parameter-double from injected-double).
        double returnDouble = (Double)serializer.deserialize(this.response.getContent(), Double.class);
        assertThat(returnDouble, is(1.0045));
        
        content = serializer.serializeXmlRpcRequest("first_method", parameters);
        this.request.setURI("/test_context_03");
        this.request.setContent(content);
        
        returnDouble = (Double)serializer.deserialize(this.response.getContent(), Double.class);
        assertThat(returnDouble, is(1.0045));
    }
    
    @Test
    public void test_that_handler_03_test_method_02_returns_correct_result() throws Exception {
        // Build up XML-RPC request.
        XmlRpcSerializer serializer = new XmlRpcSerializer();
        Map<String, Date> parameters = ImmutableMap.of("date", new Date());
        String content = serializer.serializeXmlRpcRequest("second_method", parameters);
        
        // Send request to Servlet (served by Jetty).
        this.request.setMethod("POST");
        this.request.setURI("/test_context_03");
        this.request.setContent(content);
        this.response.parse(this.tester.getResponses(this.request.generate()));
        
        // Test that response is correct (parameter-date should have occurred after injected-date).
        boolean returnBoolean = (Boolean)serializer.deserialize(this.response.getContent(), Boolean.class);
        assertThat(returnBoolean, is(true));
    }
}
