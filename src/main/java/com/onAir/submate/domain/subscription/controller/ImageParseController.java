package com.onAir.submate.domain.subscription.controller;

import com.onAir.submate.domain.subscription.dto.ParsedSubscriptionDto;
import com.onAir.submate.domain.subscription.service.ImageParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class ImageParseController {

    private final ImageParseService imageParseService;

    @PostMapping(value = "/parse-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsedSubscriptionDto> parseImage(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(imageParseService.parseImage(file));
    }
}
