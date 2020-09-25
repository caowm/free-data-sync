package data.sync.mq.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
* Spring MVC Configure
*/
@Configuration
public class WebMvcConfigurer {

  @Bean
  public HttpMessageConverters fastJsonHttpMessageConverters() {
      FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

      FastJsonConfig config = new FastJsonConfig();
      config.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      config.setSerializerFeatures(
      		SerializerFeature.WriteMapNullValue  
//      		,SerializerFeature.PrettyFormat
//      		,SerializerFeature.SortField
      ); 
      
      List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
      fastMediaTypes.add(MediaType.APPLICATION_JSON);
      converter.setSupportedMediaTypes(fastMediaTypes);
      converter.setFastJsonConfig(config);
      
      return new HttpMessageConverters(converter);
  }

}
