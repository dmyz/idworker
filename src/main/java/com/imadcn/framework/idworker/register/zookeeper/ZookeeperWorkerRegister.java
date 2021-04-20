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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.exception.RegException;
import com.imadcn.framework.idworker.register.AbstractWorkerRegister;
import com.imadcn.framework.idworker.register.NodeInfo;
import com.imadcn.framework.idworker.registry.CoordinatorRegistryCenter;

/**
 * 基于Zookeeper的机器节点注册器
 * 
 * @author imadcn
 * @since 1.0.0
 */
public class ZookeeperWorkerRegister extends AbstractWorkerRegister {

    /**
     * zk节点信息
     */
    private final NodePath nodePath;

    public ZookeeperWorkerRegister(CoordinatorRegistryCenter regCenter,
        ApplicationConfiguration applicationConfiguration) {
        super(regCenter, applicationConfiguration);
        this.nodePath = new NodePath(applicationConfiguration.getGroup());
    }

    /**
     * 向zookeeper注册workerId
     * 
     * @return workerId workerId
     */
    @Override
    public long register() {
        InterProcessMutex lock = null;
        try {
            CoordinatorRegistryCenter regCenter = getRegCenter();
            CuratorFramework client = (CuratorFramework) regCenter.getRawClient();
            lock = new InterProcessMutex(client, nodePath.getGroupPath());
            int numOfChildren = regCenter.getNumChildren(nodePath.getWorkerPath());
            if (numOfChildren < MAX_WORKER_NUM) {
                if (!lock.acquire(MAX_LOCK_WAIT_TIME_MS, TimeUnit.MILLISECONDS)) {
                    String message = String.format("acquire lock failed after %s ms.", MAX_LOCK_WAIT_TIME_MS);
                    throw new TimeoutException(message);
                }
                NodeInfo localNodeInfo = getLocalNodeInfo();
                List<String> children = regCenter.getChildrenKeys(nodePath.getWorkerPath());
                // 有本地缓存的节点信息，同时ZK也有这条数据
                if (localNodeInfo != null && children.contains(String.valueOf(localNodeInfo.getWorkerId()))) {
                    String key = getNodePathKey(nodePath, localNodeInfo.getWorkerId());
                    String zkNodeInfoJson = regCenter.get(key);
                    NodeInfo zkNodeInfo = createNodeInfoFromJsonStr(zkNodeInfoJson);
                    if (checkNodeInfo(localNodeInfo, zkNodeInfo)) {
                        // 更新ZK节点信息，保存本地缓存，开启定时上报任务
                        nodePath.setWorkerId(zkNodeInfo.getWorkerId());
                        zkNodeInfo.setUpdateTime(new Date());
                        updateNodeInfo(key, zkNodeInfo);
                        saveLocalNodeInfo(zkNodeInfo);
                        executeUploadNodeInfoTask(key, zkNodeInfo);
                        return zkNodeInfo.getWorkerId();
                    }
                }
                // 无本地信息或者缓存数据不匹配，开始向ZK申请节点机器ID
                for (int workerId = 0; workerId < MAX_WORKER_NUM; workerId++) {
                    String workerIdStr = String.valueOf(workerId);
                    if (!children.contains(workerIdStr)) { // 申请成功
                        NodeInfo applyNodeInfo = createNodeInfo(nodePath.getGroupName(), workerId);
                        nodePath.setWorkerId(applyNodeInfo.getWorkerId());
                        // 保存ZK节点信息，保存本地缓存，开启定时上报任务
                        saveZookeeperNodeInfo(nodePath.getWorkerIdPath(), applyNodeInfo);
                        saveLocalNodeInfo(applyNodeInfo);
                        executeUploadNodeInfoTask(nodePath.getWorkerIdPath(), applyNodeInfo);
                        return applyNodeInfo.getWorkerId();
                    }
                }
            }
            throw new RegException("max worker num reached. register failed");
        } catch (RegException e) {
            throw e;
        } catch (Exception e) {
            logger.error("", e);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception ignored) {
                logger.error("", ignored);
            }
        }
    }

    /**
     * 添加连接监听
     * 
     * @param listener zk状态监听listener
     */
    @Deprecated
    public void addConnectionListener(ConnectionStateListener listener) {
        // CuratorFramework client = (CuratorFramework)
        // regCenter.getRawClient();
        // client.getConnectionStateListenable().addListener(listener);
    }

    /**
     * 关闭注册
     */
    @Override
    public synchronized void logout() {
        CoordinatorRegistryCenter regCenter = getRegCenter();
        CuratorFramework client = (CuratorFramework) regCenter.getRawClient();
        if (client != null && client.getState() == CuratorFrameworkState.STARTED) {
            // 移除注册节点（最大程度的自动释放资源）
            regCenter.remove(nodePath.getWorkerIdPath());
            // 关闭连接
            regCenter.close();
        }
    }

    /**
     * 刷新注册中心节点信息（修改updateTime）
     * 
     * @param key
     * @param nodeInfo
     */
    @Override
    public void updateNodeInfo(String key, NodeInfo nodeInfo) {
        try {
            nodeInfo.setUpdateTime(new Date());
            CoordinatorRegistryCenter regCenter = getRegCenter();
            if (isDurable()) {
                regCenter.persist(key, jsonizeNodeInfo(nodeInfo));
            } else {
                regCenter.persistEphemeral(key, jsonizeNodeInfo(nodeInfo));
            }
        } catch (Exception e) {
            logger.debug("update zookeeper node info error, {}", e);
        }
    }

    /**
     * 获取节点ZK Path Key
     * 
     * @param nodePath 节点路径信息
     * @param workerId 节点机器ID
     * @return
     */
    private String getNodePathKey(NodePath nodePath, Integer workerId) {
        StringBuilder builder = new StringBuilder();
        builder.append(nodePath.getWorkerPath()).append("/");
        builder.append(workerId);
        return builder.toString();
    }

    /**
     * 保存ZK节点信息
     * 
     * @param key
     * @param nodeInfo
     */
    private void saveZookeeperNodeInfo(String key, NodeInfo nodeInfo) {
        CoordinatorRegistryCenter regCenter = getRegCenter();
        if (isDurable()) {
            regCenter.persist(key, jsonizeNodeInfo(nodeInfo));
        } else {
            regCenter.persistEphemeral(key, jsonizeNodeInfo(nodeInfo));
        }
    }

}
