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

import com.github.jcustenborder.netty.syslog.Message.StructuredData
import com.github.jcustenborder.netty.syslog.MessageParser
import org.slf4j.LoggerFactory
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class MessageParser @JvmOverloads constructor(private val zoneId: ZoneId = ZoneId.of("UTC")) {
    protected val dateFormats: List<DateTimeFormatter> = Arrays.asList(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,  //This supports
        DateTimeFormatterBuilder()
            .appendPattern("MMM d")
            .optionalStart()
            .appendPattern("[ yyyy]")
            .parseDefaulting(ChronoField.YEAR_OF_ERA, 1)
            .optionalEnd()
            .appendPattern(" HH:mm:ss")
            .toFormatter()
    )
    private val matcherStructuredData: ThreadLocal<Matcher> = ThreadLocal.withInitial { Pattern.compile("\\[([^\\]]+)\\]").matcher("") }
    private val matcherKeyValue: ThreadLocal<Matcher> = ThreadLocal.withInitial { Pattern.compile("(?<key>\\S+)=\"(?<value>[^\"]+)\"|(?<id>\\S+)").matcher("") }

    /**
     * Method is used to parse an incoming syslog message.
     *
     * @param request Incoming syslog request.
     * @return Object to pass along the pipeline. Null if could not be parsed.
     */
    abstract fun parse(request: SyslogRequest): SyslogMessage?

    protected fun nullableString(groupText: String): String? {
        return if (NULL_TOKEN == groupText) null else groupText
    }

    protected fun parseDate(date: String): LocalDateTime? {
        val cleanDate = date.replace("\\s+".toRegex(), " ")
        var result: LocalDateTime? = null
        for (formatter in dateFormats) {
            try {
                val temporal = formatter.parseBest(
                    cleanDate,
                    TemporalQuery<Any> { temporal: TemporalAccessor? -> OffsetDateTime.from(temporal) },
                    TemporalQuery<Any> { temporal: TemporalAccessor? -> LocalDateTime.from(temporal) })
                result = if (temporal is LocalDateTime) {
                    temporal
                } else {
                    (temporal as OffsetDateTime).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
                }
                /*
        The parser will output dates that do not have a year. If this happens we default the year
        to 1 AD which I'm pretty sure there were no computers. This means that the sender was a lazy
        ass and didn't sent a date. This is easy to detect so we set it to the current date.
         */if (result!!.getLong(ChronoField.YEAR_OF_ERA) == 1L) {
                    result = result.withYear(LocalDateTime.now(zoneId).year)
                }
                break
            } catch (e: DateTimeException) {
                log.trace("parseDate() - Could not parse '{}' with '{}'", cleanDate, formatter.toString())
            }
        }
        if (null == result) {
            log.error("Could not parse date '{}'", cleanDate)
        }
        return result
    }

    protected fun parseStructuredData(structuredData: String?): List<StructuredData> {
        log.trace("parseStructuredData() - structuredData = '{}'", structuredData)
        val matcher = matcherStructuredData.get().reset(structuredData)
        val result: MutableList<StructuredData> = LinkedList()
        while (matcher.find()) {
            val input = matcher.group(1)
            log.trace("parseStructuredData() - input = '{}'", input)

            var id: String? = null
            var dataElements = mutableMapOf<String, String?>()

            val kvpMatcher = matcherKeyValue.get().reset(input)
            while (kvpMatcher.find()) {
                val key = kvpMatcher.group("key")
                val value = kvpMatcher.group("value")
                val gid = kvpMatcher.group("id")
                if (null != gid && gid.isNotEmpty()) {
                    log.trace("parseStructuredData() - id='{}'", id)
                    id = gid
                } else {
                    log.trace("parseStructuredData() - key='{}' value='{}'", key, value)
                    dataElements[key] = value
                }
            }
            result.add(SyslogMessage.StructuredData(id, dataElements))
        }
        return result
    }

    internal class MatcherInheritableThreadLocal(private val pattern: Pattern) : InheritableThreadLocal<Matcher>() {
        override fun initialValue(): Matcher {
            return pattern.matcher("")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MessageParser::class.java)
        private const val NULL_TOKEN = "-"
    }
}
