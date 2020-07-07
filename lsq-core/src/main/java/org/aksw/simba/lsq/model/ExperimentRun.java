package org.aksw.simba.lsq.model;

import java.util.Calendar;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ExperimentRun
    extends Resource
{
    @Iri(LSQ.Strs.config)
    ExperimentRun setConfig(ExperimentConfig dataRef);
    ExperimentConfig getConfig();

    @Iri(LSQ.Strs.atTime)
    Calendar getTimestamp();
    RemoteExecution setTimestamp(Calendar calendar);


    /**
     * The identifier should be composed of getConfig().getIdentifier() and getTimestamp()
     *
     * @return
     */
    @HashId
    @Iri("dct:identifier")
    String getIdentifier();
    ExperimentRun setIdentifier(String id);

//    @HashId
//    default String getHashId() {
//        String id = getIdentifier();
//        Calendar cal = getTimestamp();
//        String timestamp = cal.toString(); // TODO Properly format
//        String result = id + "_at_" + timestamp;
//        return result;
//    }

//    @Iri(LSQ.Strs.endpoint)
//    @IriType
//    String getEndpoint();
//    ExperimentConfig setEndpoint(String url);

//	@Iri(LSQ.Strs.runTimeMs)
//	Resource getDistribution();
//	ExperimentConfig setDistribution(Resource distribution);
}
