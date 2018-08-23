package gf4j.discovery;

import gf4j.examples.helloworld.GreeterGrpc;
import gf4j.examples.helloworld.HelloReply;
import gf4j.examples.helloworld.HelloRequest;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;
import io.opentracing.contrib.ClientTracingInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

public class ConsulNameResolverTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void tx() {
        StringBuilder ads = new StringBuilder();
        ads.append("192.168.31.70:9100").append(",");
        ads.delete(ads.length() - 1, ads.length());
        System.out.println(ads.toString());
    }

    public static void main(String[] args) throws Exception {

        Discovery discovery = DiscoveryFactory
                .create(new Config(Provider.Consul, "192.168.31.70", "8500"));

        Configuration configuration = new Configuration("hello-client")
                .withSampler(new Configuration.SamplerConfiguration()
                        .withType(ProbabilisticSampler.TYPE)
                        .withParam(1))
                .withReporter(new Configuration.ReporterConfiguration()
                        .withSender(new Configuration.SenderConfiguration()
                                .withAgentHost("192.168.31.70").withAgentPort(6831)));

        Tracer tracer = configuration.getTracer();

        ManagedChannel channel = discovery.dial("hello");

        ClientTracingInterceptor tracingInterceptor = new ClientTracingInterceptor(tracer);

        GreeterGrpc.GreeterBlockingStub stub =
                GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, tracingInterceptor));

        Scanner reader = new Scanner(System.in);
        while (reader.hasNext()) {
            String line = reader.nextLine();
            HelloRequest request = HelloRequest.newBuilder().setName(line).build();
            HelloReply helloReply = stub.sayHello(request);
            System.out.println(helloReply.getMessage());
        }

    }
}