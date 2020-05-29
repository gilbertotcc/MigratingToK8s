package liveproject.m2k8s.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileNotFound extends RuntimeException {
    private static final long serialVersionUID = 2655207035858201578L;
}
