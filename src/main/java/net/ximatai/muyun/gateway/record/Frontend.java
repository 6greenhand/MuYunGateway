package net.ximatai.muyun.gateway.record;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import net.ximatai.muyun.gateway.routes.IBaseRoute;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public record Frontend(String path, String dir, String notFoundReroute, boolean protect, boolean regex,
                       String comment, List<String> noStore, List<String> whiteList)
        implements IBaseRoute {

    private static final ConcurrentHashMap<String, StaticHandler> STATIC_HANDLER_CACHE = new ConcurrentHashMap<>();
    private static final long MAX_AGE_SECONDS = 7 * 24 * 60 * 60;
    private static final long CACHE_ENTRY_TIMEOUT = 30_000;

    public Handler<RoutingContext> getStaticHandler() {
        return STATIC_HANDLER_CACHE.computeIfAbsent(dir, key -> StaticHandler.create(FileSystemAccess.ROOT, key)
                .setCachingEnabled(false)
                .setMaxAgeSeconds(MAX_AGE_SECONDS)
                .setCacheEntryTimeout(CACHE_ENTRY_TIMEOUT)
                .setFilesReadOnly(false));
    }

    @Override
    public void handle(RoutingContext routingContext) {
        getStaticHandler().handle(routingContext);
    }

    public boolean isNotFoundReroute() {
        return notFoundReroute != null && !notFoundReroute.isBlank();
    }

    public String reroutePath() {
        if (notFoundReroute.startsWith("/")) {
            return burgerPath() + notFoundReroute.substring(1);
        } else {
            return burgerPath() + notFoundReroute;
        }
    }
}
