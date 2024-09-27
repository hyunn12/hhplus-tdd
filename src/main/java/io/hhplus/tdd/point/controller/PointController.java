package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("{id}")
    public ResponseEntity<?> point(@PathVariable long id) {
        return new ResponseEntity<>(pointService.getUserPoint(id), HttpStatus.OK);
    }

    @GetMapping("{id}/histories")
    public ResponseEntity<?> history(@PathVariable long id) {
        return new ResponseEntity<>(pointService.getUserPointHistoryList(id), HttpStatus.OK);
    }

    @PatchMapping("{id}/charge")
    public ResponseEntity<?> charge(@PathVariable long id, @RequestBody long amount) {
        return new ResponseEntity<>(pointService.chargePoint(id, amount), HttpStatus.OK);
    }

    @PatchMapping("{id}/use")
    public ResponseEntity<?> use(@PathVariable long id, @RequestBody long amount) {
        return new ResponseEntity<>(pointService.usePoint(id, amount), HttpStatus.OK);
    }
}
