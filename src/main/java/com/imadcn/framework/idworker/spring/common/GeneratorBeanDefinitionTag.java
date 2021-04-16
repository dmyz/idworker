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
package com.imadcn.framework.idworker.spring.common;

/**
 * idworker:application 配置TAF
 * 
 * @author imadcn
 * @since 1.0.0
 */
public final class GeneratorBeanDefinitionTag extends BaseBeanDefinitionTag {

    private GeneratorBeanDefinitionTag() {
    }

    public static final String GROUOP = "group";

    public static final String REGISTRY_CENTER_REF = "registry-center-ref";

    /**
     * 生成策略
     * 
     * @since 1.2.0
     */
    public static final String STRATEGY = "strategy";

    /**
     * 低并发模式
     * 
     * @since 1.2.5
     */
    @Deprecated
    public static final String LOW_CONCURRENCY = "low-concurrency";

    /**
     * 注册信息本地缓存文件地址
     * 
     * @since 1.3.0
     */
    public static final String REGISTRY_FILE = "registry-file";

    /**
     * ZK节点是否持久化存储
     * 
     * @since 1.4.0
     */
    public static final String DURABLE = "durable";
}
