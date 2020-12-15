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
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.util.ByteProcessor
import io.netty.util.CharsetUtil

class SyslogFrameDecoder(maxLength: Int) : LineBasedFrameDecoder(maxLength, true, false) {
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, b: ByteBuf): Any? {
        val buffer = b.retain()
        val lengthIndex = buffer.forEachByte(INTEGER)
        val digitCount = lengthIndex - buffer.readerIndex()
        return if (digitCount > 0) {
            buffer.markReaderIndex()
            val frameLength = buffer.getCharSequence(buffer.readerIndex(), digitCount, CharsetUtil.UTF_8).toString()
            buffer.skipBytes(digitCount + 1)
            val length = frameLength.toInt()
            if (b.readerIndex() + length > b.writerIndex()) {
                buffer.resetReaderIndex()
                null
            } else
                buffer.slice(digitCount + 1, length)
        } else {
            super.decode(ctx, buffer)
        }
    }

    companion object {
        val INTEGER = ByteProcessor { b: Byte -> b >= 48.toByte() && b <= 57.toByte() }
    }
}
