package org.example.review;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewSubmittedEvent {
    private Long campaignId;
    private Long userId;
    private String reviewUrl;

}
