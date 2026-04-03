package in.sfp.main.repo;

import in.sfp.main.model.Attendance;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.model.Labourer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByLabourerAndAttendanceDateBetween(Labourer labourer, LocalDate start, LocalDate end);
    List<Attendance> findByAttendanceDateBetween(LocalDate start, LocalDate end);
    Optional<Attendance> findByLabourerAndAttendanceDateAndSite(Labourer labourer, LocalDate date, ConstructionSite site);
    List<Attendance> findByLabourerAndAttendanceDate(Labourer labourer, LocalDate date);
    List<Attendance> findByAttendanceDate(LocalDate date);
}
