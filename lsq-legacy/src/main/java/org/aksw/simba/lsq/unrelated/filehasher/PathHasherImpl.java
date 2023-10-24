package org.aksw.simba.lsq.unrelated.filehasher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map.Entry;

import com.google.common.primitives.Ints;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;


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
public class PathHasherImpl
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
