package com.blog.config;

import jakarta.servlet.*;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    // Создаём папку для загрузок если её нет
    createUploadsDirectory();

    // Root context
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.register(RootConfig.class);
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Servlet context
    AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext();
    servletAppContext.register(WebConfig.class);

    // ===== ФИЛЬТРЫ =====
    // 1. CORS Filter
    SimpleCorsFilter corsFilter = new SimpleCorsFilter();
    FilterRegistration.Dynamic cors = servletContext.addFilter("corsFilter", corsFilter);
    cors.addMappingForUrlPatterns(null, false, "/*");

    // 2. Character Encoding Filter
    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);

    FilterRegistration.Dynamic encodingFilter = servletContext.addFilter(
        "characterEncodingFilter",
        characterEncodingFilter
    );
    encodingFilter.addMappingForUrlPatterns(null, false, "/*");

    // ===== DISPATCHER SERVLET =====
    DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping("/api/*");
    dispatcher.setAsyncSupported(true);

    // Multipart config - УВЕЛИЧИВАЕМ ЛИМИТЫ И НАСТРАИВАЕМ ПУТЬ
    String uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "blog-uploads";
    File uploadFolder = new File(uploadDir);
    if (!uploadFolder.exists()) {
      uploadFolder.mkdirs();
    }

    dispatcher.setMultipartConfig(new MultipartConfigElement(
        uploadDir,          // location - используем системную временную папку
        10485760L,          // maxFileSize - 10MB
        20971520L,          // maxRequestSize - 20MB
        1048576             // fileSizeThreshold - 1MB (хранить в памяти)
    ));

    servletContext.setSessionTrackingModes(java.util.EnumSet.of(SessionTrackingMode.COOKIE));

    System.out.println("✅ Blog Backend initialized successfully!");
    System.out.println("   - DispatcherServlet mapped to: /api/*");
    System.out.println("   - Upload directory: " + uploadDir);
    System.out.println("   - CORS Filter enabled: *");
    System.out.println("   - Multipart config: max 10MB file, 20MB request");
  }

  private void createUploadsDirectory() {
    try {
      File uploadsDir = new File("uploads");
      if (!uploadsDir.exists()) {
        boolean created = uploadsDir.mkdirs();
        System.out.println("📁 Uploads directory created: " + created + " at " + uploadsDir.getAbsolutePath());
      }
    } catch (Exception e) {
      System.err.println("❌ Failed to create uploads directory: " + e.getMessage());
    }
  }
}
