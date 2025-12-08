package com.stacklog.class_service.model.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stacklog.class_service.model.entities.Semester;
import com.stacklog.class_service.model.repo.SemesterRepo;
import com.stacklog.core_service.model.service.IService;
import com.stacklog.core_service.utils.CommonFunction;
import com.stacklog.core_service.utils.kafka.KafkaProducer;
import com.stacklog.core_service.utils.redis.RedisService;

import jakarta.transaction.Transactional;

@Service
public class SemesterService implements IService<Semester> {

    private static final String NAME_SERVICE = "class-service";

    private static final String KAFKA_TOPIC_UPDATE = "class-service.semester.updated";
    private static final String KAFKA_TOPIC_CREATE = "class-service.semester.created";

    @Autowired
    SemesterRepo semesterRepo;

    @Autowired
    private KafkaProducer<Semester> kafkaSemesterProducer;

    RedisService<Semester> redisSemestService;

    public SemesterService(RedisService<Semester> redisSemestService) {
        this.redisSemestService = redisSemestService;
    }

    @Override
    public List<Semester> getAllByUserId(String token) {
        List<Semester> semesters = redisSemestService.getAll(token, NAME_SERVICE);
        if (semesters.isEmpty()) {
            semesters = semesterRepo.findAllByMemberUserId(redisSemestService.getCurrentUserId(token));
            redisSemestService.saveListToRedis(semesters, token, NAME_SERVICE);
        }
        return semesters;
    }

    @Transactional
    public List<Semester> getAllByYearAndQuarter(Integer year, String quarter, String token) {
        String userId = redisSemestService.getCurrentUserId(token);

        Semester.Quarter q;
        try {
            q = Semester.Quarter.valueOf(quarter.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }

        return semesterRepo.findAllByYearQuarterAndUser(year, q, userId);
    }

    @Override
    public Semester getById(String id, String token) {
        Semester semester = redisSemestService.getById(id, token, NAME_SERVICE);
        if (semester == null) {
            semester = semesterRepo.findById(id).orElseThrow();
            redisSemestService.saveToRedis(semester, token, NAME_SERVICE);
        }
        return semester;
    }

    @Override
    public Semester save(Semester e, String token) {
        boolean isCreate = (e.getSemesterId() == null || !semesterRepo.existsById(e.getSemesterId()));
        Semester newSemester = saveToDB(e, token);
        if (newSemester == null) {
            return null;
        }

        if (isCreate) {
            // kafka producer
            kafkaSemesterProducer.sendMessage(e, KAFKA_TOPIC_CREATE);
        } else {
            // kafa producer
            kafkaSemesterProducer.sendMessage(e, KAFKA_TOPIC_UPDATE);
        }

        redisSemestService.saveToRedis(newSemester, token, NAME_SERVICE);
        return newSemester;
    }

    @Transactional
    private Semester saveToDB(Semester e, String token) {
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisSemestService.getCurrentUserId(token));
        if (e.getSemesterId() == null || e.getSemesterId().isBlank()) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisSemestService.getCurrentUserId(token));
            e.setSemesterId(UUID.randomUUID().toString());
        }
        return semesterRepo.save(e);
    }

    @Override
    public Semester delete(String id, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public List<Semester> getAll() {
        return semesterRepo.findAll();
    }

}