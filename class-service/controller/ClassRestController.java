package com.stacklog.class_service.controller;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.class_service.model.entities.Classes;
import com.stacklog.class_service.model.service.ClassService;
import com.stacklog.class_service.model.service.GroupsStudentService;
import com.stacklog.class_service.model.service.SemesterService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/class")
public class ClassRestController {

    @Autowired
    ClassService classService;

    @Autowired
    SemesterService semesterService;

    @Autowired
    GroupsStudentService groupsStudentService;

    @GetMapping("/{semesterId}")
    public ResponseEntity<List<Classes>> getClassesByUserId(@RequestHeader("Authorization") String token,
            @PathVariable(name = "semesterId") String semesterId) {
        List<Classes> classes = classService.getAllBySemesterNUserId(token, semesterId);
        return ResponseEntity.ok().body(classes);
    }

    @GetMapping("/getalladmin")
    public ResponseEntity<List<Classes>> getClassesByAdmin(@RequestHeader("Authorization") String token,
            @RequestParam(name = "semesterId") String semesterId) {
        List<Classes> classes = classService.getAll(semesterId);
        return ResponseEntity.ok().body(classes);
    }
    

    @PostMapping(path = "")
    public ResponseEntity<Classes> saveClasses(@RequestBody Classes classes,
            @RequestHeader("Authorization") String token, @RequestParam(name = "semesterId", required = false) String semesterId) {
        if (semesterId == null) {
            if (semesterService.getById(semesterId, token) == null) {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.badRequest().body(null);
        }
        classes.setSemester(semesterService.getById(semesterId, token));
        Classes newClasses = classService.save(classes, token);
        return ResponseEntity.ok(newClasses);
    }

    @DeleteMapping(path = "")
    public ResponseEntity<String> deleteClasses(@RequestHeader("Authorization") String token,
            @RequestParam(name = "classId", required = false) String classesId) {
        if (classesId == null || classesId.isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        Classes classes = classService.delete(classesId, token);
        return ResponseEntity.ok("Delete class" + classes.getClassesName() + " successfully ");
    }

    @GetMapping("/join")
    public ResponseEntity<String> getJoinClass(@RequestHeader("Authorization") String token,
            @RequestParam(name = "code", required = false) String code) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Your code is wrong!");
        }
        String classId = decodeInviteCode(code);
        Classes classes = classService.getById(classId, token);
        if (classes == null) {
            return ResponseEntity.badRequest().body("Your class is not exist!");
        }
        if (groupsStudentService.checkExistGroupStudent(classes, token)) {
            return ResponseEntity.badRequest().body("You were in class!");
        }
        groupsStudentService.joinClass(classId, token);
        return ResponseEntity.ok("You have joined the class");
    }

    @GetMapping("/generateInviteCode/{classesId}")
    public ResponseEntity<String> genInviteCode(@RequestHeader("Authorization") String token,
            @PathVariable(name = "classesId", required = false) String classesId) {
        if (classesId == null || classesId.isEmpty()) {
            return ResponseEntity.badRequest().body("Your code is wrong!");
        }
        Classes classes = classService.getById(classesId, token);
        if (classes == null) {
            return ResponseEntity.badRequest().body("Your class is not exist!");
        }
        return ResponseEntity
                .ok("https://stacklog.id.vn/api/class/class/join?code=" + generateInviteCodeFromClassId(classesId));
    }

    private String generateInviteCodeFromClassId(String classesId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(classesId.getBytes());
    }

    private String decodeInviteCode(String code) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(code);
        return new String(decodedBytes);
    }

}