package ca.gc.aafc.objectstore.api.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSources {
    
    @Bean(name = "validationMessageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
        = new ReloadableResourceBundleMessageSource();
        
        messageSource.setBasename("classpath:messages/validation/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}