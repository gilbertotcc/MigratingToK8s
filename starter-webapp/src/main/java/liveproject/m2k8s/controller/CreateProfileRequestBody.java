package liveproject.m2k8s.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CreateProfileRequestBody {

    final String password;

    final String firstName;

    final String lastName;

    final String email;
}
