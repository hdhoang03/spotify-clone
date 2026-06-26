package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.DashboardDataResponse;
import com.spotify.spotify.dto.response.DashboardOverviewResponse;
import com.spotify.spotify.service.AnalyticsService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsController {
    AnalyticsService analyticsService;

    @GetMapping("/overview")
    ApiResponse<DashboardOverviewResponse> getDashboardOverview() {
        return ApiResponse.<DashboardOverviewResponse>builder()
                .code(1000)
                .message("Dashboard overview fetched successfully")
                .result(analyticsService.getDashboardOverview())
                .build();
    }

    @GetMapping("/dashboard")
    ApiResponse<DashboardDataResponse> getDashboardData(@RequestParam(defaultValue = "week") String timeRange,
                                                        @RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month){
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        return ApiResponse.<DashboardDataResponse>builder()
                .code(1000)
                .message("Dashboard data fetched successfully")
                .result(analyticsService.getDashBoardData(timeRange, year, month))
                .build();
    }
}

