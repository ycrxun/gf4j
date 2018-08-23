package gf4j.discovery;

import gf4j.registry.Config;
import gf4j.registry.Provider;
import gf4j.registry.Registry;
import gf4j.registry.RegistryFactory;
import gf4j.service.Addr;
import gf4j.service.GreeterImpl;
import gf4j.service.Service;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;

import java.io.IOException;

public class Hello1 {

    public static void main(String[] args) throws IOException {
        gf4j.registry.Config config = new Config(Provider
                .Consul, "192.168.31.70", "8500");
        Registry registry = null;
        try {
            registry = RegistryFactory.create(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Configuration configuration = new Configuration("hello")
                .withSampler(new Configuration.SamplerConfiguration()
                        .withType(ProbabilisticSampler.TYPE)
                        .withParam(1))
                .withReporter(new Configuration.ReporterConfiguration()
                        .withSender(new Configuration.SenderConfiguration()
                                .withAgentHost("192.168.31.70").withAgentPort(6831)));

        Tracer tracer = configuration.getTracer();

        Service service = new Service("hello");
        service.setServiceAddr(new Addr("0.0.0.0", 9100));
        service.setRegistry(registry);
        service.setTracer(tracer);
        service.setGRPCImpl(new GreeterImpl());

        service.run();
    }
}
