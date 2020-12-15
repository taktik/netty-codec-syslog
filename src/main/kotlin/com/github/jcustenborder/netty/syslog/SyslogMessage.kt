/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.syslog

import java.net.InetAddress
import java.time.LocalDateTime

data class SyslogMessage(
    override val date: LocalDateTime,
    override val remoteAddress: InetAddress,
    override val rawMessage: String,
    override val type: MessageType,
    override val level: Int? = null,
    override val version: Int? = null,
    override val facility: Int? = null,
    override val host: String? = null,
    override val message: String? = null,
    override val processId: String? = null,
    override val tag: String? = null,
    override val messageId: String? = null,
    override val appName: String? = null,
    override val structuredData: List<Message.StructuredData>? = null,
    override val deviceVendor: String? = null,
    override val deviceProduct: String? = null,
    override val deviceVersion: String? = null,
    override val deviceEventClassId: String? = null,
    override val name: String? = null,
    override val severity: String? = null,
    override val extension: Map<String, String>? = null
) : Message {
    data class StructuredData(override val id: String? = null, override val structuredDataElements: Map<String, String?>? = null) : Message.StructuredData
}
