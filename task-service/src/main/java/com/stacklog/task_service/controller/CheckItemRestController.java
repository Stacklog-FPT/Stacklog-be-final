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

import com.stacklog.task_service.model.entities.CheckItem;
import com.stacklog.task_service.model.service.CheckItemService;

@RestController
@RequestMapping(path = "/check-item")
public class CheckItemRestController {

    @Autowired
    CheckItemService checkItemService;

    @MessageMapping("/checkitem")
    @SendTo("/topic/task-service")
    public ResponseEntity<Map<String, String>> sendMessage(Map<String, String> message) {
        // System.out.println("oke");
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<CheckItem>> getCheckItemByGroupId(@RequestHeader("Authorization") String token) {
        List<CheckItem> checkItems = checkItemService.getAllByUserId(token);
        if (checkItems.isEmpty() || checkItems == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(checkItems);
    }

    @PostMapping("")
    public ResponseEntity<CheckItem> postCheckItem(@RequestHeader("Authorization") String token,
            @RequestBody CheckItem checkItem) {
        CheckItem newCheckItem = checkItemService.save(checkItem, token);
        if (newCheckItem == null || newCheckItem.getCheckItemId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(newCheckItem);
    }

    @DeleteMapping("/{checkItemId}")
    public ResponseEntity<String> deleteCheckItem(@RequestHeader("Authorization") String token,
            @PathVariable(name = "checkItemId") String checkItemId) {
        CheckItem checkItem = checkItemService.delete(checkItemId, token);
        if (checkItem == null || checkItem.getCheckItemId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

}
