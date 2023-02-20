package com.doras.web.stellight.api.web;

import com.doras.web.stellight.api.service.schedule.ScheduleService;
import com.doras.web.stellight.api.web.dto.ScheduleResponseDto;
import com.doras.web.stellight.api.web.dto.ScheduleSaveRequestDto;
import com.doras.web.stellight.api.web.dto.ScheduleUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/schedules")
public class SchedulesController {

    private final ScheduleService scheduleService;

    @PostMapping
    public Long save(@RequestBody ScheduleSaveRequestDto requestDto) {
        return scheduleService.save(requestDto);
    }

    @GetMapping("/{id}")
    public ScheduleResponseDto findById(@PathVariable Long id) {
        return scheduleService.findById(id);
    }

    @PutMapping("/{id}")
    public Long update(@PathVariable Long id, @RequestBody ScheduleUpdateRequestDto requestDto) {
        return scheduleService.update(id, requestDto);
    }
}