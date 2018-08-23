package gf4j.service;

import gf4j.registry.Config;
import gf4j.registry.Provider;
import gf4j.registry.Registry;
import gf4j.registry.RegistryFactory;

import java.io.IOException;

public class ServiceTestViaMain {

    public static void main(String[] args) throws IOException {
        Config config = new Config(Provider
                .Consul, "192.168.31.70", "8500");
        Registry registry = null;
        try {
            registry = RegistryFactory.create(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Service service = new Service("hello");
        service.setServiceAddr(new Addr("0.0.0.0", 9100));
        service.setRegistry(registry);
        service.setGRPCImpl(new GreeterImpl());

        service.run();
    }
}
