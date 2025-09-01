package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long suspendedUsers;
}
