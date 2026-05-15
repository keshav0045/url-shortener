package com.url_shortner.repository;

import com.url_shortner.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

    List<Click> findByShortCode(String shortCode);
    long countByShortCode(String shortCode);
    List<Click> findByShortCodeAndClickedAtBetween(String shortCode, LocalDateTime from, LocalDateTime to);

    List<Click> shortCode(String shortCode);
}
