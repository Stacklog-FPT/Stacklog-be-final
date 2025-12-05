package com.stacklog.class_service.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.class_service.model.entities.Classes;
import com.stacklog.class_service.model.entities.GroupStudent;
import com.stacklog.class_service.model.entities.Groupss;
import com.stacklog.class_service.model.service.ClassService;
import com.stacklog.class_service.model.service.GroupService;
import com.stacklog.class_service.model.service.GroupsStudentService;

import lombok.Getter;
import lombok.Setter;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "/group")
public class GroupRestController {

    private static final Logger logger = LoggerFactory.getLogger(GroupRestController.class);

    @Autowired
    GroupService groupService;

    @Autowired
    GroupsStudentService groupsStudentService;

    @Autowired
    ClassService classService;

    @GetMapping("")
    public ResponseEntity<List<Groupss>> getGroupByUserId(@RequestHeader("Authorization") String token) {
        List<Groupss> groupsses = groupService.getAllByUserId(token);
        return ResponseEntity.ok().body(groupsses);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<Groupss>> getGroupByClassId(@RequestHeader("Authorization") String token,
            @PathVariable(name = "classId") String classId) {
        List<Groupss> groupsses = groupService.getAllByClassId(token, classId);
        return ResponseEntity.ok().body(groupsses);
    }

    @GetMapping("/find/{semesterId}")
    public ResponseEntity<List<Groupss>> getGroupssBySemesterId(@RequestHeader("Authorization") String token,
            @PathVariable(name = "semesterId") String semesterId) {
        List<Groupss> groupsses = groupService.getGroupssBySemesterIdAndToken(semesterId, token);
        return ResponseEntity.ok().body(groupsses);
    }

    @PostMapping("")
    public ResponseEntity<Groupss> saveGroupss(@RequestBody GroupDTO groupDTO,
            @RequestHeader("Authorization") String token) {

        logger.info("Saving group with name: {}", groupDTO.groupsName);

        Groupss newGroupss = new Groupss();
        newGroupss.setGroupsName(groupDTO.groupsName);
        newGroupss.setGroupsDescriptions(groupDTO.groupsDescriptions);
        newGroupss.setGroupsMaxMember(groupDTO.groupsMaxMember);
        newGroupss.setGroupsAvgScore(0.00);
        newGroupss.setGroupsLeaderId(groupDTO.groupsLeaderId);

        Classes clazz = classService.getById(groupDTO.classId, token);
        if (clazz == null) {
            logger.warn("Class not found with ID: {}", groupDTO.classId);
            return ResponseEntity.badRequest().build();
        }

        newGroupss.setClasses(clazz);

        try {
            newGroupss = groupService.save(newGroupss, token);
        } catch (Exception e) {
            logger.error("Failed to save group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        for (String userId : groupDTO.groupUserUserIds) {
            try {
                Groupss oldGroups = groupService.getAllByClassId(token, clazz.getClassesId()).stream()
                        .filter(g -> g.getGroupsName().toLowerCase().equals("unassigned")).findFirst()
                        .orElseThrow(() -> new RuntimeException("Unassigned group not found"));
                GroupStudent groupStudent = groupsStudentService.getByGroupIdAndStudentId(oldGroups.getGroupsId(),
                        userId);
                groupStudent.setGroups(newGroupss);
                groupsStudentService.save(groupStudent, token);
            } catch (Exception e) {
                logger.error("Failed to assign user {} to new group: {}", userId, e.getMessage(), e);
            }
        }

        logger.info("Group {} saved successfully", newGroupss.getGroupsName());
        return ResponseEntity.ok().body(newGroupss);
    }

    @PutMapping("/update")
    public ResponseEntity<Groupss> updateGroupss(@RequestBody GroupDTO groupDTO,
            @RequestHeader("Authorization") String token) {

        logger.info("Updating group with ID: {}", groupDTO.groupsId);

        Groupss groupss = groupService.getById(groupDTO.groupsId, token);
        if (groupss == null) {
            logger.warn("Group not found with ID: {}", groupDTO.groupsId);
            return ResponseEntity.badRequest().body(null);
        }

        Classes clazz = classService.getById(groupDTO.classId, token);
        if (clazz == null) {
            logger.warn("Class not found with ID: {}", groupDTO.classId);
            return ResponseEntity.badRequest().build();
        }

        groupss.setClasses(clazz);
        groupss.setGroupsName(groupDTO.groupsName);
        groupss.setGroupsDescriptions(groupDTO.groupsDescriptions);
        groupss.setGroupsMaxMember(groupDTO.groupsMaxMember);
        groupss.setGroupsLeaderId(groupDTO.groupsLeaderId);

        Groupss unassignedGroupss;
        try {
            unassignedGroupss = groupService.getAllByClassId(token, clazz.getClassesId()).stream()
                    .filter(g -> g.getGroupsName().equals("unassigned")).findFirst()
                    .orElseThrow(() -> new RuntimeException("Unassigned group not found"));
        } catch (Exception e) {
            logger.error("Error finding unassigned group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        for (GroupStudent gs : groupss.getGroupStudents()) {
            if (!groupDTO.groupUserUserIds.contains(gs.getUserId())) {
                gs.setGroups(unassignedGroupss);
            }
        }

        for (String userId : groupDTO.groupUserUserIds) {
            try {
                // Kiểm tra xem user đã nằm trong group mới chưa
                GroupStudent existingGroupStudent = groupsStudentService.getByGroupIdAndStudentId(groupss.getGroupsId(),
                        userId);
                if (existingGroupStudent != null) {
                    logger.info("User {} already in group {}", userId, groupss.getGroupsId());
                    continue;
                }
            } catch (Exception e) {
                // Nếu xảy ra lỗi (tức là không tìm thấy user trong group mới), thì di chuyển từ
                // unassigned vào
                try {
                    GroupStudent groupStudent = groupsStudentService
                            .getByGroupIdAndStudentId(unassignedGroupss.getGroupsId(), userId);
                    groupStudent.setGroups(groupss);
                    groupsStudentService.save(groupStudent, token);
                    logger.info("Moved user {} from unassigned group to group {}", userId, groupss.getGroupsId());
                } catch (Exception ex) {
                    logger.error("Failed to move user {} to group {}: {}", userId, groupss.getGroupsId(),
                            ex.getMessage(), ex);
                }
            }
        }

        logger.info("Group {} updated successfully", groupss.getGroupsId());
        return ResponseEntity.ok().body(groupss);
    }

    @DeleteMapping("/{groupsId}")
    public ResponseEntity<String> deleteGroupss(@RequestHeader("Authorization") String token,
            @PathVariable(name = "groupsId") String groupsId) {
        Groupss groupss = groupService.delete(groupsId, token);
        if (groupss == null) {
            return ResponseEntity.badRequest().body("Delete failed");
        }
        return ResponseEntity.ok().body("Delete success");
    }

}

@Getter
@Setter
class GroupDTO {
    String groupsId;
    String groupsName;
    String groupsDescriptions;
    Integer groupsMaxMember;
    String groupsLeaderId;
    String classId;
    List<String> groupUserUserIds;
}