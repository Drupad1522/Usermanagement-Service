package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDistributionResponse {
    private List<RoleCount> roleDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleCount {
        private String roleName;
        private Long count;
    }
}
