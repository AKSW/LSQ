package org.aksw.simba.lsq.jena.plugin;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.sys.JenaSystem;

public class JenaPluginLsq
    implements JenaSubsystemLifecycle {

    static {
        JenaSystem.init();
    }
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }


    public static void init() {
        //SP.init(BuiltinPersonalities.model);
        // Noop to load spin personalities

        JenaPluginUtils.scan(LsqQuery.class);
        JenaPluginUtils.scan(SpinQueryEx.class);
    }
}
