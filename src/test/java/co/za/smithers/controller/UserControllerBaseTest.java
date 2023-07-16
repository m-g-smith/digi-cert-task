package co.za.smithers.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.za.smithers.controller.dto.ListResponse;
import co.za.smithers.controller.dto.UserRequest;
import co.za.smithers.domain.User;
import co.za.smithers.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

public abstract class UserControllerBaseTest {
    protected abstract MockMvc getMvc();
    protected abstract UserRepository getUserRepository();
    protected abstract ObjectMapper getObjectMapper();

    @Test
    public void testCreateUser() throws Exception {
        final var user = createUserRequest();
        final var request = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        final var result = getMvc().perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andReturn();
        final var userResponse = getObjectMapper().readValue(result.getResponse().getContentAsString(), User.class);
        final var loadedUser = loadUser(userResponse);
        assertEquals(userResponse,loadedUser);
    }

    @Test
    public void testCreateUser_duplicateEmail() throws Exception {
        final var user = createUser();
        final var request = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateUser_blankFields() throws Exception {
        final var user = new UserRequest();
        final var request = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email can not be blank")))
                .andExpect(content().string(containsString("Last Name can not be blank")))
                .andExpect(content().string(containsString("First Name can not be blank")))
                .andReturn();
    }

    @Test
    public void testCreateUser_invalidEmail() throws Exception {
        final var user = createUserRequest();
        user.setEmail("Test");
        final var request = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must be a well-formed email address")))
                .andReturn();
    }

    @Test
    public void testGetUser() throws Exception {
        final User storedUser = createUser();
        final var request = get("/users/" + storedUser.getId());
        final var result = getMvc().perform(request)
                .andExpect(status().isOk())
                .andReturn();
        final var userResponse = getObjectMapper().readValue(result.getResponse().getContentAsString(), User.class);
        assertEquals(userResponse,storedUser);
    }

    @Test
    public void testGetUser_doesNotExist() throws Exception {
        final var request = get("/users/" + 1);
        getMvc().perform(request)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        final var users = createUsers(5);
        final var request = get("/users");
        final var result = getMvc().perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andReturn();
        final var usersResponse = getObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<ListResponse<User>>() {});
        assertTrue(usersResponse.getItems().containsAll(users));
    }

    @Test
    public void testGetAllUsers_empty() throws Exception {
        final var request = get("/users");
        getMvc().perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andReturn();
    }

    @Test
    public void deleteUser() throws Exception {
        final User storedUser = createUser();
        final var request = delete("/users/" + storedUser.getId());
        getMvc().perform(request)
                .andExpect(status().isOk())
                .andReturn();
        assertFalse(getUserRepository().findById(storedUser.getId()).isPresent());
    }

    @Test
    public void deleteUser_doesNotExist() throws Exception {
        final var request = delete("/users/" + 1);
        getMvc().perform(request)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void editUser() throws Exception {
        final var user = createUserObject(0);
        final var storedUser = getUserRepository().save(user);
        storedUser.setEmail("newEmail@gmail.com");
        storedUser.setFirstName("newFirstName");
        storedUser.setLastName("newLastName");
        final var request = put("/users/" + storedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        final var result = getMvc().perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andReturn();
        final var userResponse = getObjectMapper().readValue(result.getResponse().getContentAsString(), User.class);
        final var loadedUser = loadUser(userResponse);
        assertEquals(userResponse, loadedUser);
    }

    @Test
    public void editUser_duplicateEmail() throws Exception {
        final var user1 = createUserObject(0);
        final var storedUser1 = getUserRepository().save(user1);
        final var user2 = createUserObject(1);
        final var storedUser2 = getUserRepository().save(user2);

        final var request = put("/users/" + storedUser2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(storedUser1));
        getMvc().perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void editUser_doesNotExist() throws Exception {
        final var user = createUserRequest();
        final var request = put("/users/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void editUser_blankFields() throws Exception {
        final var user = new UserRequest();
        final var request = put("/users/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email can not be blank")))
                .andExpect(content().string(containsString("Last Name can not be blank")))
                .andExpect(content().string(containsString("First Name can not be blank")))
                .andReturn();
    }

    @Test
    public void editUser_badEmail() throws Exception {
        final var user = createUserRequest();
        user.setEmail("Test");
        final var request = put("/users/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(user));
        getMvc().perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must be a well-formed email address")))
                .andReturn();
    }

    private User loadUser(User userResponse) {
        return getUserRepository().findById(userResponse.getId()).orElseThrow();
    }

    protected User createUser() {
        final var user = createUserObject(0);
        return getUserRepository().save(user);
    }

    protected List<User> createUsers(int userCount) {
        var users = new ArrayList<User>();
        for (int index = 0; index < userCount; index++) {
            users.add(getUserRepository().save(createUserObject(index)));
        }
        return users;
    }

    protected static User createUserObject(int index) {
        final var user = new User();
        user.setFirstName("TestFirstName_" + index);
        user.setLastName("TestLastName_" + index);
        user.setEmail(String.format("TestEmail_%s@gmail.com", index));
        return user;
    }

    protected static UserRequest createUserRequest() {
        final var user = new UserRequest();
        user.setFirstName("TestFirstName");
        user.setLastName("TestLastName");
        user.setEmail("TestEmail@gmail.com");
        return user;
    }
}
