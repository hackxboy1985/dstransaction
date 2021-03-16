

package org.saga.server.api;

import org.saga.server.ServerApplication;
import org.saga.server.ServerConfig;
import org.saga.server.common.NodeStatus;
import org.saga.server.metrics.ServerMetricsEndpoint;
import org.saga.server.metrics.MetricsBean;
import org.saga.server.metrics.MetricsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {ServerApplication.class, ServerConfig.class})
public class APIv1ControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  ServerMetricsEndpoint serverMetricsEndpoint;

  @MockBean
  MetricsService metricsService;

  @MockBean
  NodeStatus nodeStatus;

//  @MockBean
//  ElasticsearchTemplate template;


  @Test
  public void metricsTest() throws Exception {
    MetricsBean metricsBean = new MetricsBean();
    metricsBean.doEventReceived();
    metricsBean.doEventAccepted();
    metricsBean.doEventAvgTime(5);
    metricsBean.doActorReceived();
    metricsBean.doActorAccepted();
    metricsBean.doActorAvgTime(5);
    metricsBean.doRepositoryReceived();
    metricsBean.doRepositoryAccepted();
    metricsBean.doRepositoryAvgTime(5);
    metricsBean.doCommitted();
    metricsBean.doCompensated();
    metricsBean.doSuspended();
    metricsBean.doSagaBeginCounter();
    metricsBean.doSagaEndCounter();
    metricsBean.doSagaAvgTime(5);
    when(metricsService.metrics()).thenReturn(metricsBean);
    when(nodeStatus.getTypeEnum()).thenReturn(NodeStatus.TypeEnum.MASTER);
    mockMvc.perform(get("/saga/api/v1/metrics"))
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath("$.metrics.eventReceived").value(1))
        .andExpect(jsonPath("$.metrics.eventAccepted").value(1))
        .andExpect(jsonPath("$.metrics.eventRejected").value(0))
        .andExpect(jsonPath("$.metrics.eventAvgTime").value(5.0))
        .andExpect(jsonPath("$.metrics.actorReceived").value(1))
        .andExpect(jsonPath("$.metrics.actorAccepted").value(1))
        .andExpect(jsonPath("$.metrics.actorRejected").value(0))
        .andExpect(jsonPath("$.metrics.actorAvgTime").value(5.0))
        .andExpect(jsonPath("$.metrics.repositoryReceived").value(1))
        .andExpect(jsonPath("$.metrics.repositoryAccepted").value(1))
        .andExpect(jsonPath("$.metrics.repositoryRejected").value(0))
        .andExpect(jsonPath("$.metrics.repositoryAvgTime").value(5.0))
        .andExpect(jsonPath("$.metrics.sagaBeginCounter").value(1))
        .andExpect(jsonPath("$.metrics.sagaEndCounter").value(1))
        .andExpect(jsonPath("$.metrics.sagaAvgTime").value(5.0))
        .andExpect(jsonPath("$.metrics.committed").value(1))
        .andExpect(jsonPath("$.metrics.compensated").value(1))
        .andExpect(jsonPath("$.metrics.suspended").value(1))
        .andExpect(jsonPath("$.nodeType").value(NodeStatus.TypeEnum.MASTER.name()))
        .andReturn();
  }

  @Test
  public void transactionTest() throws Exception {
    final String serviceName = "serviceName-1";
    final String instanceId = "instanceId-1";
    final String globalTxId = UUID.randomUUID().toString();
    final String localTxId_1 = UUID.randomUUID().toString();
    final String localTxId_2 = UUID.randomUUID().toString();
    final String localTxId_3 = UUID.randomUUID().toString();
  }



}
