package org.aksw.simba.lsq.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * LSQ vocabulary
 *
 * @author Claus Stadler
 *
 */
public class LSQ {
    public static final String ns = "http://lsq.aksw.org/vocab#";


    public static class Strs {
        public static final String Query = ns + "Query";

        public static final String text = ns + "text";
        public static final String hash = ns + "hash";
        public static final String resultSize = ns + "resultSize";
        public static final String isDistinct = ns + "isDistinct";
//        public static final String distinctResultSize = ns + "distinctResultSize";

        public static final String hasStructuralFeatures = ns + "hasStructuralFeatures";
        public static final String hasSpin = ns + "hasSpin";
        public static final String hasTP = ns + "hasTP";
        public static final String hasTpInBgp = ns + "hasTpInBgp";
        public static final String hasBGP = ns + "hasBGP";
        public static final String hasSubBGP = ns + "hasSubBGP";
        public static final String extensionQuery = ns + "extensionQuery";
        public static final String joinExtensionQuery = ns + "joinExtensionQuery";

        //public static final String tpText = ns + "tpText";
        //public static final String triplePatternResultSize = ns + "triplePatternResultSize";

        // Used in benchmark run - align with other error msgs
        public static final String execStatus = ns + "execStatus";

        public static final String execError = ns + "execError";
        public static final String processingError = ns + "processingError";
        public static final String parseError = ns + "parseError";
        public static final String runTimeMs = ns + "runTimeMs";

        public static final String benchmarkRun = ns + "benchmarkRun";

        public static final String hasExec = ns + "hasExec";
        public static final String hasElementExec =  ns + "hasElementExec";
        public static final String hasLocalExec =  ns + "hasLocalExec";
        public static final String hasQueryExec =  ns + "hasQueryExec";
        public static final String hasRemoteExec = ns + "hasRemoteExec";

        public static final String hasBgpExec = ns + "hasBgpExec";
        public static final String hasTpExec = ns + "hasTpExec";
        public static final String hasJoinVarExec = ns + "hasJoinVarExec";
        public static final String hasTpInBgpExec = ns + "hasTpInBgpExec";

        public static final String usesFeature = "usesFeature";
        public static final String feature = "feature";
        public static final String count = ns + "count";


        public static final String Vertex = ns + "Vertex";
        public static final String Edge = ns + "Edge";

        public static final String hasEdge = ns + "hasEdge";

        public static final String in = ns + "in";
        public static final String out = ns + "out";
        // Internal use. Indicates that one resource represents another one
        public static final String proxyFor = ns + "proxyFor";



        public static final String Star = ns + "Star";
        public static final String Sink = ns + "Sink";
        public static final String Path = ns + "Path";
        public static final String Hybrid = ns + "Hybrid";


        public static final String joinVertex = ns + "joinVertex";
        public static final String joinVertexType = ns + "joinVertexType";
        public static final String joinVertexDegree = ns + "joinVertexDegree";

        public static final String bgps = ns + "bgps";
        public static final String tps = ns + "tps";
        public static final String minBgpTriples = ns + "minBGPTriples";
        public static final String maxBgpTriples = ns + "maxBGPTriples";
        public static final String joinVertices = ns + "joinVertices";
        public static final String projectVars = ns + "projectVars";

        //public static final String avgJoinVerticesDegree = ns + "avgJoinVerticesDegree";
        public static final String meanJoinVertexDegree = ns + "meanJoinVertexDegree";
        public static final String medianJoinVertexsDegree = ns + "medianJoinVertexDegree";

        public static final String hasBGPNode = ns + "hasBGPNode";

        public static final String mentionsSubject = ns + "mentionsSubject";
        public static final String mentionsPredicate = ns + "mentionsPredicate";
        public static final String mentionsObject = ns + "mentionsObject";

        public static final String mentionsTuple = ns + "mentionsTuple";

        public static final String sequenceId = ns + "sequenceId";

        public static final String host = ns + "host";
        public static final String hostHash = ns + "hostHash";
        public static final String atTime = PROV.ns + "atTime";
        public static final String endpoint = ns + "endpoint";
        public static final String userAgent = ns + "userAgent";

