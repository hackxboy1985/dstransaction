

package cn.ds.transaction.transfer;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.aspect.SagaStartAspect;
import cn.ds.transaction.framework.aspect.TransactionAspect;
import cn.ds.transaction.framework.compensable.CompensableAnnotationProcessor;
import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.messageHandlerImpl.CompensationMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class TransactionAspectConfig {

  @Bean
  MessageHandler messageHandler(SagaMessageSender sender,
                                @Qualifier("compensationContext") CallbackContext context, OmegaContext omegaContext) {
    return new CompensationMessageHandler(sender, context);
  }

  @Bean
  SagaStartAspect sagaStartAspect(SagaMessageSender sender, OmegaContext context) {
    return new SagaStartAspect(sender, context);
  }

  @Bean
  TransactionAspect transactionAspect(SagaMessageSender sender, OmegaContext context) {
    return new TransactionAspect(sender, context);
  }

  @Bean
  CompensableAnnotationProcessor compensableAnnotationProcessor(OmegaContext omegaContext,
                                                                @Qualifier("compensationContext") CallbackContext compensationContext) {
    return new CompensableAnnotationProcessor(omegaContext, compensationContext);
  }

}
