package partner42.moduleapi.service.random;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import partner42.moduleapi.dto.matchcondition.MatchConditionRandomMatchDto;
import partner42.moduleapi.dto.matchcondition.MatchConditionRandomMatchDto.MatchConditionRandomMatchDtoBuilder;
import partner42.moduleapi.dto.random.RandomMatchCancelRequest;
import partner42.moduleapi.dto.random.RandomMatchCountResponse;
import partner42.moduleapi.dto.random.RandomMatchDto;
import partner42.moduleapi.dto.random.RandomMatchExistDto;
import partner42.moduleapi.dto.random.RandomMatchSearch;
import partner42.modulecommon.utils.CustomTimeUtils;
import partner42.modulecommon.domain.model.match.ContentCategory;
import partner42.modulecommon.domain.model.matchcondition.Place;
import partner42.modulecommon.domain.model.matchcondition.TypeOfStudy;
import partner42.modulecommon.domain.model.matchcondition.WayOfEating;
import partner42.modulecommon.domain.model.member.Member;
import partner42.modulecommon.domain.model.random.MealRandomMatch;
import partner42.modulecommon.domain.model.random.RandomMatch;
import partner42.modulecommon.domain.model.random.StudyRandomMatch;
import partner42.modulecommon.exception.ErrorCode;
import partner42.modulecommon.exception.InvalidInputException;
import partner42.modulecommon.exception.NoEntityException;
import partner42.modulecommon.exception.RandomMatchAlreadyExistException;
import partner42.modulecommon.repository.random.RandomMatchRepository;
import partner42.modulecommon.repository.user.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RandomMatchService {

    private final UserRepository userRepository;
    private final RandomMatchRepository randomMatchRepository;


    @Transactional
    public void createRandomMatch(String username,
        RandomMatchDto randomMatchDto) {
        Member member = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(
                ErrorCode.ENTITY_NOT_FOUND)).getMember();
        //"2020-12-01T00:00:00"
        LocalDateTime now = CustomTimeUtils.nowWithoutNano();
        //?????? RandomMatch.MAX_WAITING_TIME??? ????????? ?????? ?????? ????????? ??? ?????? ?????? ??????
        verifyAlreadyApplied(randomMatchDto.getContentCategory(), member, now);

        //?????? dto??? ?????? ?????? ?????? ?????? ????????? ??? ???????????? RandomMatch ???????????? ??????
        List<RandomMatch> randomMatches = makeAllAvailRandomMatchesFromRandomMatchDto(
            randomMatchDto, member, now);

        //?????? ?????? ????????? ??? DB??? ??????.
        randomMatchRepository.saveAll(randomMatches);

    }


//    private void verifyAlreadyAppliedPessimisticWriteLock(ContentCategory contentCategory, Member member,
//        LocalDateTime now) {
//        if ((contentCategory.equals(ContentCategory.MEAL) &&
//            randomMatchRepository.findMealPessimisticWriteByCreatedAtBeforeAndIsExpiredAndMemberId(
//                now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
//                member.getId(), false).size() > 0) ||
//            (contentCategory.equals(ContentCategory.STUDY) &&
//                randomMatchRepository.findStudyPessimisticWriteByCreatedAtBeforeAndIsExpiredAndMemberId(
//                    now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
//                    member.getId(), false).size() > 0)) {
//
//            throw new RandomMatchAlreadyExistException(ErrorCode.RANDOM_MATCH_ALREADY_EXIST);
//        }
//    }

    private void verifyAlreadyApplied(ContentCategory contentCategory, Member member,
        LocalDateTime now) {
        if ((contentCategory.equals(ContentCategory.MEAL) &&
            randomMatchRepository.findMealByCreatedAtBeforeAndIsExpiredAndMemberId(
                now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
                member.getId(), false).size() > 0) ||
            (contentCategory.equals(ContentCategory.STUDY) &&
                randomMatchRepository.findStudyByCreatedAtBeforeAndIsExpiredAndMemberId(
                    now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
                    member.getId(), false).size() > 0)) {

            throw new RandomMatchAlreadyExistException(ErrorCode.RANDOM_MATCH_ALREADY_EXIST);
        }
    }

    @Transactional
    public void deleteRandomMatch(String username,
        RandomMatchCancelRequest request) {
        Long memberId = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(
                ErrorCode.ENTITY_NOT_FOUND)).getMember().getId();
        List<RandomMatch> randomMatches = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        //????????? ??? RandomMatch.MAX_WAITING_TIME??? ?????? + ???????????? ?????? ?????? ?????? ????????? ??????.
        ContentCategory contentCategory = request.getContentCategory();
        if (contentCategory == ContentCategory.MEAL) {
            randomMatches.addAll(
                randomMatchRepository.findMealPessimisticWriteByCreatedAtBeforeAndIsExpiredAndMemberId(
                    now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
                    memberId, false));
        } else if (contentCategory == ContentCategory.STUDY) {
            randomMatches.addAll(
                randomMatchRepository.findStudyPessimisticWriteByCreatedAtBeforeAndIsExpiredAndMemberId(
                    now.minusMinutes(RandomMatch.MAX_WAITING_TIME),
                    memberId, false));
        }
        // ???????????? randomMatch??? db??? ????????? ????????? ????????? ?????? ?????? exception
        if (randomMatches.isEmpty()) {
            throw new InvalidInputException(ErrorCode.ALREADY_CANCELED_RANDOM_MATCH);
        }

        //db????????? ??????
        randomMatches
            .forEach(RandomMatch::expire);

