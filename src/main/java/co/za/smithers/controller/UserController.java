package co.za.smithers.controller;

import co.za.smithers.controller.dto.ListResponse;
import co.za.smithers.controller.dto.UserRequest;
import co.za.smithers.domain.User;
import co.za.smithers.exceptions.DuplicateEmailException;
import co.za.smithers.exceptions.NoContentException;
import co.za.smithers.exceptions.UserDoesNotExistException;
import co.za.smithers.mapping.UserMapper;
import co.za.smithers.repository.UserRepository;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    //Should be paginated
    @GetMapping
    public ListResponse<User> getUsers() {
        return new ListResponse<>(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") long id) {
        return userRepository.findById(id).orElseThrow(NoContentException::new);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody UserRequest user) {
        return saveUser(userMapper.map(user));
    }

    @PutMapping("/{id}")
    public User editUser(@PathVariable("id") long id, @Valid @RequestBody UserRequest user) {
        userRepository.findById(id).orElseThrow(UserDoesNotExistException::new);
        final var userUpdate = userMapper.map(user);
        userUpdate.setId(id);
        return saveUser(userUpdate);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            log.debug("User requested for deletion does not exist: ", e);
        }
    }

    private User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Constraint triggered: ", e);
            throw new DuplicateEmailException();
        }
    }
}
