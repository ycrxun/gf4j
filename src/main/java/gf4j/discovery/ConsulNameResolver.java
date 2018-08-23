package gf4j.discovery;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ConsulNameResolver extends NameResolver {

    private final String authority;
    private final String host;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private ExecutorService executor;

    ConsulNameResolver(String target) {
        URI nameUri = URI.create("//" + target);
        this.authority = nameUri.getAuthority();
        this.host = target;
        this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
        this.executor = SharedResourceHolder.get(this.executorResource);
    }

    @Override
    public String getServiceAuthority() {
        return this.authority;
    }

    @Override
    public void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "started");
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public void shutdown() {
        if (!this.shutdown) {
            this.shutdown = true;
            if (this.executor != null) {
                this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
            }
        }
    }

    @GuardedBy("this")
    private void resolve() {
        if (!this.resolving && !this.shutdown) {
            this.executor.execute(this.resolutionRunnable);
        }
    }

    public final synchronized void refresh() {
        Preconditions.checkState(this.listener != null, "not started");
        resolve();
    }


    private final Runnable resolutionRunnable = () -> {
        final Listener savedListener;
        synchronized (ConsulNameResolver.this) {
            if (shutdown) {
                return;
            }
            savedListener = listener;
            resolving = true;
        }

        try {
            InetSocketAddress[] addresses =
                    ConsulNameResolver.this.getAllByName(ConsulNameResolver.this.host);
            List<EquivalentAddressGroup> servers = new ArrayList<>(addresses.length);
            for (InetSocketAddress address : addresses) {

                servers.add(new EquivalentAddressGroup(new InetSocketAddress(address.getHostName(), address.getPort()),
                        Attributes.EMPTY));
            }
            savedListener.onAddresses(servers, Attributes.EMPTY);
        } finally {
            synchronized (ConsulNameResolver.this) {
                ConsulNameResolver.this.resolving = false;
            }
        }
    };

    @VisibleForTesting
    InetSocketAddress[] getAllByName(String host) {
        String[] hostArray = host.split(",");
        InetSocketAddress[] inetAddresses = new InetSocketAddress[hostArray.length];

        for (int i = 0; i < inetAddresses.length; ++i) {
            String[] temp = hostArray[i].split(":");
            if (temp.length < 2) {
                throw new RuntimeException("fail to format host for curr host " + hostArray[i]);
            }

            try {
                int e = Integer.parseInt(temp[1].trim());
                inetAddresses[i] = InetSocketAddress.createUnresolved(temp[0].trim(), e);
            } catch (NumberFormatException var7) {
                throw new RuntimeException("fail to format port for curr host " + hostArray[i]);
            }
        }

        return inetAddresses;
    }


}
