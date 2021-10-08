package org.aksw.simba.lsq.jena.plugin;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.Host;
import org.aksw.simba.lsq.model.JoinVertex;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.model.QualifiedFeature;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.aksw.simba.lsq.spinx.model.Bgp;
import org.aksw.simba.lsq.spinx.model.BgpExec;
import org.aksw.simba.lsq.spinx.model.BgpInfo;
import org.aksw.simba.lsq.spinx.model.BgpNode;
import org.aksw.simba.lsq.spinx.model.BgpNodeExec;
import org.aksw.simba.lsq.spinx.model.DirectedHyperEdge;
import org.aksw.simba.lsq.spinx.model.LsqElement;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.spinx.model.SpinVarOrLiteral;
import org.aksw.simba.lsq.spinx.model.TpExec;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.spinx.model.TpInBgpExec;
import org.aksw.simba.lsq.spinx.model.TpInSubBgpExec;
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

//        JenaPluginUtils.scan(LsqQuery.class);
//        JenaPluginUtils.scan(SpinQueryEx.class);

        // Manual registrations because it reduces startup time
        // over classpath scanning
        JenaPluginUtils.registerResourceClasses(
                ElementExec.class,
                ExperimentConfig.class,
                ExperimentRun.class,
                Host.class,
                JoinVertex.class,
                LocalExecution.class,
                LsqQuery.class,
                LsqStructuralFeatures.class,
                QualifiedFeature.class,
                QueryExec.class,
                RemoteExecution.class,

                Bgp.class,
                BgpExec.class,
                BgpInfo.class,
                BgpNode.class,
                BgpNodeExec.class,
                DirectedHyperEdge.class,
                org.aksw.simba.lsq.spinx.model.JoinVertex.class,
                LsqElement.class,
                LsqTriplePattern.class,
                SpinQueryEx.class,
                SpinVarOrLiteral.class,
                TpExec.class,
                TpInBgp.class,
                TpInBgpExec.class,
                TpInSubBgpExec.class
        );

    }
}
