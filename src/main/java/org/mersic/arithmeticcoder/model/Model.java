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

import java.util.Map;

/**
 * The arithmetic coder can be implemented with different Models effecting the encode
 * and decode efficiency. In particular, if you know your symbols will have a particular
 * distribution then you can provide an accurate static cumulativeFrequency array that 
 * will provide good encode/decode efficiency. On the other hand, if the cumulativeFrequency
 * of the symbols will change than it might be more optimal to provide a Model with
 * a shorter "memory" of the cumulativeFrequency it has seen.
 * 
 * @see AdaptiveModel
 * 
 */
public interface Model {

    /**
     * Map from characters to symbols.
     * @return char to symbol map
     */
    Map<Integer, Integer> getCharToSymbol();

    /**
     * Map from symbols to characters.
     * @return symbol to char map
     */
    Map<Integer, Integer> getSymbolToChar();

    /**
     * The End of File symbol.
     * Typically, this is numChars + 1.
     * @return end of file symbol
     */
    int getEOFSymbol();

    /**
     * The cumulative frequency of the symbols. The array is indexed on the 
     * value of symbol. The array is ordered from most frequent to least frequent with
     * index 0 reserved for the cumulative frequency of all the symbols.
     * <p>
     * Neighboring values in the cumulative freq array must differ by at least 1. This 
     * is enforced by ArithmeticCoder only at the beginning of the calls to encode
     * and decode. 
     * 
     * @return cumulative freq array
     */
    long[] getCumulativeFreq();

    /**
     * If necessary, update charToSymbol, symbolToChar, and cumulativeFreq based
     * on the symbol just seen. If you have a static cumulativeFrequency array
     * then this method would be implemented as a no-op. 
     * <p>
     * For encode - the updateModel method is called immediately after
     * a character in the input stream is encoded into a symbol.
     * <p>
     * For decode - the updateModel method is called immediately after
     * a symbol is decoded from the input stream.
     *
     * @param symbol The symbol just encoded or decoded.
     * @see AdaptiveModel for an implementation that calculates cumulative frequency seen so far.
     * 
     */
    void updateModel(int symbol);

    /**
     * This is invoked immediately after the ArithmeticCode.encode() method is called.
     */
    void startEncode();

    /**
     * This is invoked immediately before the ArithmeticCode.encode() method returns.
     */
    void finishEncode();

    /**
     * This is invoked immediately after the ArithmeticCode.decode() method is called.
     */
    void startDecode();

    /**
     * This is invoked immediately before the ArithmeticCode.decode() method returns.
     */
    void finishDecode();
}
