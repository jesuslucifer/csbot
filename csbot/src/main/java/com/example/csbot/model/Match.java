package com.example.csbot.model;

import com.example.csbot.model.enums.MatchStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Match {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    private String hltvMatchId;
    private String team1;
    private String team2;
    private String event;
    private String status;
    private LocalDateTime startTime;
    private String mapPicks;
    private String meta;
    private Boolean isLive;
}
