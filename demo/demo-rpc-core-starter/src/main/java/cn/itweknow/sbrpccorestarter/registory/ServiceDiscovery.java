package cn.itweknow.sbrpccorestarter.registory;

import cn.itweknow.sbrpccorestarter.common.Constants;
import cn.itweknow.sbrpccorestarter.exception.ZkConnectException;
import cn.itweknow.sbrpccorestarter.model.ProviderInfo;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.apache.zookeeper.Watcher.Event.KeeperState.Expired;

/**
 * @author sj
 * @date 2020/12/26 17:27
 * @description 服务发现:查找zk上所有的服务及提供者信息
 */
public class ServiceDiscovery {

    private Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private volatile Map<String,List<ProviderInfo>> providerMap = new HashMap<>();
    private volatile List<ProviderInfo> dataList = new ArrayList<>();
    private volatile int index=0;
    ZooKeeper zooKeeper;
    String registoryAddress;

    public ServiceDiscovery(String registoryAddress) throws ZkConnectException {
        this.registoryAddress=registoryAddress;
        register();
    }

    void register() throws ZkConnectException{
        try {
            // 获取zk连接。
            logger.info("RpcStarter::ServiceDiscovery: start connect zk registoryAddress:{}", registoryAddress);
            zooKeeper = new ZooKeeper(registoryAddress, 2000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.info("RpcStarter::ServiceDiscovery: connect zk status:{},event:{}",event.getState().name(), event.getType().name());
                    if (event.getType().equals(Event.EventType.NodeChildrenChanged)) {
                        watchNode2(zooKeeper);
                    }else if (event.getState().equals(Expired)) {
                        reConnect();
                    }
                }
            });
            watchNode(zooKeeper);
        } catch (Exception e) {
            throw new ZkConnectException("RpcStarter::ServiceDiscovery: connect to zk exception," + e.getMessage(), e.getCause());
        }
    }

    void close(){
        try {
            if(zooKeeper!=null) {
                logger.info("RpcStarter::ServiceDiscovery: close...");
                zooKeeper.close();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }finally {
            zooKeeper = null;
        }
    }

    void reConnect(){
        logger.info("RpcStarter::ServiceDiscovery: start reconnect because disconnected...");
        try {
            close();
            register();
        } catch (ZkConnectException e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * 获取zk上的服务节点，并设置观察回调，在变更时重新获取zk中的服务变化
     * @param zk
     */
    public void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constants.ZK_ROOT_DIR, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // 节点改变，有服务上线或下线
                    logger.info("RpcStarter::ServiceDiscovery: watch node zk status:{},event:{}",event.getState().name(), event.getType().name());
                    if (event.getType().equals(Event.EventType.NodeChildrenChanged)) {
                        watchNode2(zk);
                    }
                }
            });
            refreshNode(nodeList,zk);
        } catch (Exception e) {
            logger.error("RpcStarter::ServiceDiscovery: watch zk error,", e);
        }
    }

    void watchNode2(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constants.ZK_ROOT_DIR, true);
            refreshNode(nodeList,zk);
        } catch (Exception e) {
            logger.error("RpcStarter::ServiceDiscovery: watch zk error,", e);
        }
    }

    void refreshNode(List<String> nodeList,final ZooKeeper zk) throws KeeperException, InterruptedException {
        List<ProviderInfo> providerInfos = new ArrayList<>();
        // 循环子节点，获取服务名称
        for (String node: nodeList) {
            byte[] bytes = zk.getData(Constants.ZK_ROOT_DIR + "/" + node, false, null);//"/rpc/"
            String[] providerInfo = new String(bytes).split(",");
            if (providerInfo.length == 2) {
                providerInfos.add(new ProviderInfo(providerInfo[0], providerInfo[1]));
            }
        }
        this.dataList = providerInfos;
        logger.info("RpcStarter::ServiceDiscovery: discover service list：### {} ###", this.dataList);
//            collectToHash();
    }

    void collectToHash(){
        providerMap.clear();
        for (ProviderInfo providerInfo : dataList) {
            List<ProviderInfo> providerInfoList = providerMap.get(providerInfo.getName());
            if (providerInfoList != null){
                providerInfoList = new ArrayList<>();
                providerMap.put(providerInfo.getName(), providerInfoList);
            }
            providerInfoList.add(providerInfo);
        }
    }

    /**
     * 获取一个服务提供者
     * @param providerName
     * @return
     */
    public ProviderInfo discover(String providerName) {
        if (dataList.isEmpty()) {
            return null;
        }
        List<ProviderInfo> providerInfos = dataList.stream()
                .filter(one -> providerName.equals(one.getName()))
                .collect(Collectors.toList());
        if (providerInfos.isEmpty()) {
            return null;
        }
        //随机或轮询调用
        return providerInfos.get(ThreadLocalRandom.current()
                .nextInt(providerInfos.size()));
//        return providerInfos.get(next()%providerInfos.size());
    }

    private int next(){
        return ++index;
    }
}
