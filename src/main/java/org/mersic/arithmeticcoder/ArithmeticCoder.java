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
package org.mersic.arithmeticcoder;

import org.mersic.arithmeticcoder.io.BitInputStream;
import org.mersic.arithmeticcoder.io.BitOutputStream;
import org.mersic.arithmeticcoder.model.Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * This class offers two methods, encode and decode. They are threadsafe if the passed
 * in Model is threadsafe.
 */
public final class ArithmeticCoder {

    /**
     * This value is needed to determine the maximum allowed maxFreq value, that is
     * (codeValueBits-2)^2 - 1. This is the default value for AdaptiveModel.
     */
    public static final int codeValueBits = 30;
    private static final long topValue = (1L <<codeValueBits)-1; //Largest possible code value.
    private static final long firstQtr = topValue/4 + 1;
    private static final long half = 2 * firstQtr;
    private static final long thirdQtr = 3 * firstQtr;
    
    public void encode(Model model, InputStream inputStream, OutputStream outputStream) {
        try (BitOutputStream bos = new BitOutputStream(outputStream);
             InputStreamX in = new InputStreamX(inputStream)) {
            model.startEncode();
            EncodeContext ec = new EncodeContext(bos);
            Map<Integer, Integer> charToSymbol = model.getCharToSymbol();
            long[] cumulativeFreq = model.getCumulativeFreq();
            ensureNeighboringValuesDifferByAtLeastOne(cumulativeFreq);
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    break;
                }
                int symbol = charToSymbol.get(ch);
                encodeSymbol(ec, symbol, cumulativeFreq);
                model.updateModel(symbol);
            }
            encodeSymbol(ec, model.getEOFSymbol(), cumulativeFreq);
            doneEncoding(ec);
        } finally {
            model.finishEncode();
        }
    }

    private void ensureNeighboringValuesDifferByAtLeastOne(long[] cumulativeFreq) {
        for (int i = 1; i < cumulativeFreq.length; i++) {
            if (cumulativeFreq[i] - cumulativeFreq[i-1] == 0) {
                throw new RuntimeException("Neighboring values in cumulativeFreq array must differ. Indexes " 
                        + i + " " + (i-1) + " are both value: " + cumulativeFreq[i]);
            }
        }
    }

    private void doneEncoding(EncodeContext ec) {
        ec.bitsToFollow++;
        if (ec.low < firstQtr) {
            bitPlusFollow(ec,0);
        } else {
            bitPlusFollow(ec,1);
        }
    }
    
    private void encodeSymbol(EncodeContext ec, int symbol, long[] cumulativeFreq) {
        long range = (ec.high-ec.low) + 1;
        ec.high = ec.low + (range*cumulativeFreq[symbol-1])/cumulativeFreq[0]-1;
        ec.low = ec.low + (range*cumulativeFreq[symbol])/cumulativeFreq[0];
        while (true) {
            if (ec.high < half) {
                bitPlusFollow(ec,0);
            } else if (ec.low >= half) {
                bitPlusFollow(ec,1);
                ec.low -= half;
                ec.high -= half;
            } else if (ec.low >= firstQtr && ec.high < thirdQtr) {
                ec.bitsToFollow++;
                ec.low -= firstQtr;
                ec.high -= firstQtr;
            } else {
                break;
            }
            ec.low *= 2;
            ec.high = ec.high * 2 + 1;
        }
    }
    
    private void bitPlusFollow(EncodeContext ec, int b) {
        ec.out.write(b);
        if (ec.bitsToFollow > 0) {
            b = b == 0 ? 1 : 0; //flip the bit
            while (ec.bitsToFollow > 0) {
                ec.out.write(b);
                ec.bitsToFollow--;
            }
        }
    }
    
    public void decode(Model model, InputStream inputStream, OutputStream output) {
        try (BitInputStream in = new BitInputStream(inputStream, this.getMaxGarbageBits());
             OutputStreamX out = new OutputStreamX(output)) {
            model.startDecode();
            long decodeValue = 0;
            for (int i = 1; i <= codeValueBits; i++) {
                decodeValue = 2 * decodeValue + in.read();
            }
            Map<Integer, Integer> symbolToChar = model.getSymbolToChar();
            long[] cumulativeFreq = model.getCumulativeFreq();
            ensureNeighboringValuesDifferByAtLeastOne(cumulativeFreq);
            int EOF = model.getEOFSymbol();
            DecodeContext dc = new DecodeContext();
            dc.decodeValue = decodeValue;

            while (true) {
                int symbol = decodeSymbol(dc, in, cumulativeFreq);
                if (symbol == EOF) {
                    break;
                }
                out.write(symbolToChar.get(symbol));
                model.updateModel(symbol);
            }
        } finally {
            model.finishDecode();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private int decodeSymbol(DecodeContext dc, BitInputStream in, long[] cumulativeFreq) {
        long range = (dc.high-dc.low)+1;
        long cumulative = (((dc.decodeValue-dc.low)+1)*cumulativeFreq[0]-1)/range;
        int symbol;
        for (symbol = 1; cumulativeFreq[symbol] > cumulative; symbol++);
        dc.high = (dc.low + (range * cumulativeFreq[symbol-1])/cumulativeFreq[0]-1);
        dc.low  = (dc.low + (range * cumulativeFreq[symbol])/cumulativeFreq[0]);
        while (true) {
            if (dc.high < half) {
                //do nothing
            } else if (dc.low >= half) {
                dc.decodeValue -= half;
                dc.low -= half;
                dc.high -= half;
            } else if (dc.low >= firstQtr && dc.high < thirdQtr) {
                dc.decodeValue -= firstQtr;
                dc.low -= firstQtr;
                dc.high -= firstQtr;
            } else {
                break;
            }
            dc.low = 2 * dc.low;
            dc.high = 2 * dc.high + 1;
            dc.decodeValue = 2 * dc.decodeValue + in.read();
        }
        return symbol;
    }

    private int getMaxGarbageBits() {
        return codeValueBits - 2;
    }

    /** Wrap InputStream so we don't have checked exceptions. **/
    private static class InputStreamX implements AutoCloseable {
        private final InputStream in;
        
        public InputStreamX(InputStream in) {
            this.in = in;
        }
        
        public int read() {
            try {
                return in.read();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @Override
        public void close() {
            try {
                this.in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Wrap OutputStream so we don't have checked exceptions. **/
    private static class OutputStreamX implements AutoCloseable {
        private final OutputStream out;
        
        public OutputStreamX(OutputStream out) {
            this.out = out;
        }
        
        public void write(int b) {
            try {
                out.write(b);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @Override
        public void close() {
            try {
                this.out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static class EncodeContext {
        public EncodeContext(BitOutputStream bos) {
            this.out = bos;
        }
        public final BitOutputStream out;
        public long low = 0;
        public long high = topValue; 
        public int bitsToFollow = 0;
    }

    private static class DecodeContext {
        public long low = 0;
        public long high = topValue;
        public long decodeValue = 0;
    }
}
