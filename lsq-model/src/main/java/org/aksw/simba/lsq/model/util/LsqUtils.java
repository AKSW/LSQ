package org.aksw.simba.lsq.model.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;

public class LsqUtils {
    public static String createExperimentId(String datasetLabel) {
        return createExperimentId(datasetLabel, Instant.now());
    }

    public static String createExperimentId(String datasetLabel, Instant now) {
        // experimentId = distributionId + "_" + timestamp
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
        // Calendar nowCal = GregorianCalendar.from(zdt);
        //String timestamp = now.toString();
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);

        String expId = "xc-" + datasetLabel + "_" + timestamp;
        return expId;
    }

    /**
     * Replaces any non-word character (with the exception of '-') with an underescore.
     * Only suitable for ASCII names; otherwise the result will be mostly composed of underscores!
     *
     * @param inputName
     * @return
     */
    public static String sanitizeFilename(String inputName) {
        String result = inputName.replaceAll("[^\\w-]", "_");
        return result;
    }

    public static ExperimentExec createExperimentExec(Model outModel, ExperimentConfig expConfig, Instant benchmarkRunStartTimestamp) {
      // Create an instance of the config at the current time
      ZonedDateTime zdt = ZonedDateTime.ofInstant(benchmarkRunStartTimestamp, ZoneId.systemDefault());
      Calendar cal = GregorianCalendar.from(zdt);
      XSDDateTime xsddt = new XSDDateTime(cal);
      String timestampStr = DateTimeFormatter.ISO_INSTANT.format(zdt);

      String configId = expConfig.getIdentifier();
      String runId = configId + "_" + timestampStr;
      String runIri = expConfig.getBaseIri() + runId;

      ExperimentExec expRun = outModel
          .createResource(runIri)
          .as(ExperimentExec.class)
          .setConfig(expConfig)
          .setTimestamp(xsddt)
          ;

      return expRun;
    }

    public static ExperimentRun createExperimentRun(Model outModel, ExperimentExec expExec, int runId) {
        ExperimentRun expRun = outModel.createResource().as(ExperimentRun.class)
            .setExec(expExec)
            .setRunId(runId)
            // .setTimestamp(null); // xsddt
            ;
        return expRun;
    }
}
