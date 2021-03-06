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

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.time.format.DateTimeFormatter

class MessageEncoder(val cefDateFormat: DateTimeFormatter) : MessageToMessageEncoder<SyslogMessage>() {
    val charset: Charset = Charset.forName("UTF-8")
    val cef: ByteArray = "CEF:0".toByteArray(charset)
    val pipe: ByteArray = "|".toByteArray(charset)

    private fun encodeCEF(context: ChannelHandlerContext, message: SyslogMessage, output: MutableList<Any>) {
        log.trace("encode() - message = {}", message)
        val buffer = context.alloc().buffer()
        EncoderHelper.appendPriority(buffer, message)
        buffer.writeCharSequence(cefDateFormat.format(message.date), charset)
        buffer.writeBytes(EncoderHelper.SPACE)
        buffer.writeCharSequence(message.host, charset)
        buffer.writeBytes(EncoderHelper.SPACE)
        buffer.writeBytes(cef)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.deviceVendor, charset)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.deviceProduct, charset)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.deviceVersion, charset)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.deviceEventClassId, charset)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.name, charset)
        buffer.writeBytes(pipe)
        buffer.writeCharSequence(message.severity, charset)
        buffer.writeBytes(pipe)
        var index = 0
        for ((key, value) in message.extension?.entries ?: setOf()) {
            if (index > 0) {
                buffer.writeBytes(EncoderHelper.SPACE)
            }
            buffer.writeCharSequence(key, charset)
            buffer.writeBytes(EncoderHelper.EQUALS)
            buffer.writeCharSequence(value, charset)
            index++
        }
        output.add(buffer)
    }

    private fun encodeRFC3164(context: ChannelHandlerContext, message: SyslogMessage, output: MutableList<Any>) {
        log.trace("encode() - message = {}", message)
        val buffer = context.alloc().buffer()
        EncoderHelper.appendPriority(buffer, message)
        buffer.writeCharSequence(message.date.format(cefDateFormat), charset)
        buffer.writeCharSequence(" ", charset)
        buffer.writeCharSequence(message.host, charset)
        buffer.writeCharSequence(" ", charset)
        buffer.writeCharSequence(message.tag, charset)
        if (null != message.processId) {
            buffer.writeCharSequence("[", charset)
            buffer.writeCharSequence(message.processId.toString(), charset)
            buffer.writeCharSequence("]", charset)
        }
        buffer.writeCharSequence(": ", charset)
        buffer.writeCharSequence(message.message, charset)
        output.add(buffer)
    }

    private fun encodeRFC5424(context: ChannelHandlerContext, message: SyslogMessage, output: MutableList<Any>) {
        val buffer = context.alloc().buffer()
        EncoderHelper.appendPriority(buffer, message)
        if (null != message.version) {
            buffer.writeCharSequence(message.version.toString(), charset)
        }
        buffer.writeBytes(EncoderHelper.SPACE)
        buffer.writeCharSequence(message.date.format(cefDateFormat), charset)
        buffer.writeCharSequence(" ", charset)
        buffer.writeCharSequence(message.host, charset)
        buffer.writeCharSequence(" ", charset)
        if (null != message.appName) {
            buffer.writeCharSequence(message.appName, charset)
        }
        if (null != message.processId) {
            buffer.writeCharSequence(message.processId, charset)
        } else {
            buffer.writeCharSequence(" -", charset)
        }
        if (null != message.messageId) {
            buffer.writeCharSequence(message.messageId, charset)
        } else {
            buffer.writeCharSequence(" -", charset)
        }
        buffer.writeCharSequence(" - ", charset)


//    buffer.writeCharSequence(message.tag(), this.charset);
//
//    if (null != message.processId()) {
//      buffer.writeCharSequence("[", this.charset);
//      buffer.writeCharSequence(message.processId().toString(), this.charset);
//      buffer.writeCharSequence("]", this.charset);
//    }
        buffer.writeCharSequence(message.message, charset)
        output.add(buffer)
    }

    public override fun encode(context: ChannelHandlerContext, message: SyslogMessage, list: MutableList<Any>) {
        when (message.type) {
            MessageType.CEF -> encodeCEF(context, message, list)
            MessageType.RFC3164 -> encodeRFC3164(context, message, list)
            MessageType.RFC5424 -> encodeRFC5424(context, message, list)
            else -> {
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MessageEncoder::class.java)
    }

}
