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
import io.netty.handler.codec.MessageToMessageDecoder
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.time.LocalDateTime
import java.util.regex.Matcher
import java.util.regex.Pattern

class RFC5424MessageParser : MessageParser() {
    private val matcherThreadLocal: ThreadLocal<Matcher> = ThreadLocal.withInitial {
        Pattern.compile("^<(?<priority>\\d+)>(?<version>\\d{1,3})\\s*(?<date>[0-9:+-TZ]+)\\s*(?<host>\\S+)\\s*(?<appname>\\S+)\\s*(?<procid>\\S+)\\s*(?<msgid>\\S+)\\s*(?<structureddata>(-|\\[.+\\]))\\s*(?<message>.+)$").matcher("")
    }
    override fun parse(request: SyslogRequest): SyslogMessage? {
        log.trace("parse() - request = '{}'", request)
        val matcher = matcherThreadLocal.get().reset(request.rawMessage)
        if (!matcher.find()) {
            log.trace("parse() - Could not match message. request = '{}'", request)
            return null
        }
        log.trace("parse() - Successfully matched message")
        val groupPriority = matcher.group("priority")
        val groupVersion = matcher.group("version")
        val groupDate = matcher.group("date")
        val groupHost = matcher.group("host")
        val groupAppName = matcher.group("appname")
        val groupProcID = matcher.group("procid")
        val groupMessageID = matcher.group("msgid")
        val groupStructuredData = matcher.group("structureddata")
        val groupMessage = matcher.group("message")
        val priority = groupPriority.toInt()
        val facility = Priority.facility(priority)
        val date = parseDate(groupDate) ?: LocalDateTime.now()
        val level = Priority.level(priority, facility)
        val version = groupVersion.toInt()
        val appName = nullableString(groupAppName)
        val procID = nullableString(groupProcID)
        val messageID = nullableString(groupMessageID)
        val structuredData = parseStructuredData(groupStructuredData)
        return SyslogMessage(
            type = MessageType.RFC5424,
            rawMessage = request.rawMessage,
            remoteAddress = request.remoteAddress,
            date = date,
            host = groupHost,
            level = level,
            facility = facility,
            message = groupMessage,
            version = version,
            processId = procID,
            messageId = messageID,
            structuredData = structuredData,
            appName = appName)

    }

    companion object {
        private val log = LoggerFactory.getLogger(RFC5424MessageParser::class.java)
    }
}
