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
package com.imadcn.framework.idworker.register.zookeeper;

/**
 * 机器节点Path
 * 
 * @author imadcn
 * @since 1.0.0
 */
public class NodePath {

    private static final String WORKER_NODE = "worker";

    /**
     * workerId 分组
     */
    private String groupName;

    /**
     * 机器编号
     */
    private long workerId;

    private long sessionId = -1L;

    public NodePath(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPath() {
        return String.format("/%s", groupName);
    }

    public String getWorkerPath() {
        return String.format("/%s/%s", groupName, WORKER_NODE);
    }

    public String getWorkerIdPath() {
        return String.format("/%s/%s/%s", groupName, WORKER_NODE, workerId);
    }

    public String getGroupName() {
        return groupName;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

}
