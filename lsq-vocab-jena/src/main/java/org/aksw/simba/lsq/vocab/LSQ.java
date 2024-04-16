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


    public static class Terms {
        public static final String Query = NS + "Query";

        public static final String text = NS + "text";
        public static final String hash = NS + "hash";

        public static final String runId = NS + "runId";

        public static final String countValue = NS + "countValue";
        // XXX isCountDistinct / isCountStar

        public static final String resultCount = NS + "resultCount";
        public static final String isDistinct = NS + "isDistinct";


        // Error reporting duing benchmarking upon exceeding thresholds during counting and/or retrieval
        public static final String exceededMaxByteSizeForCounting = NS + "exceededMaxByteSizeForCounting";
        public static final String exceededMaxResultCountForCounting = NS + "exceededMaxResultCountForCounting";
        public static final String exceededMaxByteSizeForSerialization = NS + "exceededMaxByteSizeForSerialization";
        public static final String exceededMaxResultCountForSerialization = NS + "exceededMaxResultCountForSerialization";

//        public static final String exceededMaxCount = NS + "exceededMaxCount";

        public static final String serializedResult = NS + "serializedResult";


        // Vocab for benchmark configuration
        // Note: In a future LSQ version we could allow configuration of thresholds separately for
        // primary queries (the queries from the log) and secondary ones (those derived from bgp and tps)
        // (However, secondary queries may occurr also occur as primary ones, so more management would be needed)

        public static final String benchmarkSecondaryQueries = NS + "benchmarkSecondaryQueries";

        public static final String maxCount = NS + "maxCount";
        public static final String maxCountAffectsTp = NS + "maxCountAffectsTp";
        public static final String maxResultCountForRetrieval = NS + "maxResultCountForRetrieval";
        public static final String maxByteSizeForRetrieval = NS + "maxByteSizeForRetrieval";
        public static final String maxResultCountForSerialization = NS + "maxResultCountForSerialization";
        public static final String maxByteSizeForSerialization = NS + "maxByteSizeForSerialization";

        public static final String retrievalDuration = NS + "retrievalDuration";
        public static final String countingDuration = NS + "countingDuration";
        public static final String evalDuration = NS + "evalDuration";


//        public static final String distinctResultSize = ns + "distinctResultSize";

        public static final String hasStructuralFeatures = NS + "hasStructuralFeatures";
        public static final String hasSpin = NS + "hasSpin";
        public static final String hasTp = NS + "hasTp";
        public static final String hasTpInBgp = NS + "hasTpInBgp";
        public static final String hasBgp = NS + "hasBgp";
        public static final String hasSubBgp = NS + "hasSubBgp";
        public static final String extensionQuery = NS + "extensionQuery";

        // Link to the resource that corresponds to the query SELECT COUNT(DISTINCT joinVar) WHERE subBgp
        public static final String joinExtensionQuery = NS + "joinExtensionQuery";

        //public static final String tpText = ns + "tpText";
        //public static final String triplePatternResultSize = ns + "triplePatternResultSize";

        public static final String headers = NS + "headers";

        // Vocab for benchmark statistics (TODO group with the other error reporting (threshold exceeded) predicates above)

        public static final String execStatus = NS + "execStatus";

        public static final String execError = NS + "execError";
        public static final String processingError = NS + "processingError";
        public static final String parseError = NS + "parseError";
        public static final String runTimeMs = NS + "runTimeMs";

        public static final String retrievalError = NS + "retrievalError";
        public static final String countingError = NS + "countingError";


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

        // Indicates that a resource represents an RDF term or a variable
        // (we cannot have those in the subject position so we need a "proxy" resource)
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

        public static final String joinVertexCount = NS + "joinVertexCount";
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
        public static final String atTime = PROV.NS + "atTime";
        public static final String endpoint = NS + "endpoint";
        public static final String userAgent = NS + "userAgent";

        public static final String config = NS + "config";
        public static final String requestDelay = NS + "requestDelay";

        public static final String connectionTimeoutForRetrieval = NS + "connectionTimeoutForRetrieval";
        public static final String executionTimeoutForRetrieval = NS + "executionTimeoutForRetrieval";
        public static final String connectionTimeoutForCounting = NS + "connectionTimeoutForCounting";
        public static final String executionTimeoutForCounting = NS + "executionTimeoutForCounting";


//        public static final String resultSetSizeThreshold = ns + "resultSetSizeThreshold";
        public static final String datasetSize = NS + "datasetSize";
        public static final String datasetLabel = NS + "datasetLabel";
        public static final String datasetIri = NS + "datasetIri";
        public static final String baseIri = NS + "baseIri";

        public static final String tpToGraphRatio = NS + "tpToGraphRatio";

        // Selectivity of a triple pattern in regard to the BGP in which it occurrs
        public static final String tpSelBGPRestricted = NS +"bgpRestrictedTpSel";

        public static final String tpToBgpRatio = NS +"tpToBgpRatio";

        // Selectivity of a triple pattern in regard to a variable that participates in a join with other TPs
        public static final String tpSelJoinVarRestricted = NS + "tpSelJoinVarRestricted";

    }



    public static Resource resource(String local) { return ResourceFactory.createResource(NS + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(NS + local); }

    public static final Property config = ResourceFactory.createProperty(Terms.config);

    // Used internally for the hypergraph representation - not part of the public vocab
    public static final Resource Vertex = ResourceFactory.createResource(Terms.Vertex);
    public static final Resource Edge = ResourceFactory.createResource(Terms.Edge);

    public static final Property in = ResourceFactory.createProperty(Terms.in);
    public static final Property out = ResourceFactory.createProperty(Terms.out);

    // Indicates that a resource represents an RDF term or a variable
    // (we cannot have those in the subject position so we need a "proxy" resource)
    public static final Property proxyFor = property("proxyFor");



    public static final Resource star = ResourceFactory.createResource(Terms.star);
    public static final Resource sink = ResourceFactory.createResource(Terms.sink);
    public static final Resource path = ResourceFactory.createResource(Terms.path);
    public static final Resource hybrid = ResourceFactory.createResource(Terms.hybrid);


    public static final Property joinVertex = ResourceFactory.createProperty(Terms.joinVertex);
    public static final Property joinVertexType = ResourceFactory.createProperty(Terms.joinVertexType);
    public static final Property joinVertexDegree = ResourceFactory.createProperty(Terms.joinVertexDegree);

    public static final Property bgps = ResourceFactory.createProperty(Terms.bgpCount);
    public static final Property tps = ResourceFactory.createProperty(Terms.tpCount);
    public static final Property minBgpTriples = ResourceFactory.createProperty(Terms.tpInBgpCountMin);
    public static final Property maxBgpTriples = ResourceFactory.createProperty(Terms.tpInBgpCountMax);
    public static final Property joinVertices = ResourceFactory.createProperty(Terms.joinVertexCount);
    public static final Property projectVars = ResourceFactory.createProperty(Terms.projectVarCount);

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
    public static final Resource Query = ResourceFactory.createResource(Terms.Query);


    public static final Property text = ResourceFactory.createProperty(Terms.text);
    public static final Property resultCount = ResourceFactory.createProperty(Terms.resultCount);
    public static final Property hasStructuralFeatures = ResourceFactory.createProperty(Terms.hasStructuralFeatures);
    public static final Property hasSpin = ResourceFactory.createProperty(Terms.hasSpin);
    public static final Property hasTp = ResourceFactory.createProperty(Terms.hasTp);
    public static final Property hasBGP = ResourceFactory.createProperty(Terms.hasBgp);

    public static final Property hasBGPNode = ResourceFactory.createProperty(Terms.hasBgpNode);

    //public static final Property tpText = property("tpText");
    //public static final Property triplePatternResultSize = property("triplePatternResultSize");
    public static final Property execError = ResourceFactory.createProperty(Terms.execError);
    public static final Property processingError = ResourceFactory.createProperty(Terms.processingError);
    public static final Property parseError = ResourceFactory.createProperty(Terms.parseError);
    public static final Property runTimeMs = ResourceFactory.createProperty(Terms.runTimeMs);

    public static final Property benchmarkRun = ResourceFactory.createProperty(Terms.benchmarkRun);

    public static final Property hasExec = ResourceFactory.createProperty(Terms.hasExec);
    public static final Property hasLocalExec = ResourceFactory.createProperty(Terms.hasLocalExec);
    public static final Property hasRemoteExec = ResourceFactory.createProperty(Terms.hasRemoteExec);

    public static final Property hasBgpExec = ResourceFactory.createProperty(Terms.hasBgpExec);
    public static final Property hasTpExec = ResourceFactory.createProperty(Terms.hasTpExec);
    public static final Property hasJoinVarExec = ResourceFactory.createProperty(Terms.hasJoinVarExec);

    // Execution
    // Selectivity of a triple pattern in regard to the whole data set
    // TODO Rename to tpSelectivity(GraphRestricted)
    public static final Property tpSel = ResourceFactory.createProperty(Terms.tpToGraphRatio);

    // Selectivity of a triple pattern in regard to the BGP in which it occurrs
    public static final Property tpSelBGPRestricted = ResourceFactory.createProperty(Terms.tpSelBGPRestricted);

    // Selectivity of a triple pattern in regard to a variable that participates in a join with other TPs
    public static final Property tpSelJoinVarRestricted = ResourceFactory.createProperty(Terms.tpSelJoinVarRestricted);

    // Similar to tpSelectivity, but considering immediate filters present on it
    // (maybe only those filters for which indexes can be used)
    //public static final Property fTpSelectivityBgpRestricted = property("fTpSelectivityBgpRestricted");


    public static final Property meanTPSelectivity = property("meanTPSelectivity");

    // TODO This is PROV vocab
    //public static final Property wasAssociatedWith = property("wasAssociatedWith");

    public static final Property usesFeature = property(Terms.usesFeature);

    public static final Property triplePath = property("triplePath");

    // TODO Not sure if the following metadata really belongs here ~ Claus
    // TODO This is covered by the MEX ontology
    public static final Property engine = property("engine");
    public static final Property vendor = property("vendor");
    public static final Property version = property("version");
    public static final Property processor = property("processor");
    public static final Property ram = property("ram");

    public static final Property dataset = property("dataset");

    public static final Property endpoint = ResourceFactory.createProperty(Terms.endpoint);
    public static final Property userAgent = ResourceFactory.createProperty(Terms.userAgent);


    // TODO This is actually the vocab for apache log parsing - move it elsewhere
    public static final Resource WebAccessLogFormat = resource("WebAccessLogFormat");
    public static final Resource CsvLogFormat = resource("CsvLogFormat");

    public static final Property pattern = property("pattern");

    public static final Property sequenceId = ResourceFactory.createProperty(Terms.sequenceId);


    /**
     * logRecord: String representation of the original log entry - usually a line
     */
    public static final Property logRecord = property("logRecord");
    public static final Property host = ResourceFactory.createProperty(Terms.host);
    public static final Property hostHash = ResourceFactory.createProperty(Terms.hostHash);
    //public static final Property timestamp = ResourceFactory.createProperty(Strs.timestamp);

    public static final Property user = property("user");
    public static final Property request = property("request");
    public static final Property query = property("query");
    public static final Property requestPath = property("uri");
    public static final Property queryString = property("queryString");
    public static final Property protocol = property("protocol");
    public static final Property headers = property(Terms.headers);
    public static final Property verb = property("verb");
    public static final Property parsed = property("parsed"); // Whether a log entry could be parsed

    public static final Property statusCode = property("statusCode");
    public static final Property execStatus = ResourceFactory.createProperty(Terms.execStatus);

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
    public static final Resource Find = resource("Find");
    public static final Resource Distinct = resource("Distinct");
    public static final Resource OrderBy = resource("OrderBy");
    public static final Resource GroupBy = resource("GroupBy");
    public static final Resource Aggregators = resource("Aggregators");
    public static final Resource Functions = resource("Functions");
    public static final Resource Offset = resource("Offset");
    public static final Resource Limit = resource("Limit");
    public static final Resource Lateral = resource("Lateral");

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
