package org.example.campaign;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository
        extends JpaRepository<Campaign, Long> {

    @Query("""
                select c from Campaign c
                join fetch c.platform
                where c.user.id = :userId
            """)
    List<Campaign> findByUserId(Long userId);

    Optional<Campaign> findByIdAndUser_Id(Long campaignId, Long userId);

    boolean existsByIdAndUser_Id(Long campaignId, Long userId);

    List<Campaign> findByUserIdAndStatus(Long userId, Status status, Sort sort);

    @Query("select c from Campaign c where c.user.id = :userId and c.visitDate >= :start and c.visitDate <= :end")
    List<Campaign> findByUserIdAndVisitDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("select c from Campaign c where c.user.id = :userId and c.visitDate >= :start and c.visitDate <= :end and c.category.id = :categoryId")
    List<Campaign> findByUserIdAndVisitDateBetweenAndCategory(Long userId, LocalDate start, LocalDate end,
            Long categoryId);

    @Query("select c from Campaign c where c.user.id = :userId and c.deadline >= :start and c.deadline <= :end")
    List<Campaign> findByUserIdAndDeadlineBetween(Long userId, LocalDate start, LocalDate end);

    @Query("select c from Campaign c where c.user.id = :userId and c.deadline >= :start and c.deadline <= :end and c.category.id = :categoryId")
    List<Campaign> findByUserIdAndDeadlineBetweenAndCategory(Long userId, LocalDate start, LocalDate end,
            Long categoryId);
}
