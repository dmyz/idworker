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
package com.imadcn.framework.idworker.registry;

import java.util.List;

/**
 * 用于协调分布式服务的注册中心.
 * 
 * @author imadcn
 * @since 1.0.0
 */
public interface CoordinatorRegistryCenter extends RegistryCenter {

    /**
     * 获取子节点名称集合.
     * 
     * @param key 键
     * @return 子节点名称集合
     */
    List<String> getChildrenKeys(String key);

    /**
     * 获取子节点数量.
     *
     * @param key 键
     * @return 子节点数量
     */
    int getNumChildren(String key);

    /**
     * 持久化临时注册数据.
     * 
     * @param key 键
     * @param value 值
     */
    void persistEphemeral(String key, String value);
}
