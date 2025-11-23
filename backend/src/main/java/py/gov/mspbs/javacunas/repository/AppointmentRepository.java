package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Appointment entity.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find appointments by child id.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.child.id = :childId " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByChildId(@Param("childId") Long childId);

    /**
     * Find appointments by status.
     */
    List<Appointment> findByStatusOrderByAppointmentDate(Appointment.AppointmentStatus status);

    /**
     * Find appointments by date range.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.appointmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findByAppointmentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Find appointments assigned to a user.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.assignedTo.id = :userId " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findByAssignedToId(@Param("userId") Long userId);

    /**
     * Find upcoming appointments (scheduled or confirmed).
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.appointmentDate >= :now " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now);

    /**
     * Find appointments created by a user.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.createdBy.id = :userId " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByCreatedById(@Param("userId") Long userId);

    /**
     * Find appointments for children associated with a user (through guardians).
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.child c " +
           "JOIN c.guardians g " +
           "WHERE g.user.id = :userId " +
           "AND c.deletedAt IS NULL " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByUserIdThroughChildren(@Param("userId") Long userId);

}
