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
package org.mersic.arithmeticcoder.model;

import org.mersic.arithmeticcoder.ArithmeticCoder;

/**
 * By definition AdaptiveModel is not threadsafe. The model must maintain state to adapt
 * to the symbols seen, the same instance cannot be used concurrently by multiple threads.
 * <p>
 * This AdaptiveModel makes no assumptions about the frequency of the symbols in the data 
 * to be encoded. After each symbol is encoded the cumulativeFrequency of symbols is updated.
 * This will affect the arithmetic coder by making more frequent symbols take less space
 * to encode. Once maxFreq symbols is reached, the cumulative frequencies are halved, and 
 * we continue encoding.
 * <p>
 * maxFreq has a default size of 2^28 - 1 = 268,435,455. However, if you believe the 
 * distribution of your data would benefit from a shorter 'memory', you can set a smaller 
 * maxFreq. That is, if the distribution of symbols is likely to change over shorter
 * periods than 268MM symbols, then you should pick a smaller maxFreq size. 
 * <p>
 * The AdaptiveModel is not threadsafe. A new AdaptiveModel should be created for each
 * ArithmeticCoder.encode() and ArithmeticCoder.decode().
 */
public final class AdaptiveModel extends ModelAdapter {

    private STATE state = STATE.INITIAL;
    private final long[] freq;
    private final long maxFreq;
    private final long[] cumulativeFreq;

    public AdaptiveModel(Builder builder) {
        super(builder.chars);
        this.freq        = new long[numSymbols + 1];
        this.cumulativeFreq = new long[numSymbols + 1];
        this.maxFreq = builder.maxFreq;
        
        for (int i = 0; i <= numSymbols; i++) {
            freq[i] = 1;
            cumulativeFreq[i] = numSymbols-i;
        }
        freq[0] = 0;
    }
    
    @Override
    public long[] getCumulativeFreq() {
        return this.cumulativeFreq;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void updateModel(int symbol) {
        if (cumulativeFreq[0] == maxFreq) {
            int cumulative = 0;
            for (int i = numSymbols; i >= 0; i--) {
                freq[i] = (freq[i]+1)/2;
                cumulativeFreq[i] = cumulative;
                cumulative += freq[i];
            }
        }
        int i = symbol;
        for (; freq[i] == freq[i-1]; i--);
        if (i < symbol) {
            //symbol is more frequent than the ith symbol (after freq[i] is updated below), swap them.
            int chI = symbolToChar.get(i);
            int chSymbol = symbolToChar.get(symbol);
            symbolToChar.put(i, chSymbol);
            symbolToChar.put(symbol, chI);
            charToSymbol.put(chI, symbol);
            charToSymbol.put(chSymbol, i);
        }
        freq[i]++;        //Update the freq of the ith symbol
        while (i > 0) {   //now update all the cumulative freq
            i -= 1;       //from i-1 down to 0.
            cumulativeFreq[i]++;
        }
    }

    @Override
    public void startEncode() {
        if (state != STATE.INITIAL) {
            throw new IllegalStateException("Cannot reuse. Create a new instance of AdaptiveModel");
        } else {
            state = STATE.INUSE;
        }
    }

    @Override
    public void finishEncode() {
        state = STATE.DONE;
    }

    @Override
    public void startDecode() {
        if (state != STATE.INITIAL) {
            throw new IllegalStateException("Cannot reuse. Create a new instance of AdaptiveModel");
        } else {
            state = STATE.INUSE;
        }
    }

    @Override
    public void finishDecode() {
        state = STATE.DONE;
    }

    public static class Builder {
        private static final int[] defaultChars = new int[256];
        private int[] chars = defaultChars;
        private long maxFreq  = (1 << (ArithmeticCoder.codeValueBits-2)) - 1;  //(2^codeValueBits) - 1 == 268,435,455 (when codeValueBits = 30)

        static {
            for (int i = 0; i < 256; i++) {
                defaultChars[i] = i;
            }
        }

        /**
         * An array of all the characters that will need to be encoded. The default is all
         * 256 UTF-8 characters. This is useful if you know your input is limited to a subset
         * of UTF-8 characters.
         * 
         * @param chars Array of characters that will be encoded or decoded.
         * @return Builder
         */
        public Builder chars(int[] chars) {
            this.chars = chars;
            return this;
        }

        /**
         * If you believe your data will change its cumulative frequency over time, set a smaller
         * value here. The default is a very long 'memory'. (ie frequencies are halved every 
         * 268MM symbols.)
         * 
         * @param maxFreq The maximum number of symbols seen before cumulative freq is halved.
         * @return Builder
         */
        public Builder maxFreq(int maxFreq) {
            if (maxFreq > this.maxFreq) {
                throw new IllegalArgumentException("maxFreq must be less than or equal to " + this.maxFreq);
            }
            this.maxFreq = maxFreq;
            return this;
        }
        
        public AdaptiveModel build() {
            return new AdaptiveModel(this);
        }
    }
    
    private enum STATE {
        INITIAL,
        INUSE,
        DONE
    }
    
}

