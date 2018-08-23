package gf4j.discovery;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Slf4j
public class ConsulDiscovery implements Discovery {

    private final HealthClient client;

    public ConsulDiscovery(Config config) {
        String addr = config.getHost() + ":" + config.getPort();
        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromString(addr))
                .build();
        this.client = consul.healthClient();
    }

    @Override
    public ManagedChannel dial(String serviceName) {
        List<ServiceHealth> healths = this.client.getHealthyServiceInstances(serviceName).getResponse();
        StringBuilder address = new StringBuilder();
        healths.forEach(serviceHealth -> address.append(serviceHealth.getService().getAddress()).append(":").append(serviceHealth.getService().getPort()).append(","));
        address.delete(address.length() - 1, address.length());
        return ManagedChannelBuilder
                .forTarget("consul://" + address.toString())
                .nameResolverFactory(new ConsulNameResolverProvider())
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext()
                .build();
    }
}
