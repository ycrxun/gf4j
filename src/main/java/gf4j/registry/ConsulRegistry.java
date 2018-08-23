package gf4j.registry;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Slf4j
public class ConsulRegistry implements Registry {
    private AgentClient client;

    public ConsulRegistry(Config config) {
        String addr = config.getHost() + ":" + config.getPort();
        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromString(addr))
                .build();
        this.client = consul.agentClient();
    }

    @Override
    public void register(String id, String name, int port, String... tags) {
        String ip = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get addr failed.");
        }
        Registration registration = ImmutableRegistration.builder()
                .id(id)
                .name(name)
                .port(port)
                .address(ip)
                .addTags(tags)
                .build();
        this.client.register(registration);
        log.info("Regsitered service {} at consul.", name);
    }

    @Override
    public void deregister(String name) {
        this.client.deregister(name);
        log.info("Deregistered service {} at consul.", name);
    }
}
