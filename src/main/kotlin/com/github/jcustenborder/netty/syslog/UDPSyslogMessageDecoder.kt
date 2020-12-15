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

import java.nio.charset.Charset
import com.github.jcustenborder.netty.syslog.EncoderHelper
import io.netty.buffer.ByteBuf
import kotlin.jvm.JvmOverloads
import java.time.ZoneId
import java.lang.ThreadLocal
import com.github.jcustenborder.netty.syslog.SyslogRequest
import com.github.jcustenborder.netty.syslog.MessageParser.MatcherInheritableThreadLocal
import com.github.jcustenborder.netty.syslog.MessageParser
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.DateTimeException
import com.github.jcustenborder.netty.syslog.Message.StructuredData
import java.lang.InheritableThreadLocal
import java.util.Arrays
import java.net.InetAddress
import io.netty.handler.codec.MessageToMessageEncoder
import kotlin.Throws
import io.netty.channel.ChannelHandlerContext
import com.github.jcustenborder.netty.syslog.CEFMessageParser
import java.util.LinkedHashMap
import com.github.jcustenborder.netty.syslog.MessageKey
import io.netty.handler.codec.LineBasedFrameDecoder
import com.github.jcustenborder.netty.syslog.SyslogFrameDecoder
import io.netty.util.CharsetUtil
import io.netty.util.ByteProcessor
import com.github.jcustenborder.netty.syslog.RFC3164MessageParser
import com.github.jcustenborder.netty.syslog.RFC5424MessageParser
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.SimpleChannelInboundHandler
import com.github.jcustenborder.netty.syslog.SyslogMessageHandler
import java.lang.Runnable
import io.netty.channel.ChannelDuplexHandler
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleState
import com.github.jcustenborder.netty.syslog.SyslogIdleStateHandler
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import java.lang.Exception
import java.net.InetSocketAddress
import java.time.LocalDateTime

@Sharable
class UDPSyslogMessageDecoder @JvmOverloads constructor(val charset: Charset = Charset.forName("UTF-8")) :
    MessageToMessageDecoder<DatagramPacket>() {
    override fun decode(
        channelHandlerContext: ChannelHandlerContext,
        datagramPacket: DatagramPacket,
        output: MutableList<Any>
    ) {
        val rawMessage = datagramPacket.content().toString(charset)
        val inetAddress = datagramPacket.sender().address
        output.add(
            SyslogRequest(
                receivedDate = LocalDateTime.now(),
                rawMessage = rawMessage,
                remoteAddress = inetAddress
            )
        )
    }
}
