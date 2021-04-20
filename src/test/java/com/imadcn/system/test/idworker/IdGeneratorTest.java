/*
 * Copyright 2013-2021 imadcn.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.imadcn.system.test.idworker;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.imadcn.framework.idworker.generator.IdGenerator;
import com.imadcn.system.test.spring.AbstractZookeeperJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:META-INF/idworker-ctx.xml")
public final class IdGeneratorTest extends AbstractZookeeperJUnit4SpringContextTests {

    @Autowired
    private IdGenerator idGenerator;

    @Test
    public void testGetId() {
        Object object = idGenerator.nextId();
        print(object);
    }

    @Test
    public void testBatchGetId() {
        Object object = idGenerator.nextId(20);
        print(object);
    }

    @Test
    public void testWithInvalidParam() {
        try {
            Object object = idGenerator.nextId(0);
            print(object);
        } catch (Exception e) {
            print(e.getMessage());
        }

        try {
            Object object = idGenerator.nextId(-1);
            print(object);
        } catch (Exception e) {
            print(e.getMessage());
        }

        try {
            Object object = idGenerator.nextId(100_001);
            print(object);
        } catch (Exception e) {
            print(e.getMessage());
        }

    }
}
