package org.aksw.simba.largerdfbench.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.simba.hibiscus.hypergraph.HyperGraph.HyperEdge;
import org.aksw.simba.hibiscus.hypergraph.HyperGraph.Vertex;
import org.aksw.simba.lsq.core.ElementVisitorFeature;
import org.aksw.simba.lsq.core.LogRDFizer;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryException;

public class QueryStatistics2 {
    /**
     * Analyze the query for a set of structural features (e.g. use of optional, union, exists, etc...)
     * and attach them to the given resource
     *
     * @param resource The resource on which to attach the features
     * @param query The query object from which to extract the features
     */
    public static void enrichResourceWithQueryFeatures(Resource resource, Query query) {
        Set<Resource> features = ElementVisitorFeature.getFeatures(query);
        for(Resource feature : features) {
            resource.addProperty(LSQ.usesFeature, feature);
        }
    }


    /**
     * linearize any structure into a flat list
     *
     * @param op
     * @param stopMarker
     * @param getChildren
     * @return
     */
    public static <T> Stream<T> linearizePrefix(T op, T stopMarker, Function<? super T, Iterable<? extends T>> getChildren) {
        boolean isIdentity = op == stopMarker || (stopMarker != null && stopMarker.equals(op));
        Stream<T> tmp;
        if(isIdentity) {
            tmp = Stream.empty();
        } else {
            Iterable<?extends T> children = getChildren.apply(op);
            Stream<? extends T> x = StreamSupport.stream(children.spliterator(), false);
            tmp = Stream.concat(x, Stream.of(stopMarker));
        }

        Stream<T> result = Stream.concat(
                Stream.of(op), // Emit parent
                tmp.flatMap(e -> linearizePrefix(e, stopMarker, getChildren)));

        return result;
    }

    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if(op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1)op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2)op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN)op).getElements();
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static int propertyDegree(Resource r, Property p) {
        int result = r.listProperties(p).toList().size();
        return result;
    }


    public static Resource getJoinVertexType(Resource r) {
        int indeg = propertyDegree(r, LSQ.in);
        int outdeg = propertyDegree(r, LSQ.out);

        Resource result;
        if(indeg == 0) {
            result = LSQ.Star;
        } else if(outdeg == 0) {
            result = LSQ.Sink;
        } else if(indeg == 1 && outdeg == 1) {
            result = LSQ.Path;
        } else {
            result = LSQ.Hybrid;
        }

        return result;
    }


    public static <V> HashSet<V> getJoinVertexCount(Model model) {
        Set<Resource> vertices = model.listResourcesWithProperty(RDF.type, LSQ.Vertex).toSet();


        HashSet<Vertex> V = new HashSet<Vertex>();
        for (Vertex vertex:Vertices)
        {
            propertyDegree(vertex)

            long inDeg = vertex.inEdges.size();
            long outDeg = vertex.outEdges.size();
            long degSum = inDeg + outDeg;
            if(degSum>1)
                V.add(vertex);
        }
        return V;
    }



    public static Model createHyperGraphModel(Iterable<Triple> triples) {
        Model result = ModelFactory.createDefaultModel();
        Map<Node, Resource> nodeToResource = new HashMap<Node, Resource>();

        for(Triple t : triples)
        {
            // Get the triple's nodes
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();

            // Create anonymous resources as proxies for the original
            // triple and nodes - needed because RDF literals cannot appear in subject position
            // TODO We treat each triple different from the other even if they happen to be equal - is this desired?
            Resource tx = result.createResource();
            Resource sx = nodeToResource.merge(s, result.createResource(), (x, y) -> x);
            Resource px = nodeToResource.merge(p, result.createResource(), (x, y) -> x);
            Resource ox = nodeToResource.merge(o, result.createResource(), (x, y) -> x);

            result.add(tx, RDF.type, LSQ.Edge);
            result.add(sx, RDF.type, LSQ.Vertex);
            result.add(px, RDF.type, LSQ.Vertex);
            result.add(ox, RDF.type, LSQ.Vertex);


            // Add the orginal nodes as annotations
            Resource ss = result.wrapAsResource(t.getSubject());
            Resource pp = result.wrapAsResource(t.getPredicate());
            RDFNode oo = result.asRDFNode(t.getObject());

            result.add(sx, RDF.subject, ss);
            result.add(px, RDF.predicate, pp);
            result.add(ox, RDF.object, oo);
            result.add(tx, RDFS.label, result.createLiteral("" + t));


            result.add(sx, LSQ.out, tx);
            result.add(px, LSQ.in, tx);
            result.add(ox, LSQ.in, tx);
        }
        return result;
    }



    /**
     * Get the benchmark query features ( e.g resultsize, bgps mean join vertices etc)
     * @param query SPARQL query
     * @return stats Query Features as string
     * @throws MalformedQueryException
     */
    public static String getDirectQueryRelatedRDFizedStats(Query query) throws MalformedQueryException {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        List<BasicPattern> bgps = linearizePrefix(op, null, QueryStatistics2::getSubOps)
                .filter(o -> o != null && o instanceof OpBGP)
                .map(o -> ((OpBGP)o).getPattern())
                .collect(Collectors.toList());


        int totalBgpCount = bgps.size();

        // Find out minimum and maximum size of the bgpgs
        int maxBgpTripleCount = bgps.stream()
            .map(BasicPattern::size)
            .max(Integer::max)
            .map(x -> x)
            .orElse(0);

        int minBgpTripleCount = bgps.stream()
            .map(BasicPattern::size)
            .min(Integer::min)
            .map(x -> x)
            .orElse(0);

        HashSet<Vertex> joinVertices = new HashSet<Vertex>();
        HashSet<Vertex> vertices = new HashSet<Vertex>();



        grandTotalTriplePatterns = grandTotalTriplePatterns + totalTriplePatterns;
        //stats = stats +"Triple Patterns: "+ totalTriplePatterns+"\n";
        stats = stats + " lsqv:triplePatterns "+totalTriplePatterns  +" ; ";
        //System.out.println("Triple Patterns: " +totalTriplePatterns);
        //System.out.println("Total Vertices:"+vertices.size() + " ==> "+vertices);
        //System.out.println("Join Vertices: " +joinVertices.size()+" ==> "+joinVertices);
        //stats = stats+"Join Vertices: " + joinVertices.size()+"\n";
        stats = stats + " lsqv:joinVertices "+joinVertices.size()  +" ; ";
        //System.out.println("Join Vertices to Total Vertices ratio: " +(double)joinVertices.size()/(double)vertices.size());
        double meanJoinVertexDegree = 0;
        //  String joinVertexType = "" ;   // {Star, path, hybrid, sink}
        for(Vertex jv:joinVertices)
        {
            long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
            meanJoinVertexDegree = meanJoinVertexDegree + joinVertexDegree;
        }
        if(joinVertices.size()==0)
            stats = stats + " lsqv:meanJoinVerticesDegree 0 . ";
        //stats = stats +"Mean Join Vertices Degree: 0 \n";
        else
            stats = stats + " lsqv:meanJoinVerticesDegree "+(meanJoinVertexDegree/joinVertices.size())  +" . ";
        //stats = stats +"Mean Join Vertices Degree: "+ +(meanJoinVertexDegree/joinVertices.size())+"\n";
        return stats;
    }

    public static String rdfizeTuples_JoinVertices(String query) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        String stats = "";
        HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
        long totalTriplePatterns = 0;
        HashSet<Vertex> joinVertices = new HashSet<Vertex>();
        HashSet<Vertex> vertices = new HashSet<Vertex>();
        Set<String> predicates = new HashSet<String> ();
        Set<String> subjects = new HashSet<String> ();
        Set<String> objects = new HashSet<String> ();
        for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
        {
            HashSet<Vertex> V = new HashSet<Vertex>();   //--Set of all vertices used in our hypergraph. each subject, predicate and object of a triple pattern is one node until it is repeated
            List<StatementPattern>   stmts =  bgpGrps.get(DNFkey);
            totalTriplePatterns = totalTriplePatterns + stmts.size();
            for (StatementPattern stmt : stmts)
            {
                String sbjVertexLabel, objVertexLabel, predVertexLabel;
                Vertex sbjVertex, predVertex,objVertex ;
                //--------add vertices---
                sbjVertexLabel = getSubjectVertexLabel(stmt);
                subjects.add(sbjVertexLabel);
                predVertexLabel = getPredicateVertexLabel(stmt);
                predicates.add(predVertexLabel);
                objVertexLabel = getObjectVertexLabel(stmt);
                objects.add(objVertexLabel);
                sbjVertex = new Vertex(sbjVertexLabel);
                predVertex = new Vertex(predVertexLabel);
                objVertex = new Vertex(objVertexLabel);
                if(!vertexExist(sbjVertex,V))
                    V.add(sbjVertex);
                if(!vertexExist(predVertex,V))
                    V.add(predVertex);
                if(!vertexExist(objVertex,V))
                    V.add(objVertex);
                //--------add hyperedges
                HyperEdge hEdge = new HyperEdge(sbjVertex,predVertex,objVertex);
                if(!(getVertex(sbjVertexLabel,V)==null))
                    sbjVertex = getVertex(sbjVertexLabel,V);
                if(!(getVertex(predVertexLabel,V)==null))
                    predVertex = getVertex(predVertexLabel,V);
                if(!(getVertex(objVertexLabel,V)==null))
                    objVertex = getVertex(objVertexLabel,V);
                sbjVertex.outEdges.add(hEdge); predVertex.inEdges.add(hEdge); objVertex.inEdges.add(hEdge);
            }
            vertices.addAll(V) ;
            joinVertices.addAll(getJoinVertexCount(V));
            // V.clear();
        }
       if(!subjects.isEmpty())
       {
               stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsSubject ";
           stats = stats + getMentionsTuple(subjects);
       }
       if(!predicates.isEmpty())
       {
               stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsPredicate ";
           stats = stats + getMentionsTuple(predicates);
       }

       if(!objects.isEmpty())
       {
               stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsObject ";
           stats = stats + getMentionsTuple(objects);
       }

       String joinVertexType = "" ;   // {Star, path, hybrid, sink}
        if(!joinVertices.isEmpty()){

        for(Vertex jv:joinVertices)
        {
            String joinVertex = jv.label;
             if(joinVertex.startsWith("http://") || joinVertex.startsWith("ftp://"))
                 joinVertex =  "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
                 else{
                     joinVertex =  "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
                     joinVertex = joinVertex.replace("?", "");
                 }

            stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:joinVertex " + joinVertex + " . \n";
            long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
            joinVertexType =  getJoinVertexType(jv);
            stats = stats + joinVertex + " lsqv:joinVertexDegree " + joinVertexDegree + " ; lsqv:joinVertexType lsqv:" + joinVertexType + " . " ;

            //System.out.println("     " + jv+ " Join Vertex Degree: " + joinVertexDegree + ", Join Vertex Type: " + joinVertexType);
        }
        }
        return stats ;
    }
    public static String getMentionsTuple(Set<String> subjects) {
     String stats = "";
     for (String sbj:subjects)
     {
         if(sbj.startsWith("http://") || sbj.startsWith("ftp://"))
         stats = stats + "<"+sbj+"> , ";
         else
             stats = stats + "\""+sbj+"\" , ";

     }
     stats = stats.substring(0,stats.lastIndexOf(",")-1);
        stats = stats + " . ";
        return stats;
    }


}
