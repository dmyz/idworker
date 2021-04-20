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
package com.imadcn.framework.idworker.spring.common;

/**
 * idworker:zookeeper 配置TAG
 * 
 * @author imadcn
 * @since 1.0.0
 */
public final class ZookeeperBeanDefinitionTag {

    private ZookeeperBeanDefinitionTag() {
    }

    public static final String SERVER_LISTS = "server-lists";

    public static final String NAMESPACE = "namespace";

    public static final String BASE_SLEEP_TIME_MS = "base-sleep-time-milliseconds";

    public static final String MAX_SLEEP_TIME_MS = "max-sleep-time-milliseconds";

    public static final String MAX_RETRIES = "max-retries";

    public static final String SESSION_TIMEOUT_MS = "session-timeout-milliseconds";

    public static final String CONNECTION_TIMEOUT_MS = "connection-timeout-milliseconds";

    public static final String DIGEST = "digest";
}
