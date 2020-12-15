/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.syslog

import com.github.jcustenborder.netty.syslog.Priority.facility
import com.github.jcustenborder.netty.syslog.Priority.level
import com.github.jcustenborder.netty.syslog.Priority.priority
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.*
import java.util.stream.Stream

class PriorityTest {
    @TestFactory
    fun level(): Stream<DynamicTest> {
        return testCases!!.stream()
            .map { t: TestCase ->
                DynamicTest.dynamicTest(t.toString()) {
                    val level = level(t.priority, t.facility)
                    Assertions.assertEquals(t.level, level)
                }
            }
    }

    @TestFactory
    fun facility(): Stream<DynamicTest> {
        return testCases!!.stream()
            .map { t: TestCase ->
                DynamicTest.dynamicTest(t.toString()) {
                    val facility = facility(t.priority)
                    Assertions.assertEquals(t.facility, facility)
                }
            }
    }

    @TestFactory
    fun priority(): Stream<DynamicTest> {
        return testCases!!.stream()
            .map { t: TestCase ->
                DynamicTest.dynamicTest(t.toString()) {
                    val actual = priority(t.level, t.facility)
                    Assertions.assertEquals(t.priority, actual)
                }
            }
    }

    class TestCase(val priority: Int, val facility: Int, val level: Int) {
        override fun toString(): String {
            return String.format("priority=%s facility=%s level=%s", priority, facility, level)
        }
    }

    companion object {
        var testCases: List<TestCase>? = null
        @BeforeAll
        fun before() {
            testCases = Arrays.asList(
                priority(13, 1, 5),
                priority(46, 5, 6),
                priority(30, 3, 6),
                priority(86, 10, 6),
                priority(0, 0, 0)
            )
        }

        fun priority(expected: Int, facility: Int, level: Int): TestCase {
            return TestCase(expected, facility, level)
        }
    }
}
