//package org.saga.server.jpa;
//
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
//import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
//import org.springframework.transaction.jta.JtaTransactionManager;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
////用eclipselink来取代hibernate https://blog.csdn.net/weixin_41751625/article/details/107481271
//@Configuration
//public class EclipseLinkJpaConfiguration extends JpaBaseConfiguration {
//  EclipseLinkJpaConfiguration(DataSource dataSource,
//                              JpaProperties properties,
//                              ObjectProvider<JtaTransactionManager> jtaTransactionManagerProvider,
//                              ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
//    super(dataSource, properties, jtaTransactionManagerProvider, transactionManagerCustomizers);
//  }
//
//  @Override
//  protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
//    return new EclipseLinkJpaVendorAdapter();
//  }
//
//  @Override
//  protected Map<String, Object> getVendorProperties() {
//    Map<String, Object> props = new HashMap<>();
//    props.put("eclipselink.weaving", "false");
//    props.put("eclipselink.logging.logger", "JavaLogger");
//    return props;
//  }
//}
