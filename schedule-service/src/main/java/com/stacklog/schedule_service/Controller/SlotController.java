package com.stacklog.schedule_service.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.schedule_service.model.entities.Slot;
import com.stacklog.schedule_service.model.entities.SlotAssign;
import com.stacklog.schedule_service.model.service.SlotAssignService;
import com.stacklog.schedule_service.model.service.SlotService;

import lombok.Getter;
import lombok.Setter;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping(value = "")
public class SlotController {
    
    @Autowired
    SlotService slotService;

    @Autowired
    SlotAssignService slotAssignService;

    @GetMapping("/personal-schedule")
    public ResponseEntity<List<Slot>> getTasksByUserId(@RequestHeader("Authorization") String token) {
        List<Slot> lists = slotService.getAllByUserId(token);
        if (lists.isEmpty() || lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(lists);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<Slot>> getTasksByGroupId(@RequestHeader("Authorization") String token, @PathVariable("groupId") String groupId) {
        List<Slot> lists = slotService.getAllByGroupId(token, groupId);
        if (lists.isEmpty() || lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(lists);
    }
    
    @PostMapping("/save")
    public ResponseEntity<Slot> saveTask(@RequestHeader("Authorization") String token, @RequestBody SlotDTO e) {
        Slot slot = new Slot();
        slot.setSlotId(e.getSlotId());
        slot.setSlotTitle(e.slotTitle);
        slot.setSlotDescription(e.slotDescription);
        slot.setSlotStartTime(e.slotStartTime);
        slot.setGroupId(e.groupId);
        List<SlotAssign> slotAssigns = new ArrayList<>();
        for (String userId : e.userIdAssigns) {
            SlotAssign slotAssign = new SlotAssign();
            slotAssign.setSlot(slot);
            slotAssign.setUserId(userId);
            slotAssigns.add(slotAssign);   
        }
        slot.setSlotAssigns(slotAssigns);
        slot = slotService.save(slot, token);
        if (slot == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(slot);
    }
    
    @DeleteMapping("/delete/{slotId}")
    public ResponseEntity<String> deleteTask(@RequestHeader("Authorization") String token, @PathVariable("slotId") String slotId) {
        Slot slot = slotService.delete(slotId, token);
        if (slot == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body("Delete success");
    }

}

@Getter
@Setter
class SlotDTO {
    String slotId;
    String slotTitle;
    String slotDescription;
    LocalDateTime slotStartTime;
    String groupId;
    List<String> userIdAssigns;
}
