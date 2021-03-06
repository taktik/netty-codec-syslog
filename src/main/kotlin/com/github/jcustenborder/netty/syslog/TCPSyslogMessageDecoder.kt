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
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.time.LocalDateTime

@Sharable
class TCPSyslogMessageDecoder @JvmOverloads constructor(val charset: Charset = Charset.forName("UTF-8")) :
    MessageToMessageDecoder<ByteBuf>() {
    @Throws(Exception::class)
    override fun decode(channelHandlerContext: ChannelHandlerContext, byteBuf: ByteBuf, output: MutableList<Any>) {
        val socketAddress = channelHandlerContext.channel().remoteAddress() as InetSocketAddress
        val rawMessage = byteBuf.toString(charset)
        output.add(
            SyslogRequest(
                receivedDate = LocalDateTime.now(),
                remoteAddress = socketAddress.address,
                rawMessage = rawMessage
            )
        )
    }
}
