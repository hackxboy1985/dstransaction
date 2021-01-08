

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.context.TransactionContext;
import cn.ds.transaction.framework.context.TransactionContextProperties;
import cn.ds.transaction.framework.contextHelper.TransactionContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionContextHelperTest {

  private final String transactionGlobalTxId = UUID.randomUUID().toString();

  private final String transactionLocalTxId = UUID.randomUUID().toString();

  private final TransactionContext txContext = new TransactionContext(transactionGlobalTxId, transactionLocalTxId);

  private final TransactionContextProperties txContextProperties = mock(TransactionContextProperties.class);

  private final TransactionContextHelper transactionContextHelper = new TransactionContextHelper() {
    @Override
    protected Logger getLogger() {
      return LoggerFactory.getLogger(getClass());
    }


  };

  @Before
  public void setUp() {
    when(txContextProperties.getGlobalTxId()).thenReturn(transactionGlobalTxId);
    when(txContextProperties.getLocalTxId()).thenReturn(transactionLocalTxId);
  }

  @Test
  public void testExtractTransactionContext() {

    TransactionContext result = transactionContextHelper.extractTransactionContext(new Object[] {txContextProperties});
    assertThat(result.globalTxId(), is(transactionGlobalTxId));
    assertThat(result.localTxId(), is(transactionLocalTxId));

    result = transactionContextHelper.extractTransactionContext(new Object[] {});
    assertNull(result);

    result = transactionContextHelper.extractTransactionContext(null);
    assertNull(result);

    result = transactionContextHelper.extractTransactionContext(new Object[] {txContext});
    assertThat(result, is(txContext));

    TransactionContext otherTx = Mockito.mock(TransactionContext.class);
    result = transactionContextHelper.extractTransactionContext(new Object[] {otherTx, txContextProperties});
    assertThat(result, is(otherTx));
  }

  @Test
  public void testPopulateSagaContextWhenItsEmpty() {

    SagaContext sagaContext = new SagaContext(null);

    transactionContextHelper.populateSagaContext(sagaContext, txContext);

    assertEquals(transactionGlobalTxId, sagaContext.globalTxId());
    assertEquals(transactionLocalTxId, sagaContext.localTxId());
  }

  @Test
  public void testPopulateSagaContextWhenItsNotEmpty() {
    SagaContext sagaContext = new SagaContext(null);

    sagaContext.setGlobalTxId("global-tx-id");
    sagaContext.setLocalTxId("local-tx-id");

    transactionContextHelper.populateSagaContext(sagaContext, txContext);

    assertEquals(transactionGlobalTxId, sagaContext.globalTxId());
    assertEquals(transactionLocalTxId, sagaContext.localTxId());
  }
}
