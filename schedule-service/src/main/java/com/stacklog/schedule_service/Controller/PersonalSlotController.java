package com.stacklog.schedule_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.schedule_service.model.entities.Slot;
import com.stacklog.schedule_service.model.service.SlotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/personal")
public class PersonalSlotController {

    @Autowired
    SlotService slotService;

    @GetMapping("")
    public ResponseEntity<List<Slot>> getPersonalSlots(
            @RequestParam(name = "semesterId", required = false) String semesterId,
            @RequestHeader("Authorization") String token) {
        List<Slot> personalSlotList = slotService.getAllBySemesterId(semesterId, token);
        return ResponseEntity.ok().body(personalSlotList);
    }

}
