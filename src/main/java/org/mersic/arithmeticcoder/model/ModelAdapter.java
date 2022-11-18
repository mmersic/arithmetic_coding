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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation of charToSymbol and symbolToChar where
 * the array of chars is in initial symbol order.  Ie chars[0] is 
 * symbol 1. chars[1] is symbol 2. And symbol chars.len + 1 is
 * the EOFSymbol.
 * <p> 
 * Why do we start at symbol 1? Because the cumulativeFrequency 
 * array will use position 0 ('symbol' 0) for the cumulativeFrequency 
 * of all the symbols.
 * <p>
 * The cumulativeFrequency array is not defined here because
 * any interesting Model will likely have a custom cumulativeFrequency.
 * <p>
 * This ModelAdapter is not threadsafe unless the values contained in
 * charToSymbol, symbolToChar, EOFSymbol, and numSymbols never change
 * due to updateModel calls.
 */
public abstract class ModelAdapter implements Model {

    protected final Map<Integer, Integer> charToSymbol;
    protected final Map<Integer, Integer> symbolToChar;
    protected final int EOFSymbol;
    protected final int numSymbols;

    public ModelAdapter(int[] chars) {
        this.charToSymbol = new HashMap<>();
        this.symbolToChar = new HashMap<>();
        this.EOFSymbol   = chars.length + 1;
        this.numSymbols  = chars.length + 1;
        
        for (int i = 0; i < chars.length; i++) {
            charToSymbol.put(chars[i], i+1);
            symbolToChar.put(i+1, chars[i]);
        }
    }

    @Override
    public Map<Integer, Integer> getCharToSymbol() {
        return this.charToSymbol;
    }

    @Override
    public Map<Integer, Integer> getSymbolToChar() { return this.symbolToChar; }

    @Override
    public int getEOFSymbol() {
        return this.EOFSymbol;
    }
    
}
