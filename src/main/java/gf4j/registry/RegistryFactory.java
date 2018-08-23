package gf4j.registry;

public class RegistryFactory {

    public static Registry create(Config config) throws Exception {
        switch (config.getProvider()) {
            case Consul:
                return new ConsulRegistry(config);
            default:
                throw new Exception("Unsupported registry provider");
        }
    }
}
