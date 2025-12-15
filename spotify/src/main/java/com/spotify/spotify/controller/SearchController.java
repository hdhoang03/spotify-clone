package com.spotify.spotify.controller;

import com.spotify.spotify.dto.ApiResponse;
import com.spotify.spotify.dto.response.SearchResponse;
import com.spotify.spotify.service.SearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    SearchService searchService;

    @GetMapping()
    ApiResponse<SearchResponse> search(@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword){
        String cleanKeyword = keyword.trim(); //Cắt chuỗi trắng
        if (keyword.isEmpty()) { //nếu không có keyword không gọi service
            return ApiResponse.<SearchResponse>builder()
                    .code(1000)
                    .result(SearchResponse.builder()
                            .artists(Collections.emptyList())
                            .songs(Collections.emptyList())
                            .albums(Collections.emptyList())
                            .categories(Collections.emptyList())
                            .build())
                    .build();
        }
        return ApiResponse.<SearchResponse>builder()
                .code(1000)
                .result(searchService.searchEverything(cleanKeyword))
                .build();
    }
}
