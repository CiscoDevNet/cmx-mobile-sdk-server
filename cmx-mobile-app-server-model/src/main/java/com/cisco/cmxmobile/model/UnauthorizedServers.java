package com.cisco.cmxmobile.model;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class UnauthorizedServers {

    private final LoadingCache<String, UnauthorizedServer> cache;

    private static final long MAX_CACHE_SIZE = 25;

    public UnauthorizedServers()
    {
        //Build The Cache
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .build(new CacheLoader<String, UnauthorizedServer>() {
                    @Override
                    public UnauthorizedServer load(String key) throws Exception {                       
                        return null;
                    }
                });
    }
    
    public void resetAll() {
        cache.invalidateAll();
    }
    
    public ConcurrentMap<String, UnauthorizedServer> getAllUnauthorizedServers() {
        return cache.asMap();
    }

    public UnauthorizedServer getUnauthorizedServer(final String serverAddress, final String serverId) throws ExecutionException {
        return cache.get(serverAddress, new Callable<UnauthorizedServer>() {
            @Override
            public UnauthorizedServer call() throws Exception {
                UnauthorizedServer server = new UnauthorizedServer();
                server.setServerAddress(serverAddress);
                server.setServerId(serverId);
                return server;
            }
        });
    }
}
