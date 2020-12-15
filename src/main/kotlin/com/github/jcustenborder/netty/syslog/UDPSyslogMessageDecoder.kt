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
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import java.nio.charset.Charset
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
