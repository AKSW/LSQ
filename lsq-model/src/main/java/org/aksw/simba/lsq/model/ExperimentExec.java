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

/** A benchmark exec is linked to by 1 or more runs */
@ResourceView
public interface ExperimentExec extends Resource {
    @HashId
    @Iri(LSQ.Terms.benchmarkConfig)
    ExperimentConfig getConfig();
    ExperimentExec setConfig(Resource experimentConfig);

//    @HashId
//    @Iri(LSQ.Terms.runId)
//    Integer getRunId();
//    ExperimentRun setRunId(Integer runId);

    // Optionally track start and end time of a run
    @HashId
    @Iri(LSQ.Terms.atTime)
    XSDDateTime getTimestamp();
    ExperimentExec setTimestamp(XSDDateTime calendar);

    // 17/Apr/2011:06:47:47 +0200
    public static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");

    default String getIdentifier() {
        ExperimentConfig config = getConfig();
        String configId = config.getIdentifier();

        XSDDateTime dt = getTimestamp();
        Calendar cal = dt.asCalendar();

        String timestampStr = dateFormat.format(cal.getTime());
        String runId = configId + "_" + timestampStr;
        return runId;
    }

    @StringId
    default String getStringId(HashIdCxt cxt) {
        ExperimentConfig config = getConfig();
        String id = config.getIdentifier();
        Calendar cal = Objects.requireNonNull(getTimestamp(), "no timestamp given").asCalendar();
        String timestamp = dateFormat.format(cal.getTime());
        // String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); //
        // ""

        String result = id;
//        Integer runId = getRunId();
//        if (runId != null) {
//            result += "_run" + runId;
//        }

        result += "_at_" + timestamp;
        return result;
    }
}
