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
    public static final String NS = "http://lsq.aksw.org/vocab#";


    public static class Strs {
        public static final String Query = NS + "Query";

        public static final String text = NS + "text";
        public static final String hash = NS + "hash";
        public static final String itemCount = NS + "itemCount";
        public static final String isDistinct = NS + "isDistinct";

        public static final String exceededMaxByteSizeForCounting = NS + "exceededMaxByteSizeForCounting";
        public static final String exceededMaxItemCountForCounting = NS + "exceededMaxItemCountForCounting";
        public static final String exceededMaxByteSizeForSerialization = NS + "exceededMaxByteSizeForSerialization";
        public static final String exceededMaxItemCountForSerialization = NS + "exceededMaxItemCountForSerialization";

        public static final String serializedResult = NS + "serializedResult";

        public static final String maxItemCountForCounting = NS + "maxItemCountForCounting";
        public static final String maxByteSizeForCounting = NS + "maxByteSizeForCounting";
        public static final String maxItemCountForSerialization = NS + "maxItemCountForSerialization";
        public static final String maxByteSizeForSerialization = NS + "maxByteSizeForSerialization";


//        public static final String distinctResultSize = ns + "distinctResultSize";

        public static final String hasStructuralFeatures = NS + "hasStructuralFeatures";
        public static final String hasSpin = NS + "hasSpin";
        public static final String hasTP = NS + "hasTP";
        public static final String hasTpInBgp = NS + "hasTpInBgp";
        public static final String hasBGP = NS + "hasBGP";
        public static final String hasSubBGP = NS + "hasSubBGP";
        public static final String extensionQuery = NS + "extensionQuery";
        public static final String joinExtensionQuery = NS + "joinExtensionQuery";

        //public static final String tpText = ns + "tpText";
        //public static final String triplePatternResultSize = ns + "triplePatternResultSize";

        // Used in benchmark run - align with other error msgs
        public static final String execStatus = NS + "execStatus";

        public static final String execError = NS + "execError";
        public static final String processingError = NS + "processingError";
        public static final String parseError = NS + "parseError";
        public static final String runTimeMs = NS + "runTimeMs";

        public static final String benchmarkRun = NS + "benchmarkRun";

        public static final String hasExec = NS + "hasExec";
        public static final String hasElementExec =  NS + "hasElementExec";
        public static final String hasLocalExec =  NS + "hasLocalExec";
        public static final String hasQueryExec =  NS + "hasQueryExec";
        public static final String hasRemoteExec = NS + "hasRemoteExec";

        public static final String hasBgpExec = NS + "hasBgpExec";
        public static final String hasTpExec = NS + "hasTpExec";
        public static final String hasJoinVarExec = NS + "hasJoinVarExec";
        public static final String hasTpInBgpExec = NS + "hasTpInBgpExec";
        public static final String hasSubBgpExec = NS + "hasSubBgpExec";

        public static final String usesFeature = "usesFeature";
        public static final String feature = "feature";
        public static final String count = NS + "count";


        public static final String Vertex = NS + "Vertex";
        public static final String Edge = NS + "Edge";

        public static final String hasEdge = NS + "hasEdge";

        public static final String in = NS + "in";
        public static final String out = NS + "out";
        // Internal use. Indicates that one resource represents another one
        public static final String proxyFor = NS + "proxyFor";



        // Join vertex type (used as an attribute, hence lower camel case spelling)
        public static final String star = NS + "star";
        public static final String sink = NS + "sink";
        public static final String path = NS + "path";
        public static final String hybrid = NS + "hybrid";


        public static final String joinVertex = NS + "joinVertex";
        public static final String joinVertexType = NS + "joinVertexType";
        public static final String joinVertexDegree = NS + "joinVertexDegree";

        public static final String bgpCount = NS + "bgpCount";
        public static final String tpCount = NS + "tpCount";

        public static final String tpInBgpCountMin = NS + "tpInBgpCountMin";
        public static final String tpInBgpCountMax = NS + "tpInBgpCountMax";
        public static final String tpInBgpCountMean = NS + "tpInBgpCountMean";
        public static final String tpInBgpCountMedian = NS + "tpInBgpCountMedian";

        public static final String joinVertexCountTotal = NS + "joinVertexCountTotal";
        public static final String projectVarCount = NS + "projectVarCount";

        //public static final String avgJoinVerticesDegree = ns + "avgJoinVerticesDegree";
        public static final String joinVertexDegreeMean = NS + "joinVertexDegreeMean";
        public static final String joinVertexDegreeMedian = NS + "joinVertexDegreeMedian";

        public static final String hasBgpNode = NS + "hasBgpNode";

        public static final String mentionsSubject = NS + "mentionsSubject";
        public static final String mentionsPredicate = NS + "mentionsPredicate";
        public static final String mentionsObject = NS + "mentionsObject";

        public static final String mentionsTuple = NS + "mentionsTuple";

        public static final String sequenceId = NS + "sequenceId";

        public static final String host = NS + "host";
        public static final String hostHash = NS + "hostHash";
        public static final String atTime = PROV.ns + "atTime";
        public static final String endpoint = NS + "endpoint";
        public static final String userAgent = NS + "userAgent";

        public static final String config = NS + "config";
        public static final String requestDelay = NS + "requestDelay";
        public static final String connectionTimeout = NS + "connectionTimeout";
        public static final String executionTimeout = NS + "executionTimeout";
//        public static final String resultSetSizeThreshold = ns + "resultSetSizeThreshold";
        public static final String datasetSize = NS + "datasetSize";
        public static final String datasetLabel = NS + "datasetLabel";
        public static final String datasetIri = NS + "datasetIri";
        public static final String baseIri = NS + "baseIri";

        public static final String tpToGraphRatio = NS + "tpToGraphRatio";

        // Selectivity of a triple pattern in regard to the BGP in which it occurrs
        public static final String tpSelBGPRestricted = NS +"tpSelBGPRestricted";

        public static final String tpToBgpRatio = NS +"tpToBgpRatio";

        // Selectivity of a triple pattern in regard to a variable that participates in a join with other TPs
        public static final String tpSelJoinVarRestricted = NS + "tpSelJoinVarRestricted";

    }



    public static Resource resource(String local) { return ResourceFactory.createResource(NS + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(NS + local); }

    public static final Property config = ResourceFactory.createProperty(Strs.config);

    // Used internally for the hypergraph representation - not part of the public vocab
    public static final Resource Vertex = ResourceFactory.createResource(Strs.Vertex);
    public static final Resource Edge = ResourceFactory.createResource(Strs.Edge);

    public static final Property in = ResourceFactory.createProperty(Strs.in);
    public static final Property out = ResourceFactory.createProperty(Strs.out);
    // Internal use. Indicates that one resource represents another one
    public static final Property proxyFor = property("proxyFor");



    public static final Resource star = ResourceFactory.createResource(Strs.star);
    public static final Resource sink = ResourceFactory.createResource(Strs.sink);
    public static final Resource path = ResourceFactory.createResource(Strs.path);
    public static final Resource hybrid = ResourceFactory.createResource(Strs.hybrid);


    public static final Property joinVertex = ResourceFactory.createProperty(Strs.joinVertex);
    public static final Property joinVertexType = ResourceFactory.createProperty(Strs.joinVertexType);
    public static final Property joinVertexDegree = ResourceFactory.createProperty(Strs.joinVertexDegree);

    public static final Property bgps = ResourceFactory.createProperty(Strs.bgpCount);
    public static final Property tps = ResourceFactory.createProperty(Strs.tpCount);
    public static final Property minBgpTriples = ResourceFactory.createProperty(Strs.tpInBgpCountMin);
    public static final Property maxBgpTriples = ResourceFactory.createProperty(Strs.tpInBgpCountMax);
    public static final Property joinVertices = ResourceFactory.createProperty(Strs.joinVertexCountTotal);
    public static final Property projectVars = ResourceFactory.createProperty(Strs.projectVarCount);

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
    public static final Property itemCount = ResourceFactory.createProperty(Strs.itemCount);
    public static final Property hasStructuralFeatures = ResourceFactory.createProperty(Strs.hasStructuralFeatures);
    public static final Property hasSpin = ResourceFactory.createProperty(Strs.hasSpin);
    public static final Property hasTP = ResourceFactory.createProperty(Strs.hasTP);
    public static final Property hasBGP = ResourceFactory.createProperty(Strs.hasBGP);

    public static final Property hasBGPNode = ResourceFactory.createProperty(Strs.hasBgpNode);

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
    public static final Property tpSel = ResourceFactory.createProperty(Strs.tpToGraphRatio);

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
    public static final Property requestPath = property("uri");
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
