package com.blog.config;

import jakarta.servlet.*;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    // Создаём Root Application Context
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.register(RootConfig.class);

    // Регистрируем ContextLoaderListener
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Создаём Servlet Application Context для DispatcherServlet
    AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext();
    servletAppContext.register(WebConfig.class);

    // Регистрируем и настраиваем DispatcherServlet
    DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping("/api/*");
    dispatcher.setAsyncSupported(true);

    // Добавляем Character Encoding Filter для UTF-8
    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);

    FilterRegistration.Dynamic encodingFilter = servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
    encodingFilter.addMappingForUrlPatterns(null, false, "/*");

    // Устанавливаем параметры для multipart
    servletContext.setSessionTrackingModes(java.util.EnumSet.of(SessionTrackingMode.COOKIE));
  }
}
