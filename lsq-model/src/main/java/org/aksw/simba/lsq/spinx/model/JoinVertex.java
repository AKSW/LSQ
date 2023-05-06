package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JoinVertex
    extends Resource
{
    Set<BgpNodeExec> getJoinVertexExecs();
}
