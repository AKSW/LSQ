package org.aksw.simba.lsq.core.util;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.arq.ARQ2SPIN;

public class SpinUtils {
    public static org.topbraid.spin.model.Query createSpinModel(
            Query query,
            Model tgtModel
            ) {

        return createSpinModel(query, tgtModel.createResource());
    }
    public static org.topbraid.spin.model.Query createSpinModel(
            Query query,
            Resource spinRes
//            BiFunction<? super Resource, String, String> lsqResToIri
            ) {
        query = query.cloneQuery();
        query.getGraphURIs().clear();

        // queryNo++;
        // .. generate the spin model ...
        //Model spinModel = queryRes.getModel();
        // Model spinModel = ModelFactory.createDefaultModel();
        Model spinModel = spinRes == null ? null : spinRes.getModel();
        if(spinModel == null) {
            spinModel = ModelFactory.createDefaultModel();
        }
        ARQ2SPIN arq2spin = new ARQ2SPIN(spinModel);
        org.topbraid.spin.model.Query tmpSpinRes = arq2spin.createQuery(query, spinRes == null ? null : spinRes.getURI());

        // ... and rename the blank node of the query
        // ResourceUtils.renameResource(tmpSpinRes, spinRes.getURI());

        // ... and skolemize the rest
        //Skolemize.skolemize(spinRes);

        return tmpSpinRes;

//      System.out.println("TEST {");
//      SpinUtils.indexTriplePatterns2(tmpSpinRes.getModel()).forEach(System.out::println);
//      SpinUtils.itp(tmpSpinRes).forEach(System.out::println);
//      System.out.println("}");
//tmpSpinRes.as(org.topbraid.spin.model.Query.class).getWhereElements().forEach(e -> {
//System.out.println("XXElement: " + e.asResource().getId() + ": " + e);
//});


    }

}
