

package org.saga.server.cluster.master.provider.jdbc;

import org.saga.server.cluster.master.provider.LockProvider;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLockRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "saga.cluster.master.enabled", havingValue = "true")
public class LockProviderJdbcConfiguration {

  @Bean
  public MasterLockRepository springElectionRepository(MasterLockEntityRepository electionRepo) {
    return new SpringMasterLockRepository(electionRepo);
  }

  @Primary
  @Bean
  @ConditionalOnProperty(name = "saga.cluster.master.type", havingValue = "jdbc", matchIfMissing = true)
  public LockProvider lockProvider(MasterLockRepository electionRepo) {
    return new JdbcLockProvider(electionRepo);
  }
}