//        //redis ?????? ????????????
//        /**
//         * redis ??????????????? ???????????? ????????? mysql???????????? ???????????????
//         * redis?????? ?????????????????? mysql????????? ???????????? ?????? ????????? ????????? ??? ??????.
//         * 2PhaseCommit?????? ?????????????????? ?????????, redis??? 2PhaseCommit??? ???????????? ?????????.
//         * eventQueue?????? ????????? ????????? ???????????? ???????????? ???????????????.
//         */
//        redisTransactionUtil.wrapTransaction(() -> {
//                randomMatches.forEach(randomMatch ->
//                    randomMatchRedisRepository.deleteSortedSet(randomMatch.toKey(),
//                        randomMatch.toValue()));
//            }
//        );
    }


    /**
     * ?????? dto??? ?????? ?????? ?????? ?????? ????????? ??? ???????????? RandomMatch ???????????? ??????
     *
     * @param randomMatchDto
     * @return
     */
    private List<RandomMatch> makeAllAvailRandomMatchesFromRandomMatchDto(
        RandomMatchDto randomMatchDto, Member member, LocalDateTime now) {
        //?????? matchCondition????????? ?????? ?????? ?????? ?????? ???????????? ??????.

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
        // redis ????????? ?????? ?????? ????????? ??????
        for (Place place : matchConditionRandomMatchDto.getPlaceList()) {
            if (randomMatchDto.getContentCategory().equals(ContentCategory.STUDY)) {
                for (TypeOfStudy typeOfStudy : matchConditionRandomMatchDto.getTypeOfStudyList()) {
                    randomMatches.add(new StudyRandomMatch(ContentCategory.STUDY,
                        place, member, typeOfStudy));
                }
            } else if (randomMatchDto.getContentCategory().equals(ContentCategory.MEAL)) {
                for (WayOfEating wayOfEating : matchConditionRandomMatchDto.getWayOfEatingList()) {
                    randomMatches.add(new MealRandomMatch(ContentCategory.MEAL,
                        place, member, wayOfEating));
                }
            }
        }
        return randomMatches;
    }


    public RandomMatchExistDto checkRandomMatchExist(String username,
        RandomMatchSearch randomMatchCancelRequest) {
        Member member = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(
                ErrorCode.ENTITY_NOT_FOUND)).getMember();
        try {
            verifyAlreadyApplied(randomMatchCancelRequest.getContentCategory(), member,
                LocalDateTime.now());
            return RandomMatchExistDto.builder()
                .isExist(false).build();
        } catch (RandomMatchAlreadyExistException e) {
            return RandomMatchExistDto.builder()
                .isExist(true).build();
        }
    }

    public RandomMatchDto readRandomMatchCondition(String username,
        RandomMatchSearch randomMatchCancelRequest) {

        Member member = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(
                ErrorCode.ENTITY_NOT_FOUND)).getMember();
        Long memberId = member.getId();
        LocalDateTime now = LocalDateTime.now();
        List<RandomMatch> randomMatches = new ArrayList<>();
        MatchConditionRandomMatchDtoBuilder builder = MatchConditionRandomMatchDto.builder();
        if (randomMatchCancelRequest.getContentCategory() == ContentCategory.MEAL) {
            randomMatches = randomMatchRepository.findMealByCreatedAtBeforeAndIsExpiredAndMemberId(
                now.minusMinutes(RandomMatch.MAX_WAITING_TIME), memberId, false);
            builder = builder.wayOfEatingList(new ArrayList<>(randomMatches.stream()
                .map(randomMatch -> ((MealRandomMatch) randomMatch).getWayOfEating())
                .collect(Collectors.toSet())));
        } else if (randomMatchCancelRequest.getContentCategory() == ContentCategory.STUDY) {
            randomMatches = randomMatchRepository.findStudyByCreatedAtBeforeAndIsExpiredAndMemberId(
                now.minusMinutes(RandomMatch.MAX_WAITING_TIME), memberId, false);
            builder = builder.typeOfStudyList(new ArrayList<>(randomMatches.stream()
                .map(randomMatch -> ((StudyRandomMatch) randomMatch).getTypeOfStudy())
                .collect(Collectors.toSet())));
        }
        MatchConditionRandomMatchDto matchConditionRandomMatchDto = builder.placeList(
                new ArrayList<>(randomMatches.stream()
                    .map(RandomMatch::getPlace)
                    .collect(Collectors.toSet())))
            .build();
        return RandomMatchDto.builder()
            .contentCategory(randomMatchCancelRequest.getContentCategory())
            .matchConditionRandomMatchDto(matchConditionRandomMatchDto)
            .build();
    }

    public RandomMatchCountResponse countRandomMatchNotExpired(
        RandomMatchSearch randomMatchCancelRequest) {
        LocalDateTime now = LocalDateTime.now();

        List<RandomMatch> randomMatches = randomMatchRepository.findRandomMatchesByCreatedAtBeforeAndIsExpired(
            now.minusMinutes(RandomMatch.MAX_WAITING_TIME), false, randomMatchCancelRequest.getContentCategory());

        int randomMatchParticipantCount = randomMatches.stream()
            .map(RandomMatch::getMember)
            .map(Member::getNickname)
            .collect(Collectors.toSet())
            .size();

        return RandomMatchCountResponse.builder()
            .randomMatchCount(randomMatchParticipantCount)
            .build();
    }
}
