package org.aksw.simba.lsq.enricher.core;import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/** A registry for {@link LsqEnricherFactory} instances. */
public class LsqEnricherRegistry {

    // Synchronized and insert-order-retaining
    protected Map<String, LsqEnricherFactory> registry = new ConcurrentSkipListMap<>();

    private static LsqEnricherRegistry INSTANCE = null;

    /** Return the global instance */
    public static LsqEnricherRegistry get() {
        if (INSTANCE == null) {
            synchronized (LsqEnricherRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = LsqEnricherRegistry.createDefault();
                }
            }
        }
        return INSTANCE;
    }

    public void register(String name, LsqEnricherFactory factory) {
        registry.put(name, factory);
    }

    public LsqEnricherFactory get(String name) {
        return registry.get(name);
    }

    public LsqEnricherFactory getOrThrow(String name) {
        LsqEnricherFactory result = get(name);
        if (result == null) {
            throw new IllegalArgumentException("No enricher factory found for " + name);
        }
        return result;
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public static LsqEnricherRegistry createDefault() {
        LsqEnricherRegistry result = new LsqEnricherRegistry();
        initDefaults(result);
        return result;
    }

    public static LsqEnricherRegistry initDefaults(LsqEnricherRegistry registry) {
        registry.register("spin", () -> LsqEnrichments::enrichWithFullSpinModelCore);
        registry.register("static", () -> LsqEnrichments::enrichWithStaticAnalysis);
        registry.register("bbox", () -> LsqEnrichments::enrichWithBBox);
        return registry;
    }
}
