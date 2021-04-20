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
package com.imadcn.framework.idworker.registry.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.imadcn.framework.idworker.config.ZookeeperConfiguration;
import com.imadcn.framework.idworker.exception.RegExceptionHandler;
import com.imadcn.framework.idworker.registry.AbstractCoordinatorRegistryCenter;

/**
 * Zookeeper注册中心
 * 
 * @author imadcn
 * @since 1.0.0
 */
public class ZookeeperRegistryCenter extends AbstractCoordinatorRegistryCenter {

    private ZookeeperConfiguration zkConfig;

    private CuratorFramework client;

    public ZookeeperRegistryCenter(ZookeeperConfiguration zookeeperConfigurarion) {
        this.zkConfig = zookeeperConfigurarion;
    }

    @Override
    public synchronized void init() {
        if (client != null) {
            // client已经初始化，直接重置返回
            return;
        }
        logger.debug("init zookeeper registry, connect to servers : {}", zkConfig.getServerLists());
        CuratorFrameworkFactory.Builder builder
            = CuratorFrameworkFactory.builder().connectString(zkConfig.getServerLists())
                .retryPolicy(new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMilliseconds(),
                    zkConfig.getMaxRetries(), zkConfig.getMaxSleepTimeMilliseconds()))
                .namespace(zkConfig.getNamespace());
        if (0 != zkConfig.getSessionTimeoutMilliseconds()) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
        }
        if (0 != zkConfig.getConnectionTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeoutMilliseconds());
        }
        if (zkConfig.getDigest() != null && !zkConfig.getDigest().isEmpty()) {
            builder.authorization("digest", zkConfig.getDigest().getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {

                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }

                    @Override
                    public List<ACL> getAclForPath(final String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                });
        }
        client = builder.build();
        client.start();
        try {
            if (!client.blockUntilConnected(zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(),
                TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void close() {
        if (client == null) {
            return;
        }
        CloseableUtils.closeQuietly(client);
        // 重置client状态
        client = null;
    }

    @Override
    public String get(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }

    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {
                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public int getNumChildren(final String key) {
        try {
            Stat stat = client.checkExists().forPath(key);
            if (null != stat) {
                return stat.getNumChildren();
            }
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
        return 0;
    }

    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key,
                    value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void update(final String key, final String value) {
        try {
            client.transactionOp().setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // client.inTransaction().check().forPath(key).and().setData().forPath(key,
            // value.getBytes(StandardCharsets.UTF_8)).and().commit();
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key,
                value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public Object getRawClient() {
        if (client == null) {
            init();
        }
        return client;
    }
}
