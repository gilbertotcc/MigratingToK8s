package liveproject.m2k8s.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import liveproject.m2k8s.Profile;
import liveproject.m2k8s.data.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProfileControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileRepository profileRepository;

    @Test
    public void shouldGetProfile() throws Exception {
        Profile profile = aProfile();
        when(profileRepository.findByUsername(any())).thenReturn(Optional.of(profile));

        String savedJson = objectMapper.writeValueAsString(profile);

        mockMvc.perform(get("/profile/cheese"))
                .andExpect(status().isOk())
                .andExpect(content().json(savedJson, true));
    }

    @Test
    public void shouldCreateProfile() throws Exception {
        Profile profile = aProfile();
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        String profileJson = objectMapper.writeValueAsString(aProfileRequestBody());

        mockMvc.perform(post("/profile/cheese")
                .content(profileJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(profileRepository, atLeastOnce()).save(any(Profile.class));
    }

    private CreateProfileRequestBody aProfileRequestBody() {
        return CreateProfileRequestBody.builder()
                .password("stilton")
                .firstName("Chuck")
                .lastName("Cheese")
                .email("cecheese@example.com")
                .build();
    }

    private Profile aProfile() {
        return Profile.builder()
                .id(24L)
                .username("cheese")
                .password("stilton")
                .firstName("Chuck")
                .lastName("Cheese")
                .email("cecheese@example.com")
                .build();
    }
}