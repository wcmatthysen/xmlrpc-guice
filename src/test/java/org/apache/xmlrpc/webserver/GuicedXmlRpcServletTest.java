package org.apache.xmlrpc.webserver;

import com.google.inject.name.Named;
import java.util.EnumSet;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import java.util.Map;
import javax.servlet.DispatcherType;
import net.sf.sojo.interchange.xmlrpc.XmlRpcSerializer;
import org.apache.xmlrpc.guice.XmlRpcModule;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

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
        
        @Inject
        private TestHandler03(@Named("double") double injectedDouble) {
            this.injectedDouble = injectedDouble;
        }
        
        public double testMethod03(Map<String, Object> parameters) {
            return (Double)parameters.get("double") - this.injectedDouble;
        }
    }

    @Override
    protected void configureServlets() {
        bindConstant().annotatedWith(Names.named("string")).to("injected_string");
        bindConstant().annotatedWith(Names.named("integer")).to(12345);
        bindConstant().annotatedWith(Names.named("double")).to(1.2345);
        
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
                map("second_method").to(TestHandler03.class.getMethod("testMethod03", Map.class));
                exposeAs("test_context_02");
            }
        });
        serve("/test_context_02").with(GuicedXmlRpcServlet.named("test_context_02"));
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
    public void test_that_handler_method_gets_called_from_xmlrpc_servlet() throws Exception {
        // Build up XML-RPC request.
        XmlRpcSerializer serializer = new XmlRpcSerializer();
        Map<String, Object> parameters = ImmutableMap.<String, Object>of("string", "parameter_string");
        String content = serializer.serializeXmlRpcRequest("first_method", parameters);
        
        // Send request to Servlet (served by Jetty).
        this.request.setMethod("POST");
        this.request.setURI("/test_context_01");
        this.request.setContent(content);
        this.response.parse(this.tester.getResponses(this.request.generate()));
        
        // Test if response is correct (should contain concatenation of injected-string and parameter-string).
        String returnValue = (String)serializer.deserialize(this.response.getContent(), String.class);
        assertThat(returnValue, is(equalTo("parameter_string injected_string")));
    }
}
