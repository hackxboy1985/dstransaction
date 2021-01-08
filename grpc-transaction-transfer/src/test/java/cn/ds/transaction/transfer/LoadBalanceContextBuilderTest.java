

package cn.ds.transaction.transfer;

import cn.ds.transaction.framework.context.ServiceConfig;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.transfer.core.LoadBalanceContextBuilder;
import cn.ds.transaction.transfer.core.TransactionType;
import com.google.common.collect.Lists;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoadBalanceContextBuilderTest {

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final SagaSvrClusterConfig clusterConfig = mock(SagaSvrClusterConfig.class);
  private final String serverName = uniquify("serviceName");
  private final ServiceConfig serviceConfig = new ServiceConfig(serverName);
  protected final String[] addresses = {"localhost:8080", "localhost:8090"};

  private LoadBalanceContextBuilder tccLoadBalanceContextBuilder;
  private  LoadBalanceContextBuilder sagaLoadBalanceContextBuilder;

  @Before
  public void setup() throws IOException {
    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder.forName("localhost:8080").directExecutor().build().start());
    grpcCleanup.register(InProcessServerBuilder.forName("localhost:8090").directExecutor().build().start());
    when(clusterConfig.getAddresses()).thenReturn(Lists.newArrayList(addresses));
    sagaLoadBalanceContextBuilder =
        new LoadBalanceContextBuilder(TransactionType.SAGA, clusterConfig, serviceConfig, 30,10);
  }

  @After
  public void teardown() {
  }


  @Test(expected = IllegalArgumentException.class)
  public void throwExceptionWhenAddressIsNotExist() {
    when(clusterConfig.getAddresses()).thenReturn(new ArrayList<String>());
    tccLoadBalanceContextBuilder.build();
  }

  @Test
  public void buildSagaLoadBalanceContextWithoutSsl() {
    LoadBalanceContext loadContext = sagaLoadBalanceContextBuilder.build();
    assertThat(loadContext.getPendingTaskRunner().getReconnectDelay(), is(30));
    assertThat(loadContext.getSenders().size(), is(2));
    assertThat(loadContext.getSenders().keySet().iterator().next(), instanceOf(SagaMessageSender.class));
    assertThat(loadContext.getSenders().values().iterator().next(), is(0l));
    assertThat(loadContext.getChannels().size(), is(2));
    loadContext.getSenders().keySet().iterator().next().close();
    shutdownChannels(loadContext);
  }

  @Test
  public void buildSagaLoadBalanceContextWithSsl() {
    when(clusterConfig.isEnableSSL()).thenReturn(true);
    when(clusterConfig.getCert()).thenReturn(getClass().getClassLoader().getResource("client.crt").getFile());
    when(clusterConfig.getCertChain()).thenReturn(getClass().getClassLoader().getResource("ca.crt").getFile());
    when(clusterConfig.getKey()).thenReturn(getClass().getClassLoader().getResource("client.pem").getFile());
    LoadBalanceContext loadContext = sagaLoadBalanceContextBuilder.build();
    assertThat(loadContext.getPendingTaskRunner().getReconnectDelay(), is(30));
    assertThat(loadContext.getSenders().size(), is(2));
    assertThat(loadContext.getSenders().keySet().iterator().next(), instanceOf(SagaMessageSender.class));
    assertThat(loadContext.getSenders().values().iterator().next(), is(0l));
    assertThat(loadContext.getChannels().size(), is(2));
    shutdownChannels(loadContext);
  }

  private void shutdownChannels(LoadBalanceContext loadContext) {
    for (ManagedChannel each : loadContext.getChannels()) {
      each.shutdownNow();
    }
  }
}
