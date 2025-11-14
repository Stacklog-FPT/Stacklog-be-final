package com.stacklog.task_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.task_service.model.entities.Review;
import com.stacklog.task_service.model.service.ReviewService;
import com.stacklog.task_service.model.service.TaskService;
import com.stacklog.task_service.payload.ResponseTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping(value = {"/review", "/review/"})
public class ReviewRestController {
    
    @Autowired
    ReviewService reviewService;

    @Autowired
    TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<List<Review>> getReviewByTaskId(@RequestHeader("Authorization") String token, @PathVariable("taskId") String taskId) {
        List<Review> lists = reviewService.getAllByTaskId(token, taskId);
        if (lists.isEmpty() || lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(lists);
    }
    
    @PostMapping("")
    public ResponseEntity<ResponseTask> saveReview(@RequestHeader("Authorization") String token, @RequestBody ReviewDTO e) {
        Review review = null;
        if (e.reviewId == null || e.reviewId.isEmpty()) {
            review = new Review();
        } else {
            review = reviewService.getById(e.reviewId, token);
        }
        review.setReviewContent(e.getReviewContent());
        review.setTask(taskService.getById(e.getTaskId(), token));
        review = reviewService.save(review, token);
        if (review == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(new ResponseTask(taskService.getById(e.getTaskId(), token)));
    }
    
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<String> deleteReview(@RequestHeader("Authorization") String token, @PathVariable("reviewId") String reviewId) {
        Review review = reviewService.delete(reviewId, token);
        if (review == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body("Delete success");
    }

}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReviewDTO{
    String reviewContent;
    String taskId;
    String reviewId;
}
