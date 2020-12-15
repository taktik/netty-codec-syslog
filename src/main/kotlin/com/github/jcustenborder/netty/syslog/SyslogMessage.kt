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
    val date: LocalDateTime,
    val remoteAddress: InetAddress,
    val rawMessage: String,
    val type: MessageType,
    val level: Int? = null,
    val version: Int? = null,
    val facility: Int? = null,
    val host: String? = null,
    val message: String? = null,
    val processId: String? = null,
    val tag: String? = null,
    val messageId: String? = null,
    val appName: String? = null,
    val structuredData: List<StructuredData>? = null,
    val deviceVendor: String? = null,
    val deviceProduct: String? = null,
    val deviceVersion: String? = null,
    val deviceEventClassId: String? = null,
    val name: String? = null,
    val severity: String? = null,
    val extension: Map<String, String>? = null
) {
    data class StructuredData(val id: String? = null, val structuredDataElements: Map<String, String?>? = null)
}
