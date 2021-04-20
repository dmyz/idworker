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
package com.imadcn.framework.idworker.register.redis;

import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.register.AbstractWorkerRegister;
import com.imadcn.framework.idworker.register.NodeInfo;
import com.imadcn.framework.idworker.registry.CoordinatorRegistryCenter;

/**
 * 基于Redis的机器节点注册器
 * 
 * @author imadcn
 * @since 2.0.0
 */
public class RedisWorkerRegister extends AbstractWorkerRegister {

    public RedisWorkerRegister(CoordinatorRegistryCenter regCenter, ApplicationConfiguration applicationConfiguration) {
        super(regCenter, applicationConfiguration);
    }

    @Override
    public long register() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNodeInfo(String key, NodeInfo nodeInfo) {
        // TODO Auto-generated method stub

    }
}
