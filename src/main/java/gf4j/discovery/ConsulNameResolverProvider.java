package gf4j.discovery;

import com.google.common.base.Preconditions;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import javax.annotation.Nullable;
import java.net.URI;

public class ConsulNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "consul";

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        if (targetUri.getScheme().equals(SCHEME)) {
            String targetPath = Preconditions.checkNotNull(targetUri.getAuthority(), "authority is not null");
            return new ConsulNameResolver(targetPath);
        } else {
            throw new RuntimeException("the targetUri scheme must be server");
        }
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }
}
