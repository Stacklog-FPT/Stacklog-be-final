package com.stacklog.task_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.task_service.model.entities.TaskAssign;
import com.stacklog.task_service.model.service.TaskAssignService;

@RestController
@RequestMapping("/task-assign")
public class TaskAssignRestController {
    
    @Autowired
    TaskAssignService taskAssignService;

    @MessageMapping("/taskassign")
    @SendTo("/topic/task-service")
    public ResponseEntity<Map<String, String>> sendMessage(Map<String, String> message){
        // System.out.println("oke");
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<TaskAssign>> getTasksByTaskId(@RequestHeader("Authorization") String token, @PathVariable (name = "taskId") String taskId) {
        List<TaskAssign> lists = taskAssignService.getAllByTaskId(token, taskId);
        if (lists.isEmpty() || lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(lists);
    }
    
    
    @PostMapping("")
    public ResponseEntity<TaskAssign> saveTaskAssign(@RequestHeader("Authorization") String token, @RequestBody TaskAssign e) {
        TaskAssign taskAssign = taskAssignService.save(e, token);
        if (taskAssign == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(taskAssign);
    }
    
    @DeleteMapping("/{taskAssignId}")
    public ResponseEntity<String> deleteTask(@RequestHeader("Authorization") String token, @PathVariable("taskAssignId") String taskAssignId) {
        TaskAssign taskAssign = taskAssignService.delete(taskAssignId, token);
        if (taskAssign == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body("Delete success");
    }

}
