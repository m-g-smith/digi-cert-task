package co.za.smithers.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users", indexes = {
        @Index(name = "user_first_name_idx", columnList = "firstName"),
        @Index(name = "user_last_name_idx", columnList = "lastName"),
        @Index(name = "user_email_idx", columnList = "email", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
