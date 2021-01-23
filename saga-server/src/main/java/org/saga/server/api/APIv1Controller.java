

package org.saga.server.api;

import java.util.List;
import java.util.Map;

import org.saga.server.metrics.AlphaMetrics;
import org.saga.server.metrics.AlphaMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alpha/api/v1")
public class APIv1Controller {

  @Autowired
  APIv1Impl APIv1Impl;

  @GetMapping(value = "/metrics")
  public ResponseEntity<AlphaMetrics> metrics() {
    return ResponseEntity.ok(APIv1Impl.getMetrics());
  }

//  @GetMapping(value = "/transaction/{globalTxId}")
//  public ResponseEntity<GlobalTransaction> getTransactionByGlobalTxId(@PathVariable String globalTxId)
//      throws Exception {
//    return ResponseEntity.ok(APIv1Impl.getTransactionByGlobalTxId(globalTxId));
//  }

//  @GetMapping(value = "/transaction")
//  public ResponseEntity<PagingGlobalTransactions> getTransactions(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
//      @RequestParam(value = "size", required = false, defaultValue = "50") int size)
//      throws Exception {
//    return ResponseEntity.ok(APIv1Impl.getTransactions(null,page,size));
//  }

  @GetMapping(value = "/transaction/statistics")
  public ResponseEntity<Map<String,Long>> getTransactionStatistics() {
    return ResponseEntity.ok(APIv1Impl.getTransactionStatistics());
  }

//  @GetMapping(value = "/transaction/slow")
//  public ResponseEntity<List<GlobalTransaction>> getSlowTransactions(@RequestParam(name="size", defaultValue = "10") int size) {
//    return ResponseEntity.ok(APIv1Impl.getSlowTransactions(size));
//  }
}
