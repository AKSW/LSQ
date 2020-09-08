package org.aksw.commons.io;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;

public class StdIo {
    public static final OutputStream STDOUT = new FileOutputStream(FileDescriptor.out);
    public static final OutputStream STDERR = new FileOutputStream(FileDescriptor.err);

    public static OutputStream openStdout() {
        return new CloseShieldOutputStream(STDOUT);
    }

    public static OutputStream openStderr() {
        return new CloseShieldOutputStream(STDERR);
    }

//    public static OutputStream openStdin() {
//        return new CloseShieldInputStream(new FileInputStream(FileDescriptor.in));
//    }

}
