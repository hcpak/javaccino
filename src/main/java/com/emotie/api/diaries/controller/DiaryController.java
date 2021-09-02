package com.emotie.api.diaries.controller;

import com.emotie.api.diaries.dto.*;
import com.emotie.api.diaries.service.DiaryService;
import com.emotie.api.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<Void> write(
            @AuthenticationPrincipal Member user, @RequestBody @Valid DiaryCreateRequest diaryCreateRequest
    ) throws Exception{
        diaryService.create(diaryCreateRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{diaryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiaryReadResponse> read(@PathVariable Integer diaryId) throws Exception {
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{diaryId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Member user, @PathVariable Integer diaryId
    ) throws Exception {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Member user, @RequestBody @Valid DiaryDeleteRequest diaryDeleteRequest
    ) throws Exception{
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/export")
    public ResponseEntity<Void> export(
            @AuthenticationPrincipal Member user, @RequestBody @Valid DiaryExportRequest diaryExportRequest
    ) throws Exception {
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/export_all")
    public ResponseEntity<Void> export_all(
            @RequestBody @Valid DiaryExportAllRequest diaryExportAllRequest
    ) throws Exception {
        return ResponseEntity.ok().build();
    }

}
