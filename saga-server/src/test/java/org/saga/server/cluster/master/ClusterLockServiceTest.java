

package org.saga.server.cluster.master;

import org.saga.server.ServerApplication;
import org.saga.server.ServerConfig;
import org.saga.server.cluster.master.provider.jdbc.MasterLockEntityRepository;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLockRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {ServerApplication.class, ServerConfig.class},
    properties = {
        "alpha.cluster.master.enabled=true",
        "alpha.server.host=0.0.0.0",
        "alpha.server.port=8090",
        "alpha.event.pollingInterval=1",
        "spring.main.allow-bean-definition-overriding=true"
    })
public class ClusterLockServiceTest {

  @Value("[${alpha.server.host}]:${alpha.server.port}")
  private String instanceId;

  @Value("${spring.application.name:servicecomb-alpha-server}")
  private String serviceName;

  @Value("${alpha.cluster.master.expire:5000}")
  private int expire;

  @Autowired
  ClusterLockService clusterLockService;

  @MockBean
  private MasterLockEntityRepository masterLockEntityRepository;

  @Autowired
  MasterLockRepository masterLockRepository;

  @Before
  // As clusterLockService stop check if the cluster is locked,
  // In this way we need to clean up the locker for each unit test
  public void before(){
    await().atMost(2, SECONDS).until(() -> {
      if(clusterLockService.isLockExecuted() == true){
        clusterLockService.unLock();
        return true;
      }else{
        return false;
      }
    });
  }

  @Test
  public void testMasterNode() {
    MasterLock masterLock = clusterLockService.getMasterLock();
    Assert.assertEquals(masterLock.getServiceName(),serviceName);
    Assert.assertEquals(masterLock.getInstanceId(),instanceId);
    Assert.assertEquals((masterLock.getExpireTime().getTime()-masterLock.getLockedTime().getTime()),expire);
    when(masterLockEntityRepository.initLock(any(), any(), any(),any())).thenReturn(1);
    when(masterLockEntityRepository.findMasterLockByServiceName(any())).thenReturn(Optional.of(masterLock));
    when(masterLockEntityRepository.updateLock(any(), any(), any(),any())).thenReturn(1);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isLockExecuted() == true);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isMasterNode() == true);
  }

  @Test
  public void testSlaveNodeWhenDuplicateKey() {
    when(masterLockEntityRepository.findMasterLockByServiceName(any())).thenReturn(Optional.empty());
    when(masterLockEntityRepository.initLock(any(), any(), any(),any())).thenThrow(new RuntimeException("duplicate key"));
    when(masterLockEntityRepository.updateLock(any(), any(), any(),any())).thenReturn(0);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isLockExecuted() == true);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isMasterNode() == false);
  }

  @Test
  public void testSlaveNodeUpdateLockLater() {
    when(masterLockEntityRepository.findMasterLockByServiceName(any())).thenReturn(Optional.of(clusterLockService.getMasterLock()));
    when(masterLockEntityRepository.updateLock(any(), any(), any(),any())).thenReturn(0);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isLockExecuted() == true);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isMasterNode() == false);
  }

  @Test
  public void testSlaveNodeWhenInitLockException() {
    when(masterLockEntityRepository.findMasterLockByServiceName(any())).thenThrow(new RuntimeException("initLock Exception"));
    when(masterLockEntityRepository.updateLock(any(), any(), any(),any())).thenReturn(0);
    await().atMost(2, SECONDS).until(() -> clusterLockService.isLockExecuted() == true);
    clusterLockService.unLock();
    await().atMost(2, SECONDS).until(() -> clusterLockService.isMasterNode() == false);
  }

  @Test
  public void testSlaveNodeWhenUpdateLockException() {
    when(masterLockEntityRepository.initLock(any(), any(), any(),any())).thenReturn(1);
    when(masterLockEntityRepository.findMasterLockByServiceName(any())).thenReturn(Optional.of(clusterLockService.getMasterLock()));
    when(masterLockEntityRepository.updateLock(any(), any(), any(),any())).thenThrow(new RuntimeException("updateLock Exception"));
    await().atMost(2, SECONDS).until(() -> clusterLockService.isLockExecuted() == true);
    clusterLockService.unLock();
    await().atMost(2, SECONDS).until(() -> clusterLockService.isMasterNode() == false);
  }
}
