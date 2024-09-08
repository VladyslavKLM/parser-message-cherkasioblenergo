package innovateiu.task.parsermessagecherkasioblenergo.service;

import innovateiu.task.parsermessagecherkasioblenergo.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ParserMessageService {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ParserMessageService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public void processMessage(String message) {
        cleanUpSchedule();
        LocalDate date = extractDate(message);
        if (date != null) {
            Map<Integer, List<String>> schedule = extractShutdownHours(message);
            scheduleRepository.getScheduleMap().put(date, schedule);
        }
    }

    private void cleanUpSchedule() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        scheduleRepository.getScheduleMap().keySet().removeIf(date -> !date.equals(today) && !date.equals(tomorrow));
    }

    private Map<Integer, List<String>> extractShutdownHours(String message) {
        Map<Integer, List<String>> schedule = new HashMap<>();
        String[] lines = message.split("\n");

        for (String line : lines) {
            if (line.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}.*")) {
                String[] parts = line.split(" ");
                String timeRange = parts[0];

                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    String queueStr = part.replaceAll("[^0-9]", "");
                    if (!queueStr.isEmpty()) {
                        try {
                            int queueNumber = Integer.parseInt(queueStr);
                            schedule.computeIfAbsent(queueNumber, k -> new ArrayList<>()).add(timeRange);
                        } catch (NumberFormatException e) {
                            System.out.println("parseMessage");
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return schedule;
    }

    private LocalDate extractDate(String message) {
        String[] months = {"січня", "лютого", "березня", "квітня", "травня", "червня",
                "липня", "серпня", "вересня", "жовтня", "листопада", "грудня"};
        for (String month : months) {
            if (message.contains(month)) {
                String[] parts = message.split(month);
                String dayStr = parts[0].replaceAll(".*?(\\d{1,2}).*", "$1");
                int day = Integer.parseInt(dayStr);
                int monthIndex = Arrays.asList(months).indexOf(month) + 1;
                try {
                    return LocalDate.of(LocalDate.now().getYear(), monthIndex, day);
                } catch (DateTimeParseException e) {
                    System.out.println("extractDate");
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
