package cn.itweknow.sbrpccorestarter.registory;

import cn.itweknow.sbrpccorestarter.common.Constants;
import cn.itweknow.sbrpccorestarter.exception.ZkConnectException;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author sj
 * @date 2020/12/26 16:04
 * @description
 */
public class RegistryServer {

    private Logger logger = LoggerFactory.getLogger(RegistryServer.class);

    /**
     * zk的地址
     */
    private String addr;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 服务名
     */
    private String serverName;

    private String host;

    private int port;

    public RegistryServer(String addr,
                          int timeout,
                          String serverName,
                          String host,
                          int port) {
        this.addr = addr;
        this.timeout = timeout;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
    }

    /**
     * zk注册
     * @throws IOException
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void register() throws ZkConnectException {
        try {
            // 获取zk连接
            logger.info("RpcStarter::Provider::Registry-zk: connect...");
            ZooKeeper zooKeeper = new ZooKeeper(addr, timeout, event -> {
                logger.info("RpcStarter::Provider::ZK-Status: state={}, type={}",event.getState().name(),event.getType().name());
            });
            if (zooKeeper.exists(Constants.ZK_ROOT_DIR, false) == null) {
                zooKeeper.create(Constants.ZK_ROOT_DIR, Constants.ZK_ROOT_DIR.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            zooKeeper.create(Constants.ZK_ROOT_DIR + "/" + serverName,
                    (serverName + ","+ host + ":" + port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("RpcStarter::provider::Registry-zk: registry server success >> {} = {}", serverName, host + ":" + port);
        } catch (Exception e) {
            throw new ZkConnectException("RpcStarter::provider::Registry-zk: exception: " + e.getMessage(), e.getCause());
        }
    }

}
