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

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class CEFMessageParser : MessageParser() {
    private val matcherCEFPrefix: ThreadLocal<Matcher> = ThreadLocal.withInitial { Pattern.compile("^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+CEF:(?<version>\\d+)\\|(?<data>.*)$", 0).matcher("") }
    private val matcherCEFMain: ThreadLocal<Matcher> = ThreadLocal.withInitial { Pattern.compile("(?<!\\\\)\\|", 0).matcher("") }
    private val matcherCEFExtension: ThreadLocal<Matcher> = ThreadLocal.withInitial { Pattern.compile("(\\w+)=", 0).matcher("") }

    fun splitToList(data: String): List<String?> {
        val result: MutableList<String?> = ArrayList(10)
        val matcherData = matcherCEFMain.get().reset(data)
        var start = 0
        var end = 0
        while (matcherData.find()) {
            end = matcherData.end()
            val part = data.substring(start, end - 1)
            start = end
            result.add(part)
        }
        if (data.length > end) {
            result.add(data.substring(end))
        }
        while(result.size<7) {
            result.add(null)
        }
        return result
    }

    override fun parse(request: SyslogRequest): SyslogMessage? {
        log.trace("parse() - request = '{}'", request)
        val matcherPrefix = matcherCEFPrefix.get().reset(request.rawMessage)
        if (!matcherPrefix.find()) {
            log.trace("parse() - Could not match message. request = '{}'", request)
            return null
        }
        log.trace("parse() - Parsed message as CEF.")
        val groupPriority = matcherPrefix.group("priority")
        val groupDate = matcherPrefix.group("date")
        val groupHost = matcherPrefix.group("host")
        val groupCEFVersion = matcherPrefix.group("version")
        val groupData = matcherPrefix.group("data")
        val priority = if (groupPriority == null || groupPriority.isEmpty()) null else groupPriority.toInt()
        val facility = if (null == priority) null else Priority.facility(priority)
        val level = if (null == priority) null else Priority.level(priority, facility!!)
        val date = parseDate(groupDate) ?: LocalDateTime.now()
        val cefVersion = groupCEFVersion.toInt()

        val tokens = splitToList(groupData).map { it?.replace("\\|", "|") }
        if (log.isTraceEnabled) {
            tokens.forEachIndexed { index, s -> log.trace("parse() - index=$index, token=$s") }
        }

        return SyslogMessage(date = date, remoteAddress = request.remoteAddress, rawMessage = request.rawMessage, type = MessageType.CEF, level = level, version = cefVersion, facility = facility, host = groupHost,
            deviceVendor = tokens[0], deviceProduct = tokens[1], deviceVersion =  tokens[2], deviceEventClassId = tokens[3], name = tokens[4], severity = tokens[5], extension = tokens[6]?.let { parseExtension(it) }
        )
    }

    private fun parseExtension(token: String): Map<String, String> {
        log.trace("parseExtension() - token = '{}'", token)
        val result: MutableMap<String, String> = LinkedHashMap()
        if (token.isEmpty()) {
            return result
        }
        val matcher = matcherCEFExtension.get().reset(token)
        var key: String? = null
        var value: String
        var lastEnd = -1
        var lastStart = -1
        while (matcher.find()) {
            log.trace("parseExtension() - matcher.start() = {}, matcher.end() = {}", matcher.start(), matcher.end())
            if (lastEnd > -1) {
                value = token.substring(lastEnd, matcher.start()).trim { it <= ' ' }
                key?.let { result[it] = value }
                log.trace("parseExtension() - key='{}' value='{}'", key, value)
            }
            key = matcher.group(1)
            lastStart = matcher.start()
            lastEnd = matcher.end()
        }
        if (lastStart > -1 && !result.containsKey(key)) {
            value = token.substring(lastEnd).trim { it <= ' ' }
            key?.let { result[it] = value }
            log.trace("parseExtension() - key='{}' value='{}'", key, value)
        }
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(CEFMessageParser::class.java)
    }
}
