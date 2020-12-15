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

import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import org.graylog2.syslog4j.SyslogIF
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class SyslogIT {
    protected abstract fun port(): Int
    protected abstract fun syslogIF(): SyslogIF
    @Throws(InterruptedException::class)
    protected abstract fun setupServer(
        bossGroup: EventLoopGroup?,
        workerGroup: EventLoopGroup?,
        handler: TestSyslogMessageHandler?
    ): ChannelFuture?

    private var bossGroup: EventLoopGroup = NioEventLoopGroup() // (1)
    private var workerGroup: EventLoopGroup = NioEventLoopGroup(4)
    protected var channelFuture: ChannelFuture? = null
    protected var handler: TestSyslogMessageHandler? = null
    @BeforeEach
    @Throws(InterruptedException::class)
    fun setup() {
        bossGroup = NioEventLoopGroup()
        workerGroup = NioEventLoopGroup()
        handler = TestSyslogMessageHandler()
        channelFuture = setupServer(bossGroup, workerGroup, handler)
        Thread.sleep(500)
    }

    @Test
    @Throws(InterruptedException::class)
    fun roundtrip() {
        val count = 100
        val syslogIF = syslogIF()
        for (i in 0 until count) {
            syslogIF.info("foo")
        }
        syslogIF.flush()
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 5000 && handler!!.messages.size < count) {
            Thread.sleep(100)
        }
        Assertions.assertEquals(count, handler!!.messages.size)
    }

    @AfterEach
    @Throws(InterruptedException::class)
    fun close() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        channelFuture!!.channel().closeFuture().sync()
    }
}
