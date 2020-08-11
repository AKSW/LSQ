package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JoinVertex
    extends Resource
{
    Set<BgpNodeExec> getJoinVertexExecs();
}
