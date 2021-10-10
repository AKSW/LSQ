package org.aksw.simba.lsq.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Provenance Ontology.
 *
 * There is probably some class like this one out there as a maven dep for provo.
 * Once we find it, we can make this class obsolete.
 *
 * @author raven
 *
 */
public class PROV {
    public static final String NS = "http://www.w3.org/ns/prov#";

    public static Property property(String local) {
        return ResourceFactory.createProperty(NS + local);
    }

    public static final Property hadPrimarySource = property("hadPrimarySource");
    public static final Property atTime = property("atTime");
    public static final Property startedAtTime = property("startedAtTime");
    public static final Property endedAtTime = property("endedAtTime");
    public static final Property wasGeneratedBy = property("wasGeneratedBy");
    public static final Property wasAssociatedWith = property("wasAssociatedWith");

}
