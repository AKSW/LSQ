package org.aksw.simba.lsq.enricher.core;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.simba.lsq.core.util.Skolemize;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LsqEnricherShell
    implements SerializableSupplier<Function<Resource, Resource>>
    // implements LsqEnricherFactory
{
    private static final Logger logger = LoggerFactory.getLogger(LsqEnricherShell.class);

    private static final long serialVersionUID = 1L;

    protected String baseIri;
    protected List<String> enricherNames;
    protected Supplier<LsqEnricherRegistry> registrySupplier;

    public LsqEnricherShell(String baseIri, List<String> enricherNames, Supplier<LsqEnricherRegistry> registrySupplier) {
        super();
        this.baseIri = baseIri;
        this.enricherNames = enricherNames;
        this.registrySupplier = registrySupplier;
    }

    /** Wrap an enricher to log any exception*/
    public static <T> Function<LsqQuery, T> safeEnricher(Function<LsqQuery, T> enricher) {
        return in -> {
            T r = null;
            try {
                r = enricher.apply(in);
            } catch (Exception e) {
                // ARQ2SPIN (2.0.0) raises a classcast exception for queries making
                // use of literals in subject position
                logger.warn(String.format("Enrichment of %s failed", in.getText()), e);
            }
            return r;
        };
    }

    @Override
    public Function<Resource, Resource> get() {
        LsqEnricherRegistry registry = registrySupplier.get();

        return in -> {
            // TODO Implement support for blacklisting

             LsqQuery q = in.as(LsqQuery.class);
             // TODO Parse query here only once
             // Store it in a context object that gets passed to the enrichers?

             if (q.getParseError() == null) {
                 for (String name : enricherNames) {
                     LsqEnricherFactory f = registry.getOrThrow(name);
                     LsqEnricher enricher = f.get();
                     safeEnricher(enricher).apply(q);
                 }

                 // TODO Given enrichers a name
                 // TODO Track failed enrichments in the output? qualify error with enricher name?
                 // TODO Create a registry for enrichers
//                     safeEnricher(LsqEnrichments::enrichWithFullSpinModelCore).apply(q);
//                     safeEnricher(LsqEnrichments::enrichWithStaticAnalysis).apply(q);
//
//                     safeEnricher(LsqEnrichments::enrichWithBBox).apply(q);
             }

             // TODO createLsqRdfFlow already performs skolemize; duplicated effort
             Resource out = Skolemize.skolemize(in, baseIri, LsqQuery.class, null);
             return out.as(LsqQuery.class);
        };
    }
}
