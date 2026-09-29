package vn.iotstar.bt10;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.bt10.dto.LoginUserDto;
import vn.iotstar.bt10.dto.RegisterUserDto;
import vn.iotstar.bt10.entity.User;
import vn.iotstar.bt10.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Bt10ApplicationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String jwtToken;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @Order(2)
    void testSignupSuccess() throws Exception {
        RegisterUserDto registerDto = RegisterUserDto.builder()
                .email("testuser@example.com")
                .password("Password123!")
                .fullName("Nguyen Van Test")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Test"));

        // Verify password in DB is encrypted by BCrypt
        User savedUser = userRepository.findByEmail("testuser@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @Order(3)
    void testSignupDuplicateEmail() throws Exception {
        RegisterUserDto duplicateDto = RegisterUserDto.builder()
                .email("testuser@example.com")
                .password("Password123!")
                .fullName("Nguyen Van Test Duplicate")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @Order(4)
    void testLoginSuccess() throws Exception {
        LoginUserDto loginDto = LoginUserDto.builder()
                .email("testuser@example.com")
                .password("Password123!")
                .build();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").value(3600000L))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = json.get("token").asText();
        assertThat(jwtToken).isNotEmpty();
    }

    @Test
    @Order(5)
    void testLoginBadCredentials() throws Exception {
        LoginUserDto badDto = LoginUserDto.builder()
                .email("testuser@example.com")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Xác thực thất bại"))
                .andExpect(jsonPath("$.detail").value("Email hoặc mật khẩu không chính xác."));
    }

    @Test
    @Order(6)
    void testGetAuthenticatedUserWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @Order(7)
    void testGetAuthenticatedUserWithToken() throws Exception {
        assertThat(jwtToken).isNotNull();

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Test"));
    }

    @Test
    @Order(8)
    void testGetAllUsersWithToken() throws Exception {
        assertThat(jwtToken).isNotNull();

        mockMvc.perform(get("/users/")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("testuser@example.com"));
    }

    @Test
    @Order(9)
    void testAccessWithInvalidToken() throws Exception {
        String invalidToken = jwtToken + "invalid-signature";

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").exists());
    }
}
