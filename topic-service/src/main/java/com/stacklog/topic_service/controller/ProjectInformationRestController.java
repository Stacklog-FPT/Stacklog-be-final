package com.stacklog.topic_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.topic_service.model.entities.ProjectInformation;
import com.stacklog.topic_service.model.service.ProjectInformationService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping(path = "")
public class ProjectInformationRestController {
    
    @Autowired
    ProjectInformationService projectInformationService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ProjectInformation> getByGroupId(@PathVariable(name = "groupId", required = false) String groupId,
            @RequestHeader("Authorization") String token) {
        ProjectInformation pi = projectInformationService.getByGroupId(groupId, token);
        if (pi == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok().body(pi);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ProjectInformation>> getByClassId(@PathVariable(name = "classId", required = false) String classId, 
            @RequestHeader("Authorization") String token) {
        List<ProjectInformation> piList = projectInformationService.getAllByClassId(classId, token);
        return ResponseEntity.ok().body(piList);
    }
    
    @PostMapping("")
    public ResponseEntity<ProjectInformation> saveProjectInformation(@RequestHeader("Authorization") String token,
            @RequestBody ProjectInformation projectInformation) {
        ProjectInformation pi = projectInformationService.save(projectInformation, token);
        if (pi == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok().body(pi);
    }
    
    @DeleteMapping("/{piId}")
    public ResponseEntity<String> removeProjectInformation(@RequestHeader("Authorization") String token,
            @PathVariable(name = "piId", required = false) String piId) {
        ProjectInformation pi = projectInformationService.delete(piId, token);
        if (pi == null) {
            return ResponseEntity.badRequest().body("Delete failed");
        }
        return ResponseEntity.ok().body("Delete success");
    }

}
