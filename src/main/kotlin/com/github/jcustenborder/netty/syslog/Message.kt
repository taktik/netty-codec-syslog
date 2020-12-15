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

import java.net.InetAddress
import java.time.LocalDateTime

/**
 * Represents a standard syslog message.
 */
interface Message {
    /**
     * Date of the message. This is the parsed date from the client.
     *
     * @return Date of the message.
     */
    val date: LocalDateTime

    /**
     * IP Address for the sender of the message.
     *
     * @return Sender IP Address.
     */
    val remoteAddress: InetAddress

    /**
     * Unprocessed copy of the message.
     *
     * @return Unprocessed message.
     */
    val rawMessage: String

    /**
     * @return
     */
    val type: MessageType

    /**
     * Level for the message. Parsed from the message.
     *
     * @return Message Level
     */
    val level: Int?

    /**
     * Version of the message.
     *
     * @return Message version
     */
    val version: Int?

    /**
     * Facility of the message.
     *
     * @return Message facility.
     */
    val facility: Int?

    /**
     * Host of the message. This is the value from the message.
     *
     * @return Message host.
     */
    val host: String?

    /**
     * Message part of the overall syslog message.
     *
     * @return Message part of the overall syslog message.
     */
    val message: String?

    val processId: String?

    /*
  rfc 3164
   */
    val tag: String?

    /*
  rfc 3164
   */
    /*
rfc 5424
 */
    val messageId: String?

    val appName: String?

    val structuredData: List<StructuredData?>?

    /*
  CEF
   */
    val deviceVendor: String?

    /*
  rfc 5424
   */
    val deviceProduct: String?

    val deviceVersion: String?

    val deviceEventClassId: String?

    val name: String?

    val severity: String?

    val extension: Map<String, String>?

    interface StructuredData {
        val id: String?
        val structuredDataElements: Map<String, String?>?
    } /*
  CEF
   */
}
