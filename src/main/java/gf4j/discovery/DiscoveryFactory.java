package gf4j.discovery;

public class DiscoveryFactory {

    public static Discovery create(Config config) throws Exception {
        switch (config.getProvider()) {
            case Consul:
                return new ConsulDiscovery(config);
            default:
                throw new Exception("Unsupported discovery provider");
        }
    }
}
