package edu.dosw.infrastructure.persistence;


import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.domain.model.Notification;
import edu.dosw.infrastructure.persistence.documents.NotificationDocument;
import edu.dosw.infrastructure.persistence.mappers.NotificationMongoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MongoNotificationRepository implements NotificationRepositoryPort {

    private final MongoTemplate mongoTemplate;
    private final NotificationMongoMapper notificationMongoMapper;

    @Override
    public Notification save(Notification notification) {
        NotificationDocument document = notificationMongoMapper.toDocument(notification);
        NotificationDocument saved = mongoTemplate.save(document);
        return notificationMongoMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(String id) {
        NotificationDocument document = mongoTemplate.findById(id, NotificationDocument.class);
        return Optional.ofNullable(notificationMongoMapper.toDomain(document));
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.with(org.springframework.data.domain.Sort.by("createdAt").descending());

        List<NotificationDocument> documents = mongoTemplate.find(query, NotificationDocument.class);
        return documents.stream()
                .map(notificationMongoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingNotifications() {
        Query query = new Query(Criteria.where("status").is("PENDING"));
        List<NotificationDocument> documents = mongoTemplate.find(query, NotificationDocument.class);
        return documents.stream()
                .map(notificationMongoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByUserIdAndStatus(String userId, String status) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("status").is(status)
        );
        query.with(org.springframework.data.domain.Sort.by("createdAt").descending());

        List<NotificationDocument> documents = mongoTemplate.find(query, NotificationDocument.class);
        return documents.stream()
                .map(notificationMongoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndType(String userId, String type) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("type").is(type)
        );
        return mongoTemplate.exists(query, NotificationDocument.class);
    }

    @Override
    public long countByUserIdAndStatus(String userId, String status) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("status").is(status)
        );
        return mongoTemplate.count(query, NotificationDocument.class);
    }
}