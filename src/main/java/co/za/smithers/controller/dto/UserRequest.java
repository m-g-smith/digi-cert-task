package co.za.smithers.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "First Name can not be blank")
    private String firstName;
    @NotBlank(message = "Last Name can not be blank")
    private String lastName;
    @Email
    @NotBlank(message = "Email can not be blank")
    private String email;
}