        public static final String config = ns + "config";
        public static final String requestDelay = ns + "requestDelay";
        public static final String connectionTimeout = ns + "connectionTimeout";
        public static final String queryTimeout = ns + "queryTimeout";
        public static final String datasetSize = ns + "datasetSize";
        public static final String datasetLabel = ns + "datasetLabel";
        public static final String datasetIri = ns + "datasetIri";


        public static final String tpSel = ns + "tpSel";

        // Selectivity of a triple pattern in regard to the BGP in which it occurrs
        public static final String tpSelBGPRestricted = ns +"tpSelBGPRestricted";

        // Selectivity of a triple pattern in regard to a variable that participates in a join with other TPs
        public static final String tpSelJoinVarRestricted = ns + "tpSelJoinVarRestricted";

    }



    public static Resource resource(String local) { return ResourceFactory.createResource(ns + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(ns + local); }

    // Used internally for the hypergraph representation - not part of the public vocab
    public static final Resource Vertex = ResourceFactory.createResource(Strs.Vertex);
    public static final Resource Edge = ResourceFactory.createResource(Strs.Edge);

    public static final Property in = ResourceFactory.createProperty(Strs.in);
    public static final Property out = ResourceFactory.createProperty(Strs.out);
    // Internal use. Indicates that one resource represents another one
    public static final Property proxyFor = property("proxyFor");



    public static final Resource Star = ResourceFactory.createResource(Strs.Star);
    public static final Resource Sink = ResourceFactory.createResource(Strs.Sink);
    public static final Resource Path = ResourceFactory.createResource(Strs.Path);
    public static final Resource Hybrid = ResourceFactory.createResource(Strs.Hybrid);


    public static final Property joinVertex = ResourceFactory.createProperty(Strs.joinVertex);
    public static final Property joinVertexType = ResourceFactory.createProperty(Strs.joinVertexType);
    public static final Property joinVertexDegree = ResourceFactory.createProperty(Strs.joinVertexDegree);

    public static final Property bgps = ResourceFactory.createProperty(Strs.bgps);
    public static final Property tps = ResourceFactory.createProperty(Strs.tps);
    public static final Property minBgpTriples = ResourceFactory.createProperty(Strs.minBgpTriples);
    public static final Property maxBgpTriples = ResourceFactory.createProperty(Strs.maxBgpTriples);
    public static final Property joinVertices = ResourceFactory.createProperty(Strs.joinVertices);
    public static final Property projectVars = ResourceFactory.createProperty(Strs.projectVars);

    //public static final Property avgJoinVerticesDegree = property("avgJoinVerticesDegree");
    public static final Property meanJoinVertexDegree = property("meanJoinVertexDegree");
    public static final Property medianJoinVertexsDegree = property("medianJoinVertexDegree");

    public static final Property mentionsSubject = property("mentionsSubject");
    public static final Property mentionsPredicate = property("mentionsPredicate");
    public static final Property mentionsObject = property("mentionsObject");

    public static final Property mentionsTuple = property("mentionsTuple");


//  stats = stats + getMentionsTuple(predicates); // subjects and objects

// These attributes are part of SPIN - no need to duplicate them
//    public static final Resource Select = resource("Select");
//    public static final Resource Construct = resource("");
//    public static final Resource Ask = resource("");
//    public static final Resource Describe = resource(org.topbraid.spin.vocabulary.SP));


    // Type so that all triple pattern executions in a query can be retrieved
    //public static final Resource TPExec = resource("TPExec");

    // An LSQ Query. It is different from SPIN::Query.
    // TODO Sort out the exact semantic relation - but its roughly:
    // A SPIN query represents a query itself, whereas a LSQ query represents a record about it
    // More concretely, an LSQ record holds information about at which time a certain query was fired based on which log file, etc.
    public static final Resource Query = ResourceFactory.createResource(Strs.Query);


    public static final Property text = ResourceFactory.createProperty(Strs.text);
    public static final Property resultSize = ResourceFactory.createProperty(Strs.resultSize);
    public static final Property hasStructuralFeatures = ResourceFactory.createProperty(Strs.hasStructuralFeatures);
    public static final Property hasSpin = ResourceFactory.createProperty(Strs.hasSpin);
    public static final Property hasTP = ResourceFactory.createProperty(Strs.hasTP);
    public static final Property hasBGP = ResourceFactory.createProperty(Strs.hasBGP);

    public static final Property hasBGPNode = ResourceFactory.createProperty(Strs.hasBGPNode);

    //public static final Property tpText = property("tpText");
    //public static final Property triplePatternResultSize = property("triplePatternResultSize");
    public static final Property execError = ResourceFactory.createProperty(Strs.execError);
    public static final Property processingError = ResourceFactory.createProperty(Strs.processingError);
    public static final Property parseError = ResourceFactory.createProperty(Strs.parseError);
    public static final Property runTimeMs = ResourceFactory.createProperty(Strs.runTimeMs);

    public static final Property benchmarkRun = ResourceFactory.createProperty(Strs.benchmarkRun);

    public static final Property hasExec = ResourceFactory.createProperty(Strs.hasExec);
    public static final Property hasLocalExec = ResourceFactory.createProperty(Strs.hasLocalExec);
    public static final Property hasRemoteExec = ResourceFactory.createProperty(Strs.hasRemoteExec);

    public static final Property hasBgpExec = ResourceFactory.createProperty(Strs.hasBgpExec);
    public static final Property hasTpExec = ResourceFactory.createProperty(Strs.hasTpExec);
    public static final Property hasJoinVarExec = ResourceFactory.createProperty(Strs.hasJoinVarExec);

    // Execution
    // Selectivity of a triple pattern in regard to the whole data set
    // TODO Rename to tpSelectivity(GraphRestricted)
    public static final Property tpSel = ResourceFactory.createProperty(Strs.tpSel);

    // Selectivity of a triple pattern in regard to the BGP in which it occurrs
    public static final Property tpSelBGPRestricted = ResourceFactory.createProperty(Strs.tpSelBGPRestricted);

    // Selectivity of a triple pattern in regard to a variable that participates in a join with other TPs
    public static final Property tpSelJoinVarRestricted = ResourceFactory.createProperty(Strs.tpSelJoinVarRestricted);

    // Similar to tpSelectivity, but considering immediate filters present on it
    // (maybe only those filters for which indexes can be used)
    //public static final Property fTpSelectivityBgpRestricted = property("fTpSelectivityBgpRestricted");


    public static final Property meanTPSelectivity = property("meanTPSelectivity");

    // TODO This is PROV vocab
    //public static final Property wasAssociatedWith = property("wasAssociatedWith");

    public static final Property usesFeature = property(Strs.usesFeature);

    public static final Property triplePath = property("triplePath");

    // TODO Not sure if the following metadata really belongs here ~ Claus
    // TODO This is covered by the MEX ontology
    public static final Property engine = property("engine");
    public static final Property vendor = property("vendor");
    public static final Property version = property("version");
    public static final Property processor = property("processor");
    public static final Property ram = property("ram");

    public static final Property dataset = property("dataset");

    public static final Property endpoint = ResourceFactory.createProperty(Strs.endpoint);
    public static final Property userAgent = ResourceFactory.createProperty(Strs.userAgent);


    // TODO This is actually the vocab for apache log parsing - move it elsewhere
    public static final Resource WebAccessLogFormat = resource("WebAccessLogFormat");
    public static final Property pattern = property("pattern");

    public static final Property sequenceId = ResourceFactory.createProperty(Strs.sequenceId);


    /**
     * logRecord: String representation of the original log entry - usually a line
     */
    public static final Property logRecord = property("logRecord");
    public static final Property host = ResourceFactory.createProperty(Strs.host);
    public static final Property hostHash = ResourceFactory.createProperty(Strs.hostHash);
    //public static final Property timestamp = ResourceFactory.createProperty(Strs.timestamp);

    public static final Property user = property("user");
    public static final Property request = property("request");
    public static final Property query = property("query");
    public static final Property path = property("uri");
    public static final Property queryString = property("queryString");
    public static final Property protocol = property("protocol");
    public static final Property headers = property("headers");
    public static final Property verb = property("verb");
    public static final Property parsed = property("parsed"); // Whether a log entry could be parsed

    public static final Property statusCode = property("statusCode");
    public static final Property execStatus = ResourceFactory.createProperty(Strs.execStatus);

    public static final Property numResponseBytes = property("numResponseBytes");



    // Query / Graph Pattern Features
    // None indicates the absence of features; must not appear with any other features
    public static final Resource None = resource("None");

    // TODO These terms can be found in SP instead of SPIN
    public static final Resource Select = resource("Select");
    public static final Resource Construct = resource("Construct");
    public static final Resource Describe = resource("Describe");
    public static final Resource Ask = resource("Ask");
    public static final Resource Unknown = resource("Unknown");

    public static final Resource TriplePattern = resource("TriplePattern");
    public static final Resource TriplePath = resource("TriplePath");
    public static final Resource Triple = resource("Triples");
    public static final Resource Group = resource("Group");
    public static final Resource Assign = resource("Assign");
    public static final Resource Dataset = resource("Dataset");
    public static final Resource SubQuery = resource("SubQuery");
    public static final Resource Filter = resource("Filter");
    public static final Resource Values = resource("Values");
    public static final Resource Bind = resource("Bind");
    public static final Resource Service = resource("Service");
    public static final Resource Exists = resource("Exists");
    public static final Resource NotExists = resource("NotExists");
    public static final Resource Minus = resource("Minus");
    public static final Resource NamedGraph = resource("NamedGraph");
    public static final Resource Union = resource("Union");
    public static final Resource Optional = resource("Optional");
    public static final Resource Reduced = resource("Reduced");
    public static final Resource Distinct = resource("Distinct");
    public static final Resource OrderBy = resource("OrderBy");
    public static final Resource GroupBy = resource("GroupBy");
    public static final Resource Aggregators = resource("Aggregators");
    public static final Resource Functions = resource("Functions");
    public static final Resource Offset = resource("Offset");
    public static final Resource Limit = resource("Limit");

    // Path Features
    public static final Resource LinkPath = resource("LinkPath");
    public static final Resource ReverseLinkPath = resource("ReverseLinkPath");
    public static final Resource NegPropSetPath = resource("NegPropSetPath");
    public static final Resource InversePath = resource("InversePath");
    public static final Resource ModPath = resource("ModPath");
    public static final Resource FixedLengthPath = resource("FixedLengthPath");
    public static final Resource DistinctPath = resource("DistinctPath");
    public static final Resource MultiPath = resource("MultiPath");
    public static final Resource ShortestPath = resource("ShortestPath");
    public static final Resource ZeroOrOnePath = resource("ZeroOrOnePath");

    // NOTE: We do not reflect syntactic differences of path expressions such as
    // fooPath{1,} and fooPath+ in the vocab
    public static final Resource ZeroOrMore1Path = resource("ZeroOrMorePath");
    public static final Resource ZeroOrMoreNPath = resource("ZeroOrMorePath");
    public static final Resource OneOrMore1Path = resource("oneOrMorePath");
    public static final Resource OneOrMoreNPath = resource("oneOrMorePath");
    public static final Resource AltPath = resource("AltPath");
    public static final Resource SeqPath = resource("SeqPath");


    public static final Property usesService = property("usesService");


    public static final String defaultLsqrNs = "http://lsq.aksw.org/res/";


    public static final Property hasVar = property("hasVar");
    public static final Property hasVarExec = property("hasVarExec");

    // Temporary id attributes - used to craft final IRIs
    public static final Property queryId = property("queryId");
    public static final Property bgpId = property("bgpId");
    public static final Property bgpVarId = property("bgpVarId");

    public static final Property tpId = property("tpId");
    public static final Property tpVarId = property("tpVarId");

    //public static final Property

    //lsqv:resultSize
    //lsqv:runTimeMs
    //lsqv:hasLocalExecution
    //lsqv:structuralFeatures
    //lsqv:runtimeError
    //lsqv:resultSize
    //lsqv:meanTriplePatternSelectivity
    //lsqv:joinVertexDegree 2 ; lsqv:joinVertexType lsqv:Star
    //lsqv:hasRemoteExecution
    //lsqv:endpoint --> maybe supersede by dataset distribution vocab (i think dcat has something)
    //

}
