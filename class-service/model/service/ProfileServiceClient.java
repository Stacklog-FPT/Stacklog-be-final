package com.stacklog.class_service.model.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@FeignClient(name = "profile-service", url = "http://profileservice:2001/user", path = "")
public interface ProfileServiceClient {
    @GetMapping("/class/{classId}")
    List<Profile> getProfileByClassId(
            @RequestHeader("Authorization") String token,
            @PathVariable("classId") String classId);

    @PostMapping("/addAll")
    List<Profile> addProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Profile> profileList);
}

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Profile {
    private String _id;
    private String full_name;
    private String work_id;
    private String email;
    private boolean isActive;
}