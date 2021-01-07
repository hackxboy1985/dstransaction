

package cn.ds.transaction.spring;

import cn.ds.transaction.transfer.TransactionAspectConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SagaSpringConfig.class,TransactionAspectConfig.class})
@ConditionalOnProperty(value = {"saga.enabled"}, matchIfMissing = true)
public class SagaSpringAutoConfiguration {
}
