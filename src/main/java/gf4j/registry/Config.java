package gf4j.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Config {
    private Provider provider;
    private String host;
    private String port;
}

