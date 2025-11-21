package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.Notification;

import java.util.List;

/**
 * Repository interface for Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by recipient.
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.recipientId = :recipientId " +
           "AND n.recipientType = :recipientType " +
           "ORDER BY n.sentAt DESC")
    List<Notification> findByRecipient(@Param("recipientId") Long recipientId,
                                       @Param("recipientType") Notification.RecipientType recipientType);

    /**
     * Find unread notifications for a recipient.
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.recipientId = :recipientId " +
           "AND n.recipientType = :recipientType " +
           "AND n.isRead = 'N' " +
           "ORDER BY n.sentAt DESC")
    List<Notification> findUnreadByRecipient(@Param("recipientId") Long recipientId,
                                             @Param("recipientType") Notification.RecipientType recipientType);

    /**
     * Count unread notifications for a recipient.
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.recipientId = :recipientId " +
           "AND n.recipientType = :recipientType " +
           "AND n.isRead = 'N'")
    Long countUnreadByRecipient(@Param("recipientId") Long recipientId,
                                @Param("recipientType") Notification.RecipientType recipientType);

    /**
     * Find notifications by type and recipient.
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.recipientId = :recipientId " +
           "AND n.recipientType = :recipientType " +
           "AND n.type = :type " +
           "ORDER BY n.sentAt DESC")
    List<Notification> findByRecipientAndType(@Param("recipientId") Long recipientId,
                                              @Param("recipientType") Notification.RecipientType recipientType,
                                              @Param("type") Notification.NotificationType type);

}
