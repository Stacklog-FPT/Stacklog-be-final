package com.stacklog.task_service.model.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@FeignClient(name = "class-service", url = "http://classservice:2003/group", path = "")
public interface ClassServiceClient {
    @GetMapping("/find/{semesterId}")
    List<Groupss> getGroupssBySemesterId(
            @RequestHeader("Authorization") String token,
            @PathVariable("semesterId") String semesterId);
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Groupss {
    private String groupsId; // đổi tên khớp với JSON trả về

    public String getGroupsId() { return groupsId; }
    public void setGroupsId(String groupsId) { this.groupsId = groupsId; }
}
