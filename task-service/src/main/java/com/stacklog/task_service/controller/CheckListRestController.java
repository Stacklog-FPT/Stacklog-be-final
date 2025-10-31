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

import com.stacklog.task_service.model.entities.CheckList;
import com.stacklog.task_service.model.service.CheckListService;


@RestController
@RequestMapping(path = "/check-list")
public class CheckListRestController {

    @Autowired
    CheckListService checkListService;

    @MessageMapping("/checklist")
    @SendTo("/topic/task-service")
    public ResponseEntity<Map<String, String>> sendMessage(Map<String, String> message) {
        // System.out.println("oke");
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<CheckList>> getCheckListByTaskId(@RequestHeader("Authorization") String token, @PathVariable(name = "taskId") String taskId) {
        List<CheckList> checkLists = checkListService.getAllByTaskId(taskId, token);
        if (checkLists.isEmpty() || checkLists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(checkLists);
    }
    

    @PostMapping("")
    public ResponseEntity<CheckList> postCheckList(@RequestHeader("Authorization") String token,
            @RequestBody CheckList checkList) {
        CheckList newCheckList = checkListService.save(checkList, token);
        if (checkList == null || checkList.getCheckListId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(newCheckList);
    }

    @DeleteMapping("/{checkListId}")
    public ResponseEntity<String> deleteStatusTask(@RequestHeader("Authorization") String token,
            @PathVariable(name = "checkListId") String checkListId) {
        CheckList checkList = checkListService.delete(checkListId, token);
        if (checkList == null || checkList.getCheckListId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

}
