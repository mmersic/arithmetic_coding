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

import org.junit.jupiter.api.Test;
import org.mersic.arithmeticcoder.model.AdaptiveModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRandom {
    
    private static final SecureRandom R = new SecureRandom();
    
    @Test 
    public void test_random() {
        for (int i = 0; i < 10; i++) 
            assertTrue(testRandom(new ArithmeticCoder(), R.nextInt(200000)));
    }
    
    
    public static boolean testRandom(ArithmeticCoder ae, int size) {
        byte[] plainInput = generateInput(size);
        int[] chars = new int[] {'0','1', '2', '3', '4', '5', '6', '7', '8', '9'};
        ByteArrayInputStream bis = new ByteArrayInputStream(plainInput);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ae.encode(new AdaptiveModel.Builder().chars(chars).build(), bis, bos);
        bis = new ByteArrayInputStream(bos.toByteArray());
        bos = new ByteArrayOutputStream();
        ae.decode(new AdaptiveModel.Builder().chars(chars).build(), bis, bos);
        return Objects.deepEquals(plainInput, bos.toByteArray());        
    }
    
    public static byte[] generateInput(int numBytes) {
        byte[] b = new byte[numBytes];
        int zero = '0';
        for (int i = 0; i < numBytes; i++) {
            b[i] = (byte) (zero + R.nextInt(10));
        }
        return b;
    }
}
