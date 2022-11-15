package com.seoul.openproject.partner.service;

import com.seoul.openproject.partner.domain.model.match.ContentCategory;
import com.seoul.openproject.partner.domain.model.matchcondition.MatchCondition.MatchConditionRandomMatchDto;
import com.seoul.openproject.partner.domain.model.matchcondition.Place;
import com.seoul.openproject.partner.domain.model.matchcondition.TypeOfStudy;
import com.seoul.openproject.partner.domain.model.matchcondition.WayOfEating;
import com.seoul.openproject.partner.domain.model.member.Member;
import com.seoul.openproject.partner.domain.model.random.MealRandomMatch;
import com.seoul.openproject.partner.domain.model.random.RandomMatch;
import com.seoul.openproject.partner.domain.model.random.RandomMatch.RandomMatchDto;
import com.seoul.openproject.partner.domain.model.random.StudyRandomMatch;
import com.seoul.openproject.partner.error.exception.ErrorCode;
import com.seoul.openproject.partner.error.exception.NoEntityException;
import com.seoul.openproject.partner.error.exception.RandomMatchAlreadyExistException;
import com.seoul.openproject.partner.repository.random.RandomMatchRedisRepository;
import com.seoul.openproject.partner.repository.random.RandomMatchRepository;
import com.seoul.openproject.partner.repository.user.UserRepository;
import com.seoul.openproject.partner.utils.RedisKeyFactory;
import com.seoul.openproject.partner.utils.RedisKeyFactory.Key;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RandomMatchService {

    private final UserRepository userRepository;
    private final RandomMatchRedisRepository randomMatchRedisRepository;

    private final RandomMatchRepository randomMatchRepository;

    @Transactional
    public ResponseEntity<Void> createRandomMatch(String userId,
        RandomMatchDto randomMatchDto) {
        Member member = userRepository.findByApiId(userId).orElseThrow(() -> new NoEntityException(
            ErrorCode.ENTITY_NOT_FOUND)).getMember();
        LocalDateTime now = LocalDateTime.now();
        //이미 30분 이내에 랜덤 매칭 신청을 한 경우 인지 체크
        verifyAlreadyApplied(randomMatchDto.getContentCategory(), member, now);

        //요청 dto로 부터 랜덤 매칭 모든 경우의 수 만들어서 RandomMatch 여러개로 변환
        List<RandomMatch> randomMatches = makeAllAvailRandomMatchesFromRandomMatchDto(
            randomMatchDto, member, now);

        //랜덤 매칭 신청한 것 DB에 기록.
        randomMatchRepository.saveAll(randomMatches);

        // 여러 매칭 조건들 redis 에 저장.

        randomMatches.forEach(randomMatch ->
            randomMatchRedisRepository.addToSortedSet(RedisKeyFactory.RANDOM_MATCHES,
                randomMatch.toStringKey(), 0.0)
        );

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void verifyAlreadyApplied(ContentCategory contentCategory, Member member,
        LocalDateTime now) {
        if ((contentCategory.equals(ContentCategory.MEAL) &&
            randomMatchRepository.findMealByCreatedAtBefore(now.minusMinutes(30),
                member.getId()).size() > 0) ||
            (contentCategory.equals(ContentCategory.STUDY) &&
                randomMatchRepository.findStudyByCreatedAtBefore(now.minusMinutes(30),
                    member.getId()).size() > 0)) {

            throw new RandomMatchAlreadyExistException(ErrorCode.RANDOM_MATCH_ALREADY_EXIST);
        }
    }

    public ResponseEntity<Void> deleteRandomMatch(String userId) {
        Long memberId = userRepository.findByApiId(userId).orElseThrow(() -> new NoEntityException(
            ErrorCode.ENTITY_NOT_FOUND)).getMember().getId();
        List<RandomMatch> randomMatches = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        //생성된 지 30분 이내에
        randomMatches.addAll(
            randomMatchRepository.findMealByCreatedAtBefore(now.minusMinutes(30), memberId));
        randomMatches.addAll(
            randomMatchRepository.findStudyByCreatedAtBefore(now.minusMinutes(30), memberId));

        for (RandomMatch randomMatch : randomMatches) {
            log.info("{}", randomMatch.toStringKey());
        }
        log.info("{}", RedisKeyFactory.toKey(Key.MEMBER, memberId.toString()));

        //redis 모두 삭제시도
        randomMatchRedisRepository.deleteAllSortedSet(RedisKeyFactory.RANDOM_MATCHES,
            randomMatches.stream()
                .map(randomMatch -> randomMatch.toStringKey())
                .toArray(String[]::new));
        //db상에서도 모두 삭제
        randomMatchRepository.deleteAll(randomMatches);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * 요청 dto로 부터 랜덤 매칭 모든 경우의 수 만들어서 RandomMatch 여러개로 변환
     *
     * @param randomMatchDto
     * @return
     */
    private List<RandomMatch> makeAllAvailRandomMatchesFromRandomMatchDto(
        RandomMatchDto randomMatchDto, Member member, LocalDateTime now) {
        //아무 matchCondition필드에 값이 없는 경우 모든 조건으로 변환.

        List<RandomMatch> randomMatches = new ArrayList<>();
        MatchConditionRandomMatchDto matchConditionRandomMatchDto = randomMatchDto.getMatchConditionRandomMatchDto();
        if (randomMatchDto.getContentCategory().equals(ContentCategory.STUDY) &&
            matchConditionRandomMatchDto.getTypeOfStudyList().isEmpty()) {
            matchConditionRandomMatchDto.getTypeOfStudyList()
                .addAll(List.of(TypeOfStudy.values()));
        } else if (randomMatchDto.getContentCategory().equals(ContentCategory.MEAL) &&
            matchConditionRandomMatchDto.getWayOfEatingList().isEmpty()) {
            matchConditionRandomMatchDto.getWayOfEatingList()
                .addAll(List.of(WayOfEating.values()));
        }
        // redis 조건에 따라 여러 데이터 생성
        for (Place place : matchConditionRandomMatchDto.getPlaceList()) {
            if (randomMatchDto.getContentCategory().equals(ContentCategory.STUDY)) {
                for (TypeOfStudy typeOfStudy : matchConditionRandomMatchDto.getTypeOfStudyList()) {
                    randomMatches.add(new StudyRandomMatch(ContentCategory.MEAL,
                        place, member, typeOfStudy, now));
                }
            } else if (randomMatchDto.getContentCategory().equals(ContentCategory.MEAL)) {
                for (WayOfEating wayOfEating : matchConditionRandomMatchDto.getWayOfEatingList()) {
                    randomMatches.add(new MealRandomMatch(ContentCategory.MEAL,
                        place, member, wayOfEating, now));
                }
            }
        }
        return randomMatches;
    }


}
