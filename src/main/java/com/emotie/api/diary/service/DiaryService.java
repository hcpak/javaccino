package com.emotie.api.diary.service;

import com.emotie.api.auth.exception.UnauthorizedException;
import com.emotie.api.common.domain.Postings;
import com.emotie.api.diary.domain.Diary;
import com.emotie.api.diary.exception.PeekingPrivatePostException;
import com.emotie.api.emotion.domain.Emotion;
import com.emotie.api.diary.dto.DiaryCreateRequest;
import com.emotie.api.diary.dto.DiaryDeleteRequest;
import com.emotie.api.diary.dto.DiaryUpdateRequest;
import com.emotie.api.diary.repository.DiaryRepository;
import com.emotie.api.emotion.repository.EmotionRepository;
import com.emotie.api.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;

    public void create(Member user, DiaryCreateRequest request) {
        Emotion emotion = getEmotionByEmotion(request.getEmotion());
        diaryRepository.save(
                Diary.builder()
                        .writer(user)
                        .emotion(emotion)
                        .content(request.getContent())
                        .isOpened(request.getIsOpened())
                        .build()
        );
    }

    public Diary read(Member user, Integer diaryId) {
        Diary diary = getDiaryById(diaryId);
        return diary.read(user);
    }

    @Deprecated
    public String update(Member user, Integer diaryId, DiaryUpdateRequest request) {
        Diary diary = getDiaryById(diaryId);
        Emotion originalEmotion = diary.getEmotion();

        diary.checkUserValidity(user);

        Emotion updatingEmotion = getEmotionByEmotion(request.getEmotion());

        updateDiaryWithRequest(diary, request);
        diaryRepository.saveAndFlush(diary);
        emotionRepository.saveAndFlush(originalEmotion);
        emotionRepository.saveAndFlush(updatingEmotion);

        return originalEmotion.getEmotion();
    }

    public List<Diary> delete(Member user, DiaryDeleteRequest request) {
        Set<Integer> id = new HashSet<>(request.getId());
        checkDeleteListValidity(user, id);
        LinkedList<Diary> diaries = new LinkedList<>();
        id.stream().map(this::getDiaryById).forEach(
                (diary) -> {
                    diaries.add(diary);
                    diaryRepository.delete(diary);
                }
        );

        return diaries;
    }

    private Diary getDiaryById(Integer diaryId) {
        return diaryRepository.findById(diaryId).orElseThrow(
                () -> new NoSuchElementException("해당하는 아이디의 다이어리가 없습니다.")
        );
    }

    private Emotion getEmotionByEmotion(String emotion) {
        return emotionRepository.findByEmotion(emotion).orElseThrow(
                () -> new NoSuchElementException("해당하는 이름의 감정이 없습니다.")
        );
    }

    private void updateDiaryWithRequest(Diary diary, DiaryUpdateRequest updateRequest) {
        diary.rewriteContent(updateRequest.getContent());
        diary.updateEmotion(getEmotionByEmotion(updateRequest.getEmotion()));
        diary.updateOpenness(updateRequest.getIsOpened());
    }

    private void checkDeleteListValidity(Member user, Set<Integer> id) {
        id.forEach(
                (diaryId) -> {
                    Diary diary = getDiaryById(diaryId);
                    if (!user.equals(diary.getWriter())) throw new UnauthorizedException("삭제를 요청한 사람에게 해당 권한이 없습니다.");
                    if (diary.getReportCount() >= Postings.reportCountThreshold)
                        throw new UnauthorizedException("삭제를 요청한 대상이 신고가 누적되어 삭제가 불가능합니다.");
                }
        );
    }
}