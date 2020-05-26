package liveproject.m2k8s.controller;

import liveproject.m2k8s.Profile;
import liveproject.m2k8s.data.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.String.format;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;

    @Value("${images.directory:/tmp}")
    private String uploadFolder;

    @Value("classpath:ghost.jpg")
    private Resource defaultImage;

    @PostMapping(value = "/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Profile createProfile(
            @PathVariable("username") String username,
            @RequestBody CreateProfileRequestBody createProfileRequestBody) {
        log.debug("Creating profile for user {}", username);
        Profile profile = Profile.builder()
                .username(username)
                .password(createProfileRequestBody.getPassword())
                .email(createProfileRequestBody.getEmail())
                .firstName(createProfileRequestBody.getFirstName())
                .lastName(createProfileRequestBody.getLastName())
                .build();
        return profileRepository.save(profile);
    }

    @GetMapping(value = "/{username}")
    @ResponseStatus(HttpStatus.OK)
    public Profile getProfile(@PathVariable String username) {
        log.debug(format("Reading profile for user %s", username));
        return profileRepository.findByUsername(username);
    }

    @PutMapping(value = "/{username}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Profile updateProfile(
            @PathVariable String username,
            @RequestBody CreateProfileRequestBody createProfileRequestBody) {
        log.debug("Updating profile for user {}", username);
        Profile dbProfile = profileRepository.findByUsername(username);
        boolean dirty = false;
        if (!StringUtils.isEmpty(createProfileRequestBody.getEmail())
                && !createProfileRequestBody.getEmail().equals(dbProfile.getEmail())) {
            dbProfile.setEmail(createProfileRequestBody.getEmail());
            dirty = true;
        }
        if (!StringUtils.isEmpty(createProfileRequestBody.getFirstName())
                && !createProfileRequestBody.getFirstName().equals(dbProfile.getFirstName())) {
            dbProfile.setFirstName(createProfileRequestBody.getFirstName());
            dirty = true;
        }
        if (!StringUtils.isEmpty(createProfileRequestBody.getLastName())
                && !createProfileRequestBody.getLastName().equals(dbProfile.getLastName())) {
            dbProfile.setLastName(createProfileRequestBody.getLastName());
            dirty = true;
        }
        if (dirty) {
            profileRepository.save(dbProfile);
        }
        return dbProfile;
    }

    @GetMapping(value = "/{username}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public byte[] getProfileImage(@PathVariable String username) throws IOException {
        log.debug("Reading image for user {}", username);
        Profile profile = profileRepository.findByUsername(username);
        if ((profile == null) || StringUtils.isEmpty(profile.getImageFileName())) {
            try (InputStream inputStream = defaultImage.getInputStream()) {
                return IOUtils.toByteArray(inputStream);
            }
        }
        try (InputStream inputStream = new FileInputStream(profile.getImageFileName())) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    @PostMapping(value = "/{username}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public Profile uploadProfileImage(
            @PathVariable String username,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.debug("Updating image for user {}", username);
        Optional<String> optionalFilename = Optional.ofNullable(file)
                .map(MultipartFile::getOriginalFilename)
                .filter(filename -> filename.endsWith("jpg") || filename.endsWith("JPG"));
        if (!optionalFilename.isPresent()) {
            return getProfile(username);
        }
        try {
            final String contentType = file.getContentType();
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadFolder, format("%s.jpg", username));
            Files.write(path, bytes);
            Profile profile = profileRepository.findByUsername(username);
            profile.setImageFileName(path.toString());
            profile.setImageFileContentType(contentType);
            return profileRepository.save(profile);
        } catch (IOException e) {
            log.error("Error occurred while updating file for user {}", username);
            throw e;
        }
    }

}
