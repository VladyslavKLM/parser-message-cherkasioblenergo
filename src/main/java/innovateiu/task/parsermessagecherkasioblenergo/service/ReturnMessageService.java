package innovateiu.task.parsermessagecherkasioblenergo.service;

import innovateiu.task.parsermessagecherkasioblenergo.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReturnMessageService {
    private final ScheduleRepository scheduleRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");

    @Autowired
    public ReturnMessageService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<String> getScheduleByNumber(Integer scheduleNumber) {
        List<String> scheduleForLine = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<String> todaySchedule = getScheduleForDate(today)
                .getOrDefault(scheduleNumber, Collections.emptyList());

        if (!todaySchedule.isEmpty()) {
            scheduleForLine.add("Графік на " + today.format(formatter) + ":");
            scheduleForLine.addAll(todaySchedule);
        }

        List<String> tomorrowSchedule = getScheduleForDate(tomorrow)
                .getOrDefault(scheduleNumber, Collections.emptyList());

        if (!tomorrowSchedule.isEmpty()) {
            scheduleForLine.add("Графік на " + tomorrow.format(formatter) + ":");
            scheduleForLine.addAll(tomorrowSchedule);
        }

        return scheduleForLine;
    }

    public Set<Integer> getAllSchedules(LocalDate date) {
        return scheduleRepository.getScheduleMap().getOrDefault(date, Collections.emptyMap()).keySet();
    }

    private Map<Integer, List<String>> getScheduleForDate(LocalDate date) {
        return scheduleRepository.getScheduleMap().getOrDefault(date, Collections.emptyMap());
    }

}
