package gf4j.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ServiceTests {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNewService() throws IOException {
        Service service = new Service("test");
        service.setGRPCImpl(new GreeterImpl());
        service.run();
    }
}

