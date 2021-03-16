

package org.saga.server.txtimeout;

import static org.saga.server.common.TaskStatus.PENDING;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

public class SpringTxTimeoutRepository implements TxTimeoutRepository {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TxTimeoutEntityRepository timeoutRepo;

  public SpringTxTimeoutRepository(TxTimeoutEntityRepository timeoutRepo) {
    this.timeoutRepo = timeoutRepo;
  }

  @Override
  public void save(TxTimeout timeout) {
    try {
      timeoutRepo.save(timeout);
    } catch (Exception ignored) {
      LOG.warn("Failed to save some timeout {}", timeout);
    }
  }

  @Override
  public void markTimeoutAsDone() {
    timeoutRepo.updateStatusOfFinishedTx();
  }

  @Transactional
  @Override
  public List<TxTimeout> findFirstTimeout() {
    List<TxTimeout> timeoutEvents = timeoutRepo.findFirstTimeoutTxOrderByExpireTimeAsc(PageRequest.of(0, 1));
    timeoutEvents.forEach(event -> {
      timeoutRepo
        .updateStatusByGlobalTxIdAndLocalTxId(PENDING.name(), event.globalTxId(), event.localTxId());
    });
    return timeoutEvents;
  }
}
