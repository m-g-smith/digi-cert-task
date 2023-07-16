package co.za.smithers.configuration;

import co.za.smithers.exceptions.handler.ArgumentExceptionHandler;
import co.za.smithers.mapping.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;

public class UserControllerTestConfiguration {
    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper( UserMapper.class );
    }

    @Bean
    public ArgumentExceptionHandler argumentExceptionHandler() {
        return new ArgumentExceptionHandler();
    }
}
