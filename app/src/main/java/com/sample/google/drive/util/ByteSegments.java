package com.sample.google.drive.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

public final class ByteSegments {

    private Segment head;

    public static byte[] toByteArray(InputStream is) throws IOException {
        return new ByteSegments()
                .readFrom(is)
                .toByteArray();
    }

    public static byte[] toByteArray(InputSource source) throws IOException {
        InputStream is = source.open();
        try {
            return toByteArray(is);
        } finally {
            IOUtils.close(is);
        }
    }

    public int size() {
        return head != null ? head.prevSize + head.pos : 0;
    }

    public int allocatedSize() {
        return head != null ? head.count * Segment.SIZE : 0;
    }

    public byte[] toByteArray() {
        byte[] array = new byte[size()];
        Segment it = head;
        while (it != null) {
            System.arraycopy(it.data, 0, array, it.prevSize, it.pos);
            it = it.prev;
        }
        return array;
    }

    public ByteSegments readFrom(InputStream is) throws IOException {
        int read;
        while (true) {
            Segment segment = current();
            read = is.read(segment.data, segment.pos, segment.availLength());
            if (read == -1) {
                return this;
            }
            segment.pos += read;
        }
    }

    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b});
            }

            @Override
            public void write(@NonNull byte[] buffer) throws IOException {
                write(buffer, 0, buffer.length);
            }

            @Override
            public void write(@NonNull byte[] buffer, int offset, int length) throws IOException {
                while (length > 0) {
                    Segment segment = current();
                    int copy = Math.min(segment.availLength(), length);
                    System.arraycopy(buffer, offset, segment.data, segment.pos, copy);
                    segment.pos += copy;
                    offset += copy;
                    length -= copy;
                }
            }
        };
    }

    public InputStream inputStream() {
        return new InputStream() {
            private int position = 0;
            private int mark;

            @Override
            public int read() throws IOException {
                Segment it = head;
                while (it != null) {
                    int pos = position - it.prevSize;
                    if (0 <= pos && pos < it.pos) {
                        ++position;
                        return it.data[pos];
                    }
                    it = it.prev;
                }
                return -1;
            }

            @Override
            public int read(@NonNull byte[] buffer) throws IOException {
                return read(buffer, 0, buffer.length);
            }

            @Override
            public int read(@NonNull byte[] buffer, int offset, int length) throws IOException {
                int position = this.position;
                if (position >= size()) {
                    return -1;
                }
                Segment it = head;
                while (it != null) {
                    int index = position - it.prevSize;
                    int start = Math.max(0, Math.min(it.pos, index));
                    int end = Math.max(0, Math.min(it.pos, index + length));
                    int copy = end - start;
                    if (copy != 0) {
                        int target = offset + start - index;
                        System.arraycopy(it.data, start, buffer, target, copy);
                    }
                    it = it.prev;
                }
                this.position = Math.min(size(), position + length);
                return this.position - position;
            }

            @Override
            public long skip(long l) throws IOException {
                int oldPosition = position;
                position = Math.min(size(), position + (int) l);
                return position - oldPosition;
            }

            @Override
            public int available() throws IOException {
                return size() - position;
            }

            @Override
            public void mark(int i) {
                mark = position;
            }

            @Override
            public void reset() {
                position = mark;
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
    }

    public void copyTo(OutputStream outputStream) throws IOException {
        if (head != null) {
            head.writeTo(outputStream);
        }
    }

    private Segment current() {
        Segment segment = head;
        if (segment == null || segment.pos == Segment.SIZE) {
            return next();
        }
        return segment;
    }

    private Segment next() {
        return head = new Segment(head);
    }

    private static class Segment {
        static final int SIZE = 4096;

        final byte[] data = new byte[SIZE];

        final Segment prev;
        final int prevSize;
        final int count;

        int pos;

        Segment(Segment prev) {
            this.prev = prev;
            prevSize = prev == null ? 0 : prev.prevSize + prev.pos;
            count = prev == null ? 1 : prev.count + 1;
        }

        int availLength() {
            return SIZE - pos;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            if (prev != null) {
                prev.writeTo(outputStream);
            }
            outputStream.write(data, 0, pos);
        }
    }
}
