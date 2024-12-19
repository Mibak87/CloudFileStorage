package main.cloudfilestorage;

import main.cloudfilestorage.exception.NonUniqueUserNameException;
import main.cloudfilestorage.model.User;
import main.cloudfilestorage.repository.UserRepository;
import main.cloudfilestorage.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class RegistrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerNewUser_addNewUserToBD() {
        User newUser = new User("aaa","123");
        try {
            userService.register(newUser);
        } catch (NonUniqueUserNameException e) {

        }
       assertEquals("aaa",userRepository.findByUsername("aaa").get().getUsername());

    }

    @Test
    void registerNonUniqueUser_UniqueUserNameException() {
        User newUser = new User("aaa","123");
        try {
            userService.register(newUser);
        } catch (NonUniqueUserNameException e) {

        }
        User nonUniqueUser = new User("aaa","234");
        Assertions.assertThrows(NonUniqueUserNameException.class, () -> {
            userService.register(nonUniqueUser);
        });
    }
}
