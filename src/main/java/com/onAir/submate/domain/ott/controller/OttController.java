package com.onAir.submate.domain.ott.controller;

import com.onAir.submate.domain.ott.dto.ContentWithProvidersDto;
import com.onAir.submate.domain.ott.service.OttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ott")
@RequiredArgsConstructor
public class OttController {

    private final OttService ottService;

    /**
     * GET /api/ott/search?query=오징어게임
     * 콘텐츠 검색 + 한국에서 시청 가능한 OTT 서비스 목록 반환
     */
    @GetMapping("/search")
    public ResponseEntity<List<ContentWithProvidersDto>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(ottService.search(query));
    }
}
