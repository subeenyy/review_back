/*
 * package org.example.review;
 * 
 * import lombok.extern.slf4j.Slf4j;
 * import org.springframework.kafka.annotation.KafkaListener;
 * import org.springframework.stereotype.Component;
 * 
 * @Slf4j
 * 
 * @Component
 * 
 * @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name
 * = "spring.kafka.enabled", havingValue = "true")
 * public class ReviewSubmittedConsumer {
 * 
 * @KafkaListener(topics = "review-submitted")
 * public void handle(ReviewSubmittedEvent event) {
 * log.info(
 * "** Kafka 수신됨 campaignId={}, userId={}, reviewUrl={}",
 * event.getCampaignId(),
 * event.getUserId(),
 * event.getReviewUrl());
 * 
 * // 여기서 리워드 처리
 * }
 * }
 */
