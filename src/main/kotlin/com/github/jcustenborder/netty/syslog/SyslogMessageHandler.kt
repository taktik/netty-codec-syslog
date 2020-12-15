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

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Sharable
class SyslogMessageHandler @JvmOverloads constructor(
    private val parsers: List<MessageParser> =
        listOf(
            CEFMessageParser(),
            RFC5424MessageParser(),
            RFC3164MessageParser()
        )
) : SimpleChannelInboundHandler<SyslogRequest>() {
    @Throws(Exception::class)
    override fun channelRead0(context: ChannelHandlerContext, request: SyslogRequest) {
        log.trace("channelRead0() - request = '{}'", request)
        context.executor().submit {
            for (parser in parsers) {
                val result: Any? = parser.parse(request)
                if (null != result) {
                    log.trace("channelRead0() - add result = '{}'", result)
                    context.fireChannelRead(result)
                    return@submit
                }
            }
            log.warn("decode() - Could not parse message. request = '{}'", request)
            val unparseableMessage = SyslogMessage(
                type = MessageType.UNKNOWN,
                date = LocalDateTime.now(),
                rawMessage = request.rawMessage,
                remoteAddress = request.remoteAddress
                )
            context.write(unparseableMessage)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SyslogMessageHandler::class.java)
    }
}
