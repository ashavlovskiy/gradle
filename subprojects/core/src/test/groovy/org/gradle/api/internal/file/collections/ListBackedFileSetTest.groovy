/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.file.collections

class ListBackedFileSetTest extends AbstractSetBackedFileSetTest {

    @Override
    AbstractSetBackedFileSet unit(File... files) {
        return new ListBackedFileSet(files)
    }

    def "can add to files collection"() {
        given:
        def testFile = new File('test-file')
        def fileSet = unit()

        when:
        fileSet.files.add(testFile)

        then:
        fileSet.files.size() == 1
        fileSet.files.contains(testFile)
    }
}
