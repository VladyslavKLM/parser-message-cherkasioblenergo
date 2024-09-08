package innovateiu.task.parsermessagecherkasioblenergo.repository;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class ScheduleRepository {
    private final Map<LocalDate, Map<Integer, List<String>>> scheduleMap = new HashMap<>();
}
