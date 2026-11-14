package model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Link {
    private String originalUrl;
    private String code;
    private Instant expiresAt;
    @Setter
    private int limit;
    @Setter
    private int currentLimit;
    private UUID ownerUuid;
}
