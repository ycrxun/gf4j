package gf4j.service;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import org.junit.Test;

public class TxDelAtConsul {

    @Test
    public void tx(){
        Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromString
                ("192.168.31.70:8500")).build();
        AgentClient client = consul.agentClient();
        client.deregister("add");
    }
}
