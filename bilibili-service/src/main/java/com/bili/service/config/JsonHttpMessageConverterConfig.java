package com.bili.service.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// @Configuration is used to indicate that a class contains bean definition for the application context.
// this class is used to create and configure the "HttpMessageConverters" bean.
@Configuration
public class JsonHttpMessageConverterConfig {
    @Bean
    // @Primary indicated that the bean should be given priority over any other beans of the same type
    // that may be defined in the application context
    @Primary
    public HttpMessageConverters fastJsonHttpConverters(){
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yy MM dd HH:mm:ss");
        config.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                // sometimes null strings are still needed to be displayed on the page as empty string;
                // the same with list, map, etc.
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.MapSortField,
                SerializerFeature.DisableCircularReferenceDetect
        );
        converter.setFastJsonConfig(config);
        return new HttpMessageConverters(converter);
      }
}
