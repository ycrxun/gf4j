package gf4j.registry;

public interface Registry {

    void register(String id, String name, int port, String... tags) throws
            Exception;

    void deregister(String name);
}
