package com.emotie.api.diary.service;

import com.emotie.api.auth.exception.UnauthorizedException;
import com.emotie.api.common.domain.Postings;
import com.emotie.api.diary.domain.Diary;
import com.emotie.api.diary.domain.DiaryIds;
import com.emotie.api.diary.domain.MemberBlindDiary;
import com.emotie.api.diary.domain.MemberReportDiary;
import com.emotie.api.diary.dto.*;
import com.emotie.api.diary.repository.DiaryRepository;
import com.emotie.api.diary.repository.MemberBlindDiaryRepository;
import com.emotie.api.diary.repository.MemberReportDiaryRepository;
import com.emotie.api.emotion.repository.EmotionRepository;
import com.emotie.api.emotion.service.EmotionService;
import com.emotie.api.member.domain.Follow;
import com.emotie.api.member.domain.Member;
import com.emotie.api.member.repository.FollowRepository;
import com.emotie.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class DiaryService {
    private static final int PAGE_SIZE = 10;

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;

    private final MemberReportDiaryRepository memberReportDiaryRepository;
    private final MemberBlindDiaryRepository memberBlindDiaryRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    private final EmotionService emotionService;

    public void create(Member member, DiaryCreateRequest request) {
        emotionService.deepenEmotionScore(member, request.getEmotion());

        diaryRepository.save(
                Diary.builder()
                        .writer(member)
                        .emotion(emotionService.getEmotionByMemberAndEmotionName(member, request.getEmotion()))
                        .content(request.getContent())
                        .isOpened(request.getIsOpened())
                        .build()
        );
    }

    public DiaryReadResponse read(Member user, Long diaryId) {
        Diary diary = getDiaryById(diaryId);
        return new DiaryReadResponse(diary.read(user));
    }

    public DiaryReadAllResponse readAll(Member user, String memberId, Integer pageNumber) {
        Member writer = getMemberById(memberId);

        Pageable page = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("createdAt").descending());
        if (user.equals(writer)) {
            List<Diary> allDiaries = diaryRepository.findAllByWriterAndNotBlinded(user, writer, Diary.reportCountThreshold, page);
            return new DiaryReadAllResponse(
                    allDiaries.stream().map(DiaryReadResponse::new).collect(Collectors.toList())
            );
        }

        List<Diary> allOpenedDiaries = diaryRepository.findAllByWriterAndIsOpenedAndNotBlinded(user, writer, true, Diary.reportCountThreshold, page);
        return new DiaryReadAllResponse(
                allOpenedDiaries.stream().map(DiaryReadResponse::new).collect(Collectors.toList())
        );
    }

    public void delete(Member user, DiaryDeleteRequest request) {
        DiaryIds id = new DiaryIds(request.getDiaryId());
        checkDeleteListValidity(user, id.getDiaryIds());
        id.getDiaryIds().forEach(
                diaryId -> {
                    Diary deleteDiary = getDiaryById(diaryId);
                    emotionService.reduceEmotionScore(user, deleteDiary.getEmotion().getName());
                    diaryRepository.delete(deleteDiary);
                }
        );
    }


    public DiaryReadAllResponse getFeed(Member user, Integer page) {
        List<Follow> followingMember = followRepository.findFollowByFromMember(user).get();
        List<Diary> feed = new LinkedList<>();
        followingMember.stream().forEach(follow -> {
            List<Diary> diaries = diaryRepository.findAllByWriterAndIsOpenedAndNotBlinded(user, follow.getToMember(), true, Diary.reportCountThreshold);
            feed.addAll(diaries);
        });
        feed.sort(Comparator.comparing(Diary::getCreatedAt).reversed());
        List<DiaryReadResponse> collect = feed.stream().map(DiaryReadResponse::new).skip(page * PAGE_SIZE).limit(PAGE_SIZE).collect(Collectors.toList());
        return new DiaryReadAllResponse(collect);
    }

    public void report(Member user, DiaryReportRequest request, Long diaryId) {
        checkReportOrBlindRequestValidity(user, diaryId);
        Diary target = getDiaryById(diaryId);
        target.addReportCount();
        diaryRepository.saveAndFlush(target);
        memberReportDiaryRepository.save(new MemberReportDiary(user, target, request.getReason()));
    }

    public void blind(Member user, Long diaryId) {
        checkReportOrBlindRequestValidity(user, diaryId);
        Diary target = getDiaryById(diaryId);
        memberBlindDiaryRepository.save(new MemberBlindDiary(user, target));
    }

    private Diary getDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId).orElseThrow(
                () -> new NoSuchElementException("???????????? ???????????? ??????????????? ????????????.")
        );
    }

    private Member getMemberById(String memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new NoSuchElementException("???????????? ???????????? ????????? ????????????.")
        );
    }

    private void checkDeleteListValidity(Member user, Set<Long> id) {
        id.forEach(
                (diaryId) -> {
                    Diary diary = getDiaryById(diaryId);
                    if (!user.equals(diary.getWriter())) throw new UnauthorizedException("????????? ????????? ???????????? ?????? ????????? ????????????.");
                    if (diary.getReportCount() >= Postings.reportCountThreshold)
                        throw new UnauthorizedException("????????? ????????? ????????? ????????? ???????????? ????????? ??????????????????.");
                }
        );
    }

    private void checkReportOrBlindRequestValidity(Member user, Long diaryId) {
        Diary diary = getDiaryById(diaryId);
        diary.checkNotWriter(user);
        diary.checkIsOpened();
    }
}
