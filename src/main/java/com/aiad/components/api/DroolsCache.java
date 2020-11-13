package com.aiad.components.api;

import org.kie.api.runtime.KieSession;

import java.io.IOException;

public interface DroolsCache {
    KieSession getSession(String key);

    void initCache() throws IOException;
}
