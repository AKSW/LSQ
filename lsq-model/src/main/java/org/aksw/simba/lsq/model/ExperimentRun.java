package org.aksw.simba.lsq.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ExperimentRun
    extends Resource
{
    @HashId
    @Iri(LSQ.Terms.config)
    ExperimentRun setConfig(ExperimentConfig dataRef);
    ExperimentConfig getConfig();


    // The id should be based on getConfig.getIdentifier() and getTimestamp()
    @Iri("dct:identifier")
    @HashId
    String getIdentifier();
    ExperimentRun setIdentifier(String id);


    @HashId
    @Iri(LSQ.Terms.atTime)
    XSDDateTime getTimestamp();
    ExperimentRun setTimestamp(XSDDateTime calendar);


    /**
     * The identifier should be composed of getConfig().getIdentifier() and getTimestamp()
     *
     * @return
     */
//    @HashId
//    @Iri("dct:identifier")
//    String getIdentifier();
//    ExperimentRun setIdentifier(String id);


    // 17/Apr/2011:06:47:47 +0200
    public static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");

    @StringId
    default String getStringId(HashIdCxt cxt) {
        String id = getConfig().getIdentifier();
        Calendar cal = Objects.requireNonNull(getTimestamp(), "no timestamp given").asCalendar();
        String timestamp = dateFormat.format(cal.getTime());
        //String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // ""
        String result = id + "_at_" + timestamp;
        return result;
    }

//    @Iri(LSQ.Strs.endpoint)
//    @IriType
//    String getEndpoint();
//    ExperimentConfig setEndpoint(String url);

//	@Iri(LSQ.Strs.runTimeMs)
//	Resource getDistribution();
//	ExperimentConfig setDistribution(Resource distribution);
}
