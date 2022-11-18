/*
 *     Arithmetic Coding
 *     Copyright (C) 2022  Michael Mersic
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.mersic.arithmeticcoder.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The write method writes one bit to the underlying OutputStream
 * in 8 bit chunks.
 * <p>
 * When the close method is called any unwritten bits and additional
 * zeros to fill the last byte are written.
 * <p>
 * Bits are written to the top of the byte, ie XABCDEFG, X is written
 * first. Then YXABCDEF Y is written second.  G is dropped, it was 
 * not valued.  Once 8 bits are written, the byte ZZZZZZYX is written.
 */
public final class BitOutputStream implements AutoCloseable {

    private final OutputStream output;
    private int buffer;
    private int bitsToGo;

    public BitOutputStream(OutputStream out) {
        if (out == null) { throw new IllegalArgumentException("out is null"); }
        output = out;
        buffer = 0;
        bitsToGo = 8;
    }

    /**
     * Write the bit to the underlying OutputStream.
     * @param b A 0 or 1.
     */
    public void write(int b)  {
        try {
            writeThrowing(b);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void writeThrowing(int b) throws IOException {
        buffer = buffer >>> 1; 
        if (b == 1) {
            buffer |= 0x80;
        }
        bitsToGo--;
        if (bitsToGo == 0) {
            output.write(buffer);
            bitsToGo = 8;
        }
    }

    /**
     * Write any buffered bits then close the underlying OutputStream.
     */
    public void close() {
        try {
            output.write(buffer >>> bitsToGo);
            output.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
