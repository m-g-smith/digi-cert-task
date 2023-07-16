package co.za.smithers.controller.unit;


import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.za.smithers.configuration.UserControllerTestConfiguration;
import co.za.smithers.controller.UserController;
import co.za.smithers.controller.UserControllerBaseTest;
import co.za.smithers.controller.dto.ListResponse;
import co.za.smithers.controller.dto.UserRequest;
import co.za.smithers.domain.User;
import co.za.smithers.mapping.UserMapper;
import co.za.smithers.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@Import(UserController.class)
@ContextConfiguration(classes = UserControllerTestConfiguration.class)
public class UnitUserControllerTest extends UserControllerBaseTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Before
    public void beforeTest() {
        reset(userRepository);
    }

    @Override
    protected MockMvc getMvc() {
        return this.mvc;
    }

    @Override
    protected UserRepository getUserRepository() {
        return this.userRepository;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Test
    @Override
    public void testCreateUser() throws Exception {
        mockSave();
        createRequestUser();
        super.testCreateUser();
    }

    @Override
    public void testGetUser_doesNotExist() throws Exception {
        when(userRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());
        super.testGetUser();
    }

    @Override
    public void testGetAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(createUsers(5));
        super.testGetAllUsers();
    }

    @Override
    public void testGetAllUsers_empty() throws Exception {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        super.testGetAllUsers_empty();
    }

    @Test
    public void deleteUser() throws Exception {
        final var request = delete("/users/" + 1L);
        getMvc().perform(request)
                .andExpect(status().isOk())
                .andReturn();
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void deleteUser_doesNotExist() throws Exception {
        doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(1L);
        super.deleteUser_doesNotExist();
    }

    @Test
    public void editUser() throws Exception {
        mockSave();
        //initial select mock
        when(userRepository.findById(1L)).thenReturn(Optional.of(userMapper.map(createUserRequest())));

        //assert select mock
        final var user = userMapper.map(createUserRequest());
        user.setId(1L);
        user.setEmail("newEmail@gmail.com");
        user.setFirstName("newFirstName");
        user.setLastName("newLastName");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        super.editUser();
    }

    @Test
    public void editUser_doesNotExist() throws Exception {
        //initial select mock
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        super.editUser_doesNotExist();
    }

    @Test
    public void testCreateUser_duplicateEmail() throws Exception {
        when(userRepository.save(ArgumentMatchers.any())).thenThrow(new DataIntegrityViolationException("error"));
        super.testCreateUser_duplicateEmail();
    }

    @Test
    public void editUser_duplicateEmail() throws Exception {
        final var user = createUserObject(0);
        user.setId(1L);
        when(userRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(user));
        when(userRepository.save(ArgumentMatchers.any()))
                .thenReturn(user)
                .thenReturn(user)
                .thenThrow(new DataIntegrityViolationException("error"));
        super.editUser_duplicateEmail();
    }


    private void mockSave() {
        when(userRepository.save(ArgumentMatchers.any()))
                .then(inv -> {
                    var input = (User) inv.getArguments()[0];
                    input.setId(1L);
                    return input;
                });
    }

    private void createRequestUser() {
        final var user = userMapper.map(createUserRequest());
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    protected User createUser() {
        final var user = createUserObject(0);
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        return user;
    }

    protected List<User> createUsers(int userCount) {
        var users = new ArrayList<User>();
        for (int index = 0; index < userCount; index++) {
            final User user = createUserObject(index);
            user.setId((long)index);
            users.add(user);
        }
        return users;
    }
}