package co.za.smithers.mapping;


import co.za.smithers.controller.dto.UserRequest;
import co.za.smithers.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User map(UserRequest input);
}
