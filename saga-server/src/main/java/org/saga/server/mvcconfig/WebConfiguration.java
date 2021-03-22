

package org.saga.server.mvcconfig;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(value = {"saga.rest.cors.enabled"})
public class WebConfiguration implements WebMvcConfigurer {

  /**
   * Allow saga-frontend cross-domain access
   * */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedMethods("*")
        .allowedOrigins("*")
        .allowedHeaders("authorization", "Accept");
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureContentNegotiation(
      ContentNegotiationConfigurer contentNegotiationConfigurer) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer asyncSupportConfigurer) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureDefaultServletHandling(
      DefaultServletHandlerConfigurer defaultServletHandlerConfigurer) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addFormatters(FormatterRegistry formatterRegistry) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addInterceptors(InterceptorRegistry interceptorRegistry) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addViewControllers(ViewControllerRegistry viewControllerRegistry) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {
    //compatible with spring-webmvc 4.x from spring boot 1.x
  }

  @Override
  public Validator getValidator() {
    //compatible with spring-webmvc 4.x from spring boot 1.x
    return null;
  }

  @Override
  public MessageCodesResolver getMessageCodesResolver() {
    //compatible with spring-webmvc 4.x from spring boot 1.x
    return null;
  }
}