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
import java.io.InputStream;

/**
 * The read method emits one bit from the underlying InputStream.
 * Values are read in 8 bit chunks from the underlying InputStream
 * and buffered by this class.
 * <p> 
 * Up to maxGarbageBits past the end of the underlying InputStream
 * may be read, after which a RuntimeException will be thrown.
 * <p>
 * Bits are read from the bottom of the byte. Given byte ZZZZZZYX, 
 * from the underlying InputStream X will be read first, then Y, etc.  
 */
public final class BitInputStream implements AutoCloseable {

    private final InputStream input;
    private int buffer = 0;
    private int bitsToGo = 0;
    private int garbageBits = 0;
    private final int maxGarbageBits;

    public BitInputStream(InputStream in, int maxGarbageBits) {
        if (in == null) { throw new RuntimeException(ErrorCodes.BIT_INPUT_STREAM_IS_NULL.toString()); }
        if (maxGarbageBits < 0) { throw new RuntimeException(ErrorCodes.BIT_INPUT_INVALID_MAX_GARBAGE_BITS.toString()); }
        this.input = in;
        this.maxGarbageBits = maxGarbageBits;
    }

    /**
     * Read a bit from the underlying InputStream. It will read and return
     * up to maxGarbageBits past the end of the underlying InputStream. If
     * more than maxGarbageBits are attempted to be read, a RuntimeException
     * will be thrown.
     */
    public int read() {
        try {
            return readThrowing();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private int readThrowing() throws IOException {
        if (bitsToGo == 0) {
            buffer = input.read();
            if (buffer == -1) {
                garbageBits++;
                if (garbageBits > maxGarbageBits) {
                    throw new RuntimeException(ErrorCodes.BIT_INPUT_READ_PAST_GARBAGE_BITS.toString());
                } else {
                    return 0;
                }
            }
            bitsToGo = 8;
        }
        
        int t = buffer&1;
        buffer = buffer >>> 1;
        bitsToGo--;
        return t;
    }

    /**
     * Close the underlying InputStream.
     */
    public void close() {
        try {
            input.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        buffer = -1;
        bitsToGo = 0;
    }
    
    private enum ErrorCodes {
        BIT_INPUT_READ_PAST_GARBAGE_BITS,
        BIT_INPUT_STREAM_IS_NULL,
        BIT_INPUT_INVALID_MAX_GARBAGE_BITS
    }
}
