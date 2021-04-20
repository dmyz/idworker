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
package com.imadcn.framework.idworker.registry.redis;

import org.springframework.data.redis.core.RedisTemplate;

import com.imadcn.framework.idworker.config.RedisConfiguration;
import com.imadcn.framework.idworker.registry.AbstractCoordinatorRegistryCenter;

/**
 * Redis注册中心
 * 
 * @author imadcn
 * @since 2.0.0
 */
public abstract class RedisRegistryCenter extends AbstractCoordinatorRegistryCenter {

    private RedisConfiguration redisConfig;

    private RedisTemplate<String, Object> redisTemplate;
}
