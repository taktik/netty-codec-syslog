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
import java.nio.charset.Charset

internal object EncoderHelper {
    val CHARSET = Charset.forName("UTF-8")
    val LESS_THAN = "<".toByteArray(CHARSET)
    val GREATER_THAN = ">".toByteArray(CHARSET)
    val LEFT_SQUARE = "[".toByteArray(CHARSET)
    val RIGHT_SQUARE = "]".toByteArray(CHARSET)
    val SPACE = " ".toByteArray(CHARSET)
    val EQUALS = "=".toByteArray(CHARSET)
    fun appendPriority(buffer: ByteBuf, message: SyslogMessage) {
        if (null != message.facility && null != message.level) {
            val priority = Priority.priority(message.level, message.facility)
            buffer.writeBytes(LESS_THAN)
            buffer.writeCharSequence(priority.toString(), CHARSET)
            buffer.writeBytes(GREATER_THAN)
        }
    }
}
