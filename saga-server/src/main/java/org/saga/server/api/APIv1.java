

package org.saga.server.api;


import org.saga.server.metrics.AlphaMetrics;
import org.saga.server.metrics.AlphaMetrics;

import java.util.Map;

public interface APIv1 {

    AlphaMetrics getMetrics();

//  GlobalTransaction getTransactionByGlobalTxId(String globalTxId)
//      throws Exception;
//
//  PagingGlobalTransactions getTransactions(String state, int page, int size)
//      throws Exception;

    Map<String, Long> getTransactionStatistics();

//  List<GlobalTransaction> getSlowTransactions(int size);
}
