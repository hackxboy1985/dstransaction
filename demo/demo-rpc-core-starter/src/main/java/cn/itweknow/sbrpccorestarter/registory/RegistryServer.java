package cn.itweknow.sbrpccorestarter.registory;

import cn.itweknow.sbrpccorestarter.common.Constants;
import cn.itweknow.sbrpccorestarter.exception.ZkConnectException;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.zookeeper.Watcher.Event.KeeperState.Expired;

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

    ZooKeeper zooKeeper;

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
            zooKeeper = new ZooKeeper(addr, timeout, event -> {
                try {
                    statusHandle(event);
                } catch (ZkConnectException e) {
                    logger.error("RpcStarter::Provider::Registry-zk: error={}",e.getMessage(),e);
                }
            });
            //创建目录
            if (zooKeeper.exists(Constants.ZK_ROOT_DIR, false) == null) {
                zooKeeper.create(Constants.ZK_ROOT_DIR, Constants.ZK_ROOT_DIR.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            //创建EPHEMERAL_SEQUENTIAL临时顺序编号目录节点,断开连接，结点会删除，会自动同步到watch的zk
            zooKeeper.create(Constants.ZK_ROOT_DIR + "/" + serverName,
                    (serverName + ","+ host + ":" + port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("RpcStarter::Provider::Registry-zk: success >> {} = {}", serverName, host + ":" + port);
        } catch (Exception e) {
            throw new ZkConnectException("RpcStarter::Provider::Registry-zk: exception: " + e.getMessage(), e.getCause());
        }
    }

    void statusHandle(WatchedEvent event)  throws ZkConnectException{
        logger.info("RpcStarter::Provider::Registry-zk: receive ZK-Status: state={}, type={}",event.getState().name(),event.getType().name());
        switch (event.getState()){
            case Unknown:
                break;
            case Disconnected: {
            }
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                reConnect();
                break;
        }
    }

    void close(){
        try {
            if(zooKeeper!=null) {
                logger.info("RpcStarter::Provider::Registry-zk: close...");
                zooKeeper.close();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }finally {
            zooKeeper = null;
        }
    }

    void reConnect() throws ZkConnectException{
        logger.info("RpcStarter::Provider::Registry-zk: start reconnect because disconnected...");
        close();
        register();
    }

}
