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
package com.imadcn.framework.idworker.register;

import java.io.Closeable;

/**
 * ID生成注册Connector
 * 
 * @author imadcn
 * @since 1.1.0
 */
public interface GeneratorConnector extends Closeable {

    /**
     * 初始化数据
     */
    void init();

    /**
     * 连接
     */
    void connect();

    /**
     * 挂起ID生产
     */
    void suspend();

    /**
     * 是否正在正常运行
     * 
     * @return 是返回<b> true </b>,否则返回<b> false </b>
     */
    boolean isWorking();

    /**
     * 是否正在连接
     * 
     * @return 是返回<b> true </b>,否则返回<b> false </b>
     */
    boolean isConnecting();
}
