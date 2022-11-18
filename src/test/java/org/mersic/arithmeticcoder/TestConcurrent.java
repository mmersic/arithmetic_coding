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

import jdk.incubator.concurrent.StructuredTaskScope;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConcurrent {
    private static final SecureRandom R = new SecureRandom();
    
    @Test
    public void test_concurrent() throws Exception {
        Instant deadline = Instant.now().plus(5, ChronoUnit.SECONDS);
        test_concurrent(new ArithmeticCoder(), deadline, 10, 200000);
    }
    
    public static void test_concurrent(ArithmeticCoder ae, Instant deadline, int count, int maxSize) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Future<Boolean>> fList = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                fList.add(scope.fork(() -> TestRandom.testRandom(ae, R.nextInt(maxSize))));
            }

            scope.joinUntil(deadline);
            scope.throwIfFailed();

            for (Future<Boolean> f : fList) {
                assertTrue(f.get());
            }
        }        
    }
}
