package org.aksw.simba.lsq.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.http.repository.api.HttpRepository;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.ext.com.google.common.primitives.Ints;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;


interface PathHasher
{
    HashCode hash(SeekableByteChannel channel) throws IOException;
    HashCode hash(Path path) throws IOException;
}

/**
 * Hashes file content
 * By default, the hash considers the file size and the first and last 16MB of content
 *
 * @author raven
 *
 */
class PathHasherImpl
    implements PathHasher
{
    protected HashFunction hashFunction;

    /**
     * number of bytes to use for hashing from start and tail.
     * if the file size is less than 2 * numBytes, the whole file will be hashed
     *
     */
    protected int numBytes;

    public static PathHasher createDefault() {
        return new PathHasherImpl(Hashing.sha256(), 16 * 1024 * 1024);
    }

    public PathHasherImpl(HashFunction hashFunction, int numBytes) {
        super();
        this.hashFunction = hashFunction;
        this.numBytes = numBytes;
    }

    @Override
    public HashCode hash(Path path) throws IOException {
        HashCode result;
        try(SeekableByteChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            result = hash(channel);
        }

        return result;
    }

    @Override
    public HashCode hash(SeekableByteChannel channel) throws IOException {
        long channelSize = channel.size();
        Hasher hasher = hashFunction.newHasher();

        hasher.putLong(channelSize);
        hasher.putChar('-');

        Iterable<Entry<Long, Integer>> posAndLens;
        if(channelSize < numBytes * 2) {
            posAndLens = Collections.singletonMap(0l, Ints.checkedCast(channelSize)).entrySet();
        } else {
            posAndLens = ImmutableMap.<Long, Integer>builder()
                    .put(0l, numBytes)
                    .put(channelSize - numBytes, numBytes)
                    .build()
                    .entrySet()
                    ;
        }

        ByteBuffer buffer = null;
        for(Entry<Long, Integer> e : posAndLens) {
            Long pos = e.getKey();
            Integer len = e.getValue();

            if(buffer == null || buffer.remaining() < len) {
                buffer = ByteBuffer.wrap(new byte[len]);
            }

            channel.position(pos);
            channel.read(buffer.duplicate());
            hasher.putBytes(buffer.duplicate());
        }

        HashCode result = hasher.hash();
        return result;
    }
}

interface VersionedTransform<T>
//    extends MvnEntityCore
{
    public String getId();
    public String getHash();

}

//class DatasetTransform
//	extends VersionedTransform<Dataset, Dataset>
//{
//
//}

class RdfDerive {
    public static void derive(
            HttpRepository repo,
            Flowable<Dataset> input,
            String transformId,
            FlowableTransformer<Dataset, Dataset> transformer
            ) {

    }

}

public class TestExtendedSpinModel {
    static final Logger logger = LoggerFactory.getLogger(TestExtendedSpinModel.class);


    public static void createLsqIndex() throws IOException {
        Path path = null;
        PathHasher hasher = PathHasherImpl.createDefault();
        hasher.hash(path);


        // We should use the dcat system here to track of created indexes...
        // Create a hash from head, tail
    }

//    public static void createIndexBgps(Flowable<LsqQuery> flow) {
//        // TODO How to get the shape triples??
//
//        //SorterFactory sf = new SorterFactoryFromSysCall();
//        CmdNgsSort sortCmd = new CmdNgsSort();
//        SparqlQueryParser sparqlParser = SparqlQueryParserImpl.create();
//        OutputStream out = new FileOutputStream(FileDescriptor.out);
//
//        try {
//            flow
//                .flatMap(x -> Flowable.fromIterable(x.getSpinQuery().as(SpinQueryEx.class).getBgps()))
//                .flatMap(bgp -> Flowable.fromIterable(bgp.getTriplePatterns()))
//                .map(tp -> ResourceInDatasetImpl.createFromCopyIntoResourceGraph(tp))
//                .compose(ResourceInDatasetFlowOps.createTransformerFromGroupedTransform(
//                        ResourceInDatasetFlowOps.createSystemSorter(sortCmd, sparqlParser)))
//                .map(rid -> rid.getDataset())
//                .compose(RDFDataMgrRx.createDatasetWriter(out, RDFFormat.TRIG_PRETTY))
//                .singleElement()
//                .blockingGet()
//                ;
//
//        } catch (Exception e) {
//            ExceptionUtils.rethrowIfNotBrokenPipe(e);
//        }
//    }

    public static void createIndexTriplePatterns(Flowable<LsqQuery> flow) {

    }



}
