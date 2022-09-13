package org.aksw.simba.lsq.spinx.model;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.spinrdf.vocabulary.SP;

@ResourceView
@HashId
public interface SpinVarOrLiteral
    extends Resource
{
    @IriNs(SP.NS)
    @HashId
    String getVarName();
    SpinVarOrLiteral setvarName(String name);

    @Override
    default <T extends RDFNode> boolean canAs(Class<T> view) {
        String varName = getVarName();
        boolean result = varName != null;
        return result;
    }
}
