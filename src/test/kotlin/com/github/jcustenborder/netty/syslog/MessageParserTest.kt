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

import com.github.jcustenborder.netty.syslog.MessageParserTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

abstract class MessageParserTest<P : MessageParser?> {
    protected var parser: P? = null
    @BeforeEach
    fun setup() {
        parser = createParser()
    }

    fun assertMessage(expected: SyslogMessage?, actual: SyslogMessage?) {
        if (null != expected) {
            Assertions.assertNotNull(actual, "actual should not be null.")
        } else {
            Assertions.assertNull(actual, "actual should be null.")
            return
        }
        Assertions.assertEquals(expected.facility, actual!!.facility, "facility should match.")
        Assertions.assertEquals(expected.level, actual.level, "level should match.")
        Assertions.assertEquals(expected.remoteAddress, actual.remoteAddress, "remoteAddress should match.")
        Assertions.assertEquals(expected.date, actual.date, "date should match.")
        Assertions.assertEquals(expected.rawMessage, actual.rawMessage, "rawMessage should match.")
        Assertions.assertEquals(
            expected.deviceEventClassId,
            actual.deviceEventClassId,
            "deviceEventClassId does not match."
        )
        Assertions.assertEquals(expected.deviceProduct, actual.deviceProduct, "deviceProduct does not match.")
        Assertions.assertEquals(expected.deviceVendor, actual.deviceVendor, "deviceVendor does not match.")
        Assertions.assertEquals(expected.deviceVersion, actual.deviceVersion, "deviceVersion does not match.")
        Assertions.assertEquals(expected.name, actual.name, "name does not match.")
        Assertions.assertEquals(expected.severity, actual.severity, "severity does not match.")
        Assertions.assertEquals(expected.extension, actual.extension, "extension does not match.")
    }

    protected abstract fun createParser(): P
    protected abstract fun testsPath(): File
    @TestFactory
    fun parse(): Stream<DynamicTest> {
        val testsPath = testsPath()
        return Arrays.stream(testsPath.listFiles { p: File -> p.name.endsWith(".json") }).map { file: File ->
            DynamicTest.dynamicTest(file.name) {
                val testCase = ObjectMapperFactory.INSTANCE!!.readValue(file, TestCase::class.java)

                val actual = parser!!.parse(SyslogRequest(
                    receivedDate = LocalDateTime.now(), rawMessage = testCase.input!!, remoteAddress = InetAddress.getLoopbackAddress()
                ))
                ObjectMapperFactory.INSTANCE.writeValue(file, testCase)
                Assertions.assertNotNull(actual, "actual should not be null.")
                assertMessage(testCase.expected, actual)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MessageParserTest::class.java)
    }
}
