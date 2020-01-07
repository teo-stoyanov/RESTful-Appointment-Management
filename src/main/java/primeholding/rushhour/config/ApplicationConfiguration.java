package primeholding.rushhour.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import primeholding.rushhour.models.ModelMapper;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ModelMapper getModelMapper() {
        return Mappers.getMapper(ModelMapper.class);
    }
}