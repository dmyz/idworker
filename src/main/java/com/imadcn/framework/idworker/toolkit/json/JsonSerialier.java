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
package com.imadcn.framework.idworker.toolkit.json;

/**
 * JSON工具集定义
 * @author imadcn
 * @since 2.0.0
 */
public interface JsonSerialier {
    
    /**
     * 将一个对象，转换为JSON字符串
     * @param object 对象
     * @return JSON字符串
     */
    String toJsonString(Object object);
    
    /**
     * 将一串JSON字符串，转换为一个对象
     * @param <T> 需要转换成的对象类型
     * @param json JSON字符串
     * @param clazz 转换对象类，类型
     * @return
     */
    <T> T parseObject(String json, Class<T> clazz);
}
