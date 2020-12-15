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

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.mockito.Mockito
import java.io.File
import java.nio.charset.Charset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Stream

abstract class MessageEncoderTest {
    var encoder: MessageEncoder? = null
    @BeforeEach
    fun setup() {
        encoder = MessageEncoder(DateTimeFormatter.ofPattern("MMM d HH:mm:ss"))
    }

    protected abstract fun testsPath(): File
    @TestFactory
    fun encode(): Stream<DynamicTest> {
        val testsPath = testsPath()
        return Arrays.stream(testsPath.listFiles()).map { file: File ->
            DynamicTest.dynamicTest(file.name) {
                val testCase = ObjectMapperFactory.INSTANCE!!.readValue(file, TestCase::class.java)
                val context = Mockito.mock(ChannelHandlerContext::class.java)
                Mockito.`when`(context.alloc()).thenReturn(ByteBufAllocator.DEFAULT)
                val output: MutableList<Any> = ArrayList()
                encoder!!.encode(context, testCase.expected!!, output)
                Assertions.assertFalse(output.isEmpty())
                val actual = output[0] as ByteBuf
                Assertions.assertNotNull(actual, "actual should not be null.")
                val a = actual.toString(Charset.forName("UTF-8")).replace("\\s+".toRegex(), " ")
                Assertions.assertEquals(testCase.input!!.replace("\\s+".toRegex(), " "), a)
            }
        }
    }
}
