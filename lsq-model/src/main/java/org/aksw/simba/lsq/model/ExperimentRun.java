package org.aksw.simba.lsq.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ExperimentRun
    extends Resource
{
    @HashId
    @Iri(LSQ.Terms.config)
    ExperimentRun setExec(ExperimentExec exec);
    ExperimentExec getExec();

    // The id should be based on getConfig.getIdentifier() and getTimestamp()
//    @Iri("dct:identifier")
//    @HashId
//    String getIdentifier();
//    ExperimentRun setIdentifier(String id);

    /**
     * Timestamp when this run iteration was started.
     * This is just informational. For identity, the runId is used.
     * @return
     */
    @Iri(LSQ.Terms.atTime)
    XSDDateTime getTimestamp();
    ExperimentRun setTimestamp(XSDDateTime calendar);

    @HashId
    @Iri(LSQ.Terms.runId)
    Integer getRunId();
    ExperimentRun setRunId(Integer runId);

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
        ExperimentExec exec = getExec();
        ExperimentConfig config = exec.getConfig();
        // String id = config.getIdentifier();
        Integer runId = getRunId();
//        Calendar cal = Objects.requireNonNull(getTimestamp(), "no timestamp given").asCalendar();
//        String timestamp = dateFormat.format(cal.getTime());
        //String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // ""

        String result = exec.getStringId(cxt);
        String runSuffix = runId == null ? "" : Integer.toString(runId);
        result += "_run" + runSuffix;

        return result;
//        String result = id;
//        Integer runId = getRunId();
//        if (runId != null) {
//            result += "_run" + runId;
//        }
//
//        result += "_at_" + timestamp;
//        return result;
    }

//    @Iri(LSQ.Strs.endpoint)
//    @IriType
//    String getEndpoint();
//    ExperimentConfig setEndpoint(String url);

//	@Iri(LSQ.Strs.runTimeMs)
//	Resource getDistribution();
//	ExperimentConfig setDistribution(Resource distribution);
}
