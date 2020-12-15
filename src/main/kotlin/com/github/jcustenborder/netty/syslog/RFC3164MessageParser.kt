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

import java.lang.ThreadLocal
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.regex.Matcher
import java.util.regex.Pattern

class RFC3164MessageParser : MessageParser() {
    private val matcherThreadLocal: ThreadLocal<Matcher> = ThreadLocal.withInitial {
        Pattern.compile("^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+((?<tag>[^\\[\\s\\]]+)(\\[(?<procid>\\d+)\\])?:)*\\s*(?<message>.+)$").matcher("")
    }
    override fun parse(request: SyslogRequest): SyslogMessage? {
        log.trace("parse() - request = '{}'", request)
        val matcher = matcherThreadLocal!!.get()!!.reset(request.rawMessage)
        if (!matcher.find()) {
            log.trace("parse() - Could not match message. request = '{}'", request)
            return null
        }
        log.trace("parse() - Parsed message as RFC 3164")
        val groupPriority = matcher.group("priority")
        val groupDate = matcher.group("date")
        val groupHost = matcher.group("host")
        val groupMessage = matcher.group("message")
        val groupTag = matcher.group("tag")
        val groupProcId = matcher.group("procid")
        val processId = if (groupProcId == null || groupProcId.isEmpty()) null else groupProcId
        val priority = if (groupPriority == null || groupPriority.isEmpty()) null else groupPriority.toInt()
        val facility = if (null == priority) null else Priority.facility(priority)
        val level = if (null == priority) null else Priority.level(priority, facility!!)
        val date = parseDate(groupDate) ?: LocalDateTime.now()
        return SyslogMessage(
            type = MessageType.RFC3164,
            rawMessage = request.rawMessage,
            remoteAddress = request.remoteAddress,
            date = date,
            host = groupHost,
            level = level,
            facility = facility,
            message = groupMessage,
            tag = groupTag,
            processId = processId
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(RFC3164MessageParser::class.java)
    }
}
