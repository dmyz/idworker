/*
 * Copyright 2013-2021 imadcn.
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
package com.imadcn.system.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.config.ZookeeperConfiguration;
import com.imadcn.framework.idworker.generator.SnowflakeGenerator;
import com.imadcn.framework.idworker.register.WorkerRegister;
import com.imadcn.framework.idworker.register.zookeeper.ZookeeperWorkerRegister;
import com.imadcn.framework.idworker.registry.zookeeper.ZookeeperRegistryCenter;

public class ProcedureTest {
    
    private static Logger logger = LoggerFactory.getLogger(ProcedureTest.class);

    public static void main(String[] args) {
        try {
            ZookeeperConfiguration configuration = new ZookeeperConfiguration();
            configuration.setServerLists("127.0.0.1:2181");
            configuration.setNamespace("manual_idworker");

            ZookeeperRegistryCenter registryCenter = new ZookeeperRegistryCenter(configuration);

            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            applicationConfiguration.setGroup("manual_group");

            WorkerRegister workerRegister = new ZookeeperWorkerRegister(registryCenter,
                    applicationConfiguration);

            SnowflakeGenerator generator = new SnowflakeGenerator(workerRegister);

            generator.init();
            logger.info("{}", generator.nextId());
            generator.close();

            generator.init();
            logger.info("{}", generator.nextId());
            generator.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
