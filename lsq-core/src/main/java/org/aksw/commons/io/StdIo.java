package org.aksw.commons.io;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;

public class StdIo {
    public static final OutputStream STDOUT = new CloseShieldOutputStream(new FileOutputStream(FileDescriptor.out));
}
