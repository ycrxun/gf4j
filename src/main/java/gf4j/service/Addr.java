package gf4j.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Addr {

    private String host;
    private int port;

    @Override
    public String toString() {
        return this.host + ":" + this.port;
    }
}
