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
 * JSON 序列化工厂
 * @author imadcn
 * @since 2.0.0
 */
public class JsonSerializerFactory {
    
    private JsonSerializerFactory() {}
    
    /**
     * 创建默认JSON序列化工具
     * @return 默认使用FastJson作为序列化工具
     */
    public static JsonSerialier createDefault() {
        return new FastJsonSerializer();
    }
}
