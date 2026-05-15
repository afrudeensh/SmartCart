package com.smartcart.auth.repository;

import com.smartcart.auth.entity.EventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventOutboxRepository extends JpaRepository<EventOutbox, Long> {

    // Scheduler polls this every 5 seconds
    List<EventOutbox> findByPublishedFalseOrderByCreatedAtAsc();
}