package com.stacklog.task_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.task_service.model.entities.StatusTask;
import com.stacklog.task_service.model.service.StatusTaskService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "/status-task")
public class StatusTaskRestController {

    @Autowired
    StatusTaskService statusTaskService;

    @MessageMapping("/statustask")
    @SendTo("/topic/task-service")
    public ResponseEntity<Map<String, String>> sendMessage(Map<String, String> message) {
        // System.out.println("oke");
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<StatusTask>> getStatusTaskByGroupId(@RequestHeader("Authorization") String token,
            @PathVariable(name = "groupId") String groupId) {
        List<StatusTask> statusTasks = statusTaskService.getAllByGroupId(token, groupId);
        if (statusTasks.isEmpty() || statusTasks == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(statusTasks);
    }

    @PostMapping("")
    public ResponseEntity<StatusTask> postStatusTask(@RequestHeader("Authorization") String token,
            @RequestBody StatusTask statusTask) {
        StatusTask newStatusTask = statusTaskService.save(statusTask, token);
        if (newStatusTask == null || newStatusTask.getStatusTaskId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(newStatusTask);
    }

    @DeleteMapping("/{statusTaskId}")
    public ResponseEntity<String> deleteStatusTask(@RequestHeader("Authorization") String token,
            @PathVariable(name = "statusTaskId") String statusTaskId) {
        StatusTask statusTask = statusTaskService.delete(statusTaskId, token);
        if (statusTask == null || statusTask.getStatusTaskId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

}
