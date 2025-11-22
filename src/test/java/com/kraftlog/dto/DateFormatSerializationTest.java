package com.kraftlog.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for date format serialization/deserialization
 */
@JsonTest
@ActiveProfiles("test")
class DateFormatSerializationTest {

    @Autowired
    private JacksonTester<RegisterRequest> registerRequestJson;

    @Autowired
    private JacksonTester<UserResponse> userResponseJson;

    @Autowired
    private JacksonTester<UserCreateRequest> userCreateRequestJson;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize LocalDate to dd-MM-yyyy format")
    void shouldSerializeLocalDateToCorrectFormat() throws Exception {
        // Given
        UserResponse user = UserResponse.builder()
                .name("Test")
                .surname("User")
                .birthDate(LocalDate.of(1986, 4, 8))
                .email("test@example.com")
                .weightKg(75.0)
                .heightCm(180.0)
                .createdAt(LocalDateTime.of(2025, 11, 22, 10, 30, 45))
                .updatedAt(LocalDateTime.of(2025, 11, 22, 10, 30, 45))
                .build();

        // When
        String json = userResponseJson.write(user).getJson();

        // Then
        assertThat(json).contains("\"birthDate\":\"08-04-1986\"");
        assertThat(json).contains("\"createdAt\":\"22-11-2025 10:30:45\"");
        assertThat(json).contains("\"updatedAt\":\"22-11-2025 10:30:45\"");
    }

    @Test
    @DisplayName("Should deserialize dd-MM-yyyy format to LocalDate")
    void shouldDeserializeDdMmYyyyToLocalDate() throws Exception {
        // Given
        String json = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test@example.com",
                    "password": "password123",
                    "birthDate": "08-04-1986",
                    "weightKg": 75.0,
                    "heightCm": 180.0
                }
                """;

        // When
        RegisterRequest request = registerRequestJson.parse(json).getObject();

        // Then
        assertThat(request.getBirthDate()).isEqualTo(LocalDate.of(1986, 4, 8));
        assertThat(request.getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should fail to deserialize yyyy-MM-dd format")
    void shouldFailToDeserializeIsoFormat() {
        // Given
        String json = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test@example.com",
                    "password": "password123",
                    "birthDate": "1986-04-08",
                    "weightKg": 75.0,
                    "heightCm": 180.0
                }
                """;

        // When & Then
        assertThatThrownBy(() -> registerRequestJson.parse(json))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cannot deserialize");
    }

    @Test
    @DisplayName("Should deserialize various valid dd-MM-yyyy dates")
    void shouldDeserializeVariousValidDates() throws Exception {
        // Test case 1: First day of year
        String json1 = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test1@example.com",
                    "password": "password123",
                    "birthDate": "01-01-2000",
                    "weightKg": 70.0,
                    "heightCm": 175.0
                }
                """;
        RegisterRequest request1 = registerRequestJson.parse(json1).getObject();
        assertThat(request1.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));

        // Test case 2: Last day of year
        String json2 = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test2@example.com",
                    "password": "password123",
                    "birthDate": "31-12-1999",
                    "weightKg": 70.0,
                    "heightCm": 175.0
                }
                """;
        RegisterRequest request2 = registerRequestJson.parse(json2).getObject();
        assertThat(request2.getBirthDate()).isEqualTo(LocalDate.of(1999, 12, 31));

        // Test case 3: Leap year date
        String json3 = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test3@example.com",
                    "password": "password123",
                    "birthDate": "29-02-2000",
                    "weightKg": 70.0,
                    "heightCm": 175.0
                }
                """;
        RegisterRequest request3 = registerRequestJson.parse(json3).getObject();
        assertThat(request3.getBirthDate()).isEqualTo(LocalDate.of(2000, 2, 29));
    }

    @Test
    @DisplayName("Should fail to deserialize invalid dates")
    void shouldFailToDeserializeInvalidDates() {
        // Test case 1: Invalid day (32)
        String json1 = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test@example.com",
                    "password": "password123",
                    "birthDate": "32-01-2000",
                    "weightKg": 70.0,
                    "heightCm": 175.0
                }
                """;
        assertThatThrownBy(() -> registerRequestJson.parse(json1))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cannot deserialize");

        // Test case 2: Invalid month (13)
        String json2 = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test@example.com",
                    "password": "password123",
                    "birthDate": "15-13-2000",
                    "weightKg": 70.0,
                    "heightCm": 175.0
                }
                """;
        assertThatThrownBy(() -> registerRequestJson.parse(json2))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cannot deserialize");
    }

    @Test
    @DisplayName("Should serialize and deserialize UserCreateRequest correctly")
    void shouldSerializeAndDeserializeUserCreateRequest() throws Exception {
        // Given
        UserCreateRequest original = UserCreateRequest.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .password("password123")
                .weightKg(75.5)
                .heightCm(180.0)
                .build();

        // When - serialize
        String json = userCreateRequestJson.write(original).getJson();

        // Then - check serialization
        assertThat(json).contains("\"birthDate\":\"15-05-1990\"");

        // When - deserialize
        UserCreateRequest deserialized = userCreateRequestJson.parse(json).getObject();

        // Then - check deserialization
        assertThat(deserialized.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(deserialized.getName()).isEqualTo("John");
        assertThat(deserialized.getSurname()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should handle null dates correctly")
    void shouldHandleNullDates() throws Exception {
        // Given
        String json = """
                {
                    "name": "Test",
                    "surname": "User",
                    "email": "test@example.com",
                    "password": "password123",
                    "birthDate": null,
                    "weightKg": 75.0,
                    "heightCm": 180.0
                }
                """;

        // When
        RegisterRequest request = registerRequestJson.parse(json).getObject();

        // Then
        assertThat(request.getBirthDate()).isNull();
        assertThat(request.getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should serialize LocalDateTime in dd-MM-yyyy HH:mm:ss format")
    void shouldSerializeLocalDateTimeCorrectly() throws Exception {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 22, 14, 30, 45);
        UserResponse user = UserResponse.builder()
                .name("Test")
                .surname("User")
                .birthDate(LocalDate.of(1986, 4, 8))
                .email("test@example.com")
                .createdAt(dateTime)
                .updatedAt(dateTime)
                .build();

        // When
        String json = objectMapper.writeValueAsString(user);

        // Then
        assertThat(json).contains("\"createdAt\":\"22-11-2025 14:30:45\"");
        assertThat(json).contains("\"updatedAt\":\"22-11-2025 14:30:45\"");
    }

    @Test
    @DisplayName("Should maintain consistency between serialization and deserialization")
    void shouldMaintainConsistencyBetweenSerializationAndDeserialization() throws Exception {
        // Given
        LocalDate originalDate = LocalDate.of(1995, 12, 25);
        UserCreateRequest original = UserCreateRequest.builder()
                .name("Consistency")
                .surname("Test")
                .birthDate(originalDate)
                .email("consistency@example.com")
                .password("password123")
                .weightKg(70.0)
                .heightCm(175.0)
                .build();

        // When - serialize then deserialize
        String json = objectMapper.writeValueAsString(original);
        UserCreateRequest roundTrip = objectMapper.readValue(json, UserCreateRequest.class);

        // Then
        assertThat(roundTrip.getBirthDate()).isEqualTo(originalDate);
        assertThat(json).contains("\"birthDate\":\"25-12-1995\"");
    }
}
