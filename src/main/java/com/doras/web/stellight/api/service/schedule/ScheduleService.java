package com.doras.web.stellight.api.service.schedule;

import com.doras.web.stellight.api.domain.schedule.QSchedule;
import com.doras.web.stellight.api.domain.schedule.Schedule;
import com.doras.web.stellight.api.domain.schedule.ScheduleRepository;
import com.doras.web.stellight.api.domain.stellar.Stellar;
import com.doras.web.stellight.api.domain.stellar.StellarRepository;
import com.doras.web.stellight.api.exception.InvalidArgumentException;
import com.doras.web.stellight.api.exception.ScheduleNotFoundException;
import com.doras.web.stellight.api.web.dto.ScheduleFindAllRequestDto;
import com.doras.web.stellight.api.web.dto.ScheduleResponseDto;
import com.doras.web.stellight.api.web.dto.ScheduleSaveRequestDto;
import com.doras.web.stellight.api.web.dto.ScheduleUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final StellarRepository stellarRepository;
    private final ScheduleRepository scheduleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long save(ScheduleSaveRequestDto requestDto) {

        // find Stellar entity
        Stellar stellar = stellarRepository.findById(requestDto.getStellarId())
                .orElseThrow(() ->
                        new InvalidArgumentException("존재하지 않는 스텔라 id 입니다. id = " + requestDto.getStellarId()));

        // make Schedule entity with Stellar
        Schedule schedule = requestDto.toScheduleEntity();
        schedule.setStellar(stellar);
        // save Schedule
        Schedule savedSchedule = scheduleRepository.save(schedule);

        return savedSchedule.getId();
    }

    @Transactional(readOnly = true)
    public ScheduleResponseDto findById(Long id) {
        Schedule entity = scheduleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));

        return new ScheduleResponseDto(entity);
    }

    @Transactional
    public Long update(Long id, ScheduleUpdateRequestDto requestDto) {
        Schedule schedule = scheduleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));

        // update schedule
        schedule.update(
                requestDto.getIsFixedTime(),
                requestDto.getStartDateTime(),
                requestDto.getTitle(),
                requestDto.getRemark());

        return id;
    }

    @Transactional
    public void delete(Long id) {
        Schedule schedule = scheduleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));

        // delete schedule (soft delete)
        schedule.delete();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> findAllSchedules(ScheduleFindAllRequestDto requestDto) {
        QSchedule schedule = QSchedule.schedule;

        var query = new JPAQuery<>(entityManager)
                .from(schedule);

        if (requestDto.getStellarId() != null) {
            query.where(schedule.stellar.id.eq(requestDto.getStellarId()));
        }
        if (requestDto.getStartDateTimeAfter() != null || requestDto.getStartDateTimeBefore() != null) {
            query.where(
                    schedule.startDateTime.between(
                            requestDto.getStartDateTimeAfter(), requestDto.getStartDateTimeBefore()));
        }

        return query.orderBy(schedule.id.asc()).fetch()
                .stream().map(obj -> (Schedule) obj).map(ScheduleResponseDto::new).collect(Collectors.toList());
    }
}
