package gf4j.discovery;

import gf4j.registry.Config;
import gf4j.registry.Provider;
import gf4j.registry.Registry;
import gf4j.registry.RegistryFactory;
import gf4j.service.Addr;
import gf4j.service.GreeterImpl;
import gf4j.service.Service;

import java.io.IOException;

public class Hello2 {

    public static void main(String[] args) throws IOException {
        gf4j.registry.Config config = new Config(Provider
                .Consul, "192.168.31.70", "8500");
        Registry registry = null;
        try {
            registry = RegistryFactory.create(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Service service = new Service("hello");
        service.setServiceAddr(new Addr("0.0.0.0", 9300));
        service.setPrometheusAddr(new Addr("0.0.0.0", 9200));
        service.setRegistry(registry);
        service.setGRPCImpl(new GreeterImpl());

        service.run();
    }
}
