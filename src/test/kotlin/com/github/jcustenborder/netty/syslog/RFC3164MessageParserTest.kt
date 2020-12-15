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

import com.github.jcustenborder.netty.syslog.RFC3164MessageParserTest
import org.slf4j.LoggerFactory
import java.io.File

class RFC3164MessageParserTest : MessageParserTest<RFC3164MessageParser>() {
    override fun createParser(): RFC3164MessageParser {
        return RFC3164MessageParser()
    }

    override fun testsPath(): File {
        return File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc3164")
    }

    companion object {
        private val log = LoggerFactory.getLogger(RFC3164MessageParserTest::class.java)
    }
}
