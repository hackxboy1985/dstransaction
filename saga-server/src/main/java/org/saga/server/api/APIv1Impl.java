

package org.saga.server.api;

import java.util.List;
import java.util.Map;
import org.saga.server.metrics.AlphaMetrics;
import org.saga.server.metrics.AlphaMetricsEndpoint;
import org.saga.server.metrics.AlphaMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class APIv1Impl implements APIv1 {

  @Autowired
  AlphaMetricsEndpoint alphaMetricsEndpoint;

//  @Autowired(required = false)
//  TransactionRepository transactionRepository;

  public AlphaMetrics getMetrics() {
    AlphaMetrics alphaMetrics = new AlphaMetrics();
    alphaMetrics.setMetrics(alphaMetricsEndpoint.getMetrics());
    alphaMetrics.setNodeType(alphaMetricsEndpoint.getNodeType());
    return alphaMetrics;
  }

  @Override
  public Map<String, Long> getTransactionStatistics() {
    return null;
  }

//  public GlobalTransaction getTransactionByGlobalTxId(String globalTxId)
//      throws Exception {
//    GlobalTransaction globalTransaction = transactionRepository
//        .getGlobalTransactionByGlobalTxId(globalTxId);
//    return globalTransaction;
//  }

//  public PagingGlobalTransactions getTransactions(String state, int page, int size)
//      throws Exception {
//    PagingGlobalTransactions pagingGlobalTransactions = transactionRepository
//        .getGlobalTransactions(state, page, size);
//    return pagingGlobalTransactions;
//  }

//  public Map<String, Long> getTransactionStatistics() {
//    return transactionRepository.getTransactionStatistics();
//  }

//  public List<GlobalTransaction> getSlowTransactions(int size) {
//    return transactionRepository.getSlowGlobalTransactionsTopN(size);
//  }
}
