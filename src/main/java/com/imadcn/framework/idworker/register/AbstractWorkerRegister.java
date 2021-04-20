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
package com.imadcn.framework.idworker.register;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.registry.CoordinatorRegistryCenter;
import com.imadcn.framework.idworker.toolkit.json.JsonSerialier;
import com.imadcn.framework.idworker.toolkit.json.JsonSerializerFactory;
import com.imadcn.framework.idworker.util.HostUtils;

/**
 * 机器节点（worker）注册器
 * 
 * @author imadcn
 * @since 2.0.0
 */
public abstract class AbstractWorkerRegister implements WorkerRegister {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 最大机器数
     */
    public static final long MAX_WORKER_NUM = 1024;
    /**
     * 加锁最大等待时间
     */
    public static final int MAX_LOCK_WAIT_TIME_MS = 30 * 1000;
    /**
     * 注册中心工具
     */
    private CoordinatorRegistryCenter regCenter;
    /**
     * 注册文件
     */
    private String registryFile;
    /**
     * zk节点是否持久化存储
     */
    private boolean durable;
    /**
     * JSON序列化工具
     */
    private JsonSerialier jsonSerialier = JsonSerializerFactory.createDefault();

    public AbstractWorkerRegister(CoordinatorRegistryCenter regCenter,
        ApplicationConfiguration applicationConfiguration) {

        this.regCenter = regCenter;
        this.durable = applicationConfiguration.isDurable();
        String registryFile = getDefaultFilePath(applicationConfiguration.getGroup());
        if (!StringUtils.isEmpty(applicationConfiguration.getRegistryFile())) {
            registryFile = applicationConfiguration.getRegistryFile();
        }
        this.registryFile = registryFile;
    }

    /**
     * 通过节点信息JSON字符串反序列化节点信息
     * 
     * @param jsonStr 节点信息JSON字符串
     * @return 节点信息
     */
    public NodeInfo createNodeInfoFromJsonStr(String jsonStr) {
        return getJsonSerialier().parseObject(jsonStr, NodeInfo.class);
    }

    /**
     * 节点信息转json字符串
     * 
     * @param nodeInfo 节点信息
     * @return json字符串
     */
    public String jsonizeNodeInfo(NodeInfo nodeInfo) {
        return getJsonSerialier().toJsonString(nodeInfo);
    }

    /**
     * 获取本地节点缓存文件路径
     * 
     * @param groupName 分组名
     * @return 文件路径
     */
    private String getDefaultFilePath(String groupName) {
        StringBuilder builder = new StringBuilder();
        builder.append(".").append(File.separator).append("tmp");
        builder.append(File.separator).append("idworker");
        builder.append(File.separator).append(groupName).append(".cache");
        return builder.toString();
    }

    /**
     * 检查节点信息
     * 
     * @param localNodeInfo 本地缓存节点信息
     * @param registryNodeInfo 注册中心节点信息
     * @return
     */
    public boolean checkNodeInfo(NodeInfo localNodeInfo, NodeInfo registryNodeInfo) {
        try {
            // NodeId、IP、HostName、GroupName 相等（本地缓存==ZK数据）
            if (!registryNodeInfo.getNodeId().equals(localNodeInfo.getNodeId())) {
                return false;
            }
            if (!registryNodeInfo.getIp().equals(localNodeInfo.getIp())) {
                return false;
            }
            if (!registryNodeInfo.getHostName().equals(localNodeInfo.getHostName())) {
                return false;
            }
            if (!registryNodeInfo.getGroupName().equals(localNodeInfo.getGroupName())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("check node info error, {}", e);
            return false;
        }
    }

    /**
     * 更新节点信息Task
     * 
     * @param key zk path
     * @param nodeInfo 节点信息
     */
    public void executeUploadNodeInfoTask(final String key, final NodeInfo nodeInfo) {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "upload node info task thread");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateNodeInfo(key, nodeInfo);
            }
        }, 3L, 3L, TimeUnit.SECONDS);
    }

    /**
     * 缓存机器节点信息至本地
     * 
     * @param nodeInfo 机器节点信息
     */
    public void saveLocalNodeInfo(NodeInfo nodeInfo) {
        try {
            File nodeInfoFile = new File(getRegistryFile());
            String nodeInfoJson = jsonizeNodeInfo(nodeInfo);
            FileUtils.writeStringToFile(nodeInfoFile, nodeInfoJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("save node info cache error, {}", e);
        }
    }

    /**
     * 读取本地缓存机器节点
     * 
     * @return 机器节点信息
     */
    public NodeInfo getLocalNodeInfo() {
        try {
            File nodeInfoFile = new File(getRegistryFile());
            if (nodeInfoFile.exists()) {
                String nodeInfoJson = FileUtils.readFileToString(nodeInfoFile, StandardCharsets.UTF_8);
                NodeInfo nodeInfo = createNodeInfoFromJsonStr(nodeInfoJson);
                return nodeInfo;
            }
        } catch (Exception e) {
            logger.error("read node info cache error, {}", e);
        }
        return null;
    }

    /**
     * 初始化节点信息
     * 
     * @param groupName 分组名
     * @param workerId 机器号
     * @return 节点信息
     * @throws UnknownHostException
     */
    public NodeInfo createNodeInfo(String groupName, Integer workerId) throws UnknownHostException {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeId(genNodeId());
        nodeInfo.setGroupName(groupName);
        nodeInfo.setWorkerId(workerId);
        nodeInfo.setIp(HostUtils.getLocalIP());
        nodeInfo.setHostName(HostUtils.getLocalHostName());
        nodeInfo.setCreateTime(new Date());
        nodeInfo.setUpdateTime(new Date());
        return nodeInfo;
    }

    /**
     * 获取节点唯一ID （基于UUID）
     * 
     * @return 节点唯一ID
     */
    public String genNodeId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 刷新注册中心节点信息（修改updateTime）
     * 
     * @param key 数据主键
     * @param nodeInfo 节点信息
     */
    public abstract void updateNodeInfo(String key, NodeInfo nodeInfo);

    public CoordinatorRegistryCenter getRegCenter() {
        return regCenter;
    }

    public void setRegCenter(CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }

    public String getRegistryFile() {
        return registryFile;
    }

    public void setRegistryFile(String registryFile) {
        this.registryFile = registryFile;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public JsonSerialier getJsonSerialier() {
        return jsonSerialier;
    }

    public void setJsonSerialier(JsonSerialier jsonSerialier) {
        this.jsonSerialier = jsonSerialier;
    }
}
