

package cn.ds.transaction.framework.aspect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.context.TransactionContext;
import cn.ds.transaction.framework.annotations.Compensable;
import cn.ds.transaction.framework.contextHelper.TransactionContextHelper;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.recovery.RecoveryPolicy;
import cn.ds.transaction.framework.recovery.RecoveryPolicyFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

@Aspect
@Order(value = 200)
public class TransactionAspect extends TransactionContextHelper {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SagaContext context;

  private final CompensableInterceptor interceptor;

  public TransactionAspect(SagaMessageSender sender, SagaContext context) {
    this.context = context;
    this.context.verify();
    this.interceptor = new CompensableInterceptor(context, sender);
  }
  @Around("execution(@cn.ds.transaction.framework.annotations.Compensable * *(..)) && @annotation(compensable)")
  Object advise(ProceedingJoinPoint joinPoint, Compensable compensable) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    // just check if we need to setup the transaction context information first
    TransactionContext transactionContext = extractTransactionContext(joinPoint.getArgs());
    if (transactionContext != null) {
      populateSagaContext(context, transactionContext);
    }
    // SCB-1011 Need to check if the globalTxId transaction is null to avoid the message sending failure
    if (context.globalTxId() == null) {
      throw new SagaException("Cannot find the globalTxId from SagaContext. Please using @SagaStart to start a global transaction.");
    }
    String localTxId = context.localTxId();
    context.newLocalTxId();
    LOG.debug("Updated context {} for compensable method {} ", context, method.toString());

    //TODO:根据向前尝试次数决定是向前重试，还是向后回滚
    //TODO:向前是重试，重试得有重试次数，向后是恢复(回滚)
    int forwardRetries = compensable.forwardRetries();
    RecoveryPolicy recoveryPolicy = RecoveryPolicyFactory.getRecoveryPolicy(forwardRetries);
    try {
      return recoveryPolicy.apply(joinPoint, compensable, interceptor, context, localTxId, forwardRetries);
    } finally {
      context.setLocalTxId(localTxId);
      LOG.debug("Restored context back to {}", context);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

}
