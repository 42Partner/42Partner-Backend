package partner42.moduleapi.controller.random;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import partner42.moduleapi.dto.random.RandomMatchCountResponse;
import partner42.moduleapi.dto.random.RandomMatchExistDto;
import partner42.moduleapi.dto.matchcondition.MatchConditionRandomMatchDto;
import partner42.moduleapi.dto.random.RandomMatchCancelRequest;
import partner42.moduleapi.dto.random.RandomMatchDto;
import partner42.moduleapi.dto.random.RandomMatchSearch;
import partner42.moduleapi.service.random.RandomMatchService;
import partner42.modulecommon.domain.model.match.ContentCategory;
import partner42.modulecommon.exception.ErrorCode;
import partner42.modulecommon.exception.InvalidInputException;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RandomMatchController {

    private final RandomMatchService randomMatchService;

    @PreAuthorize("hasAuthority('random-match.create')")
    @Operation(summary = "랜덤 매칭 신청", description = "랜덤 매칭 신청")
    @PostMapping("/random-matches")
    public ResponseEntity<Void> applyRandomMatch(
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user,
        @Validated @Parameter @RequestBody RandomMatchDto randomMatchDto) {
        //contentCategory에 따라 필드 검증
        verifyRandomMatchDtoHasEmptyField(randomMatchDto);
        randomMatchService.createRandomMatch(user.getUsername(), randomMatchDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }



    @PreAuthorize("hasAuthority('random-match.delete')")
    @Operation(summary = "랜덤 매칭 취소", description = "랜덤 매칭 취소")
    @PostMapping("/random-matches/mine")
    public ResponseEntity<Void> cancelRandomMatch(@Validated @Parameter @RequestBody RandomMatchCancelRequest randomMatchCancelRequest,
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user) {
        //contentCategory에 따라 필드 검증
        randomMatchService.deleteRandomMatch(user.getUsername(), randomMatchCancelRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasAuthority('random-match.read')")
    @Operation(summary = "랜덤 매칭 신청 여부 조회", description = "랜덤 매칭 신청 여부 조회")
    @GetMapping("/random-matches/mine")
    public RandomMatchExistDto checkRandomMatchExist(RandomMatchSearch randomMatchCancelRequest,
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user) {
        //contentCategory에 따라 필드 검증
        return randomMatchService.checkRandomMatchExist(user.getUsername(), randomMatchCancelRequest);
    }

    @Operation(summary = "랜덤 매칭 신청 인원 조회", description = "랜덤 매칭 신청 인원 조회")
    @GetMapping("/random-matches/members/count")
    public RandomMatchCountResponse countRandomMatchNotExpired(RandomMatchSearch randomMatchCancelRequest) {
        return randomMatchService.countRandomMatchNotExpired(randomMatchCancelRequest);
    }

    @PreAuthorize("hasAuthority('random-match.read')")
    @Operation(summary = "랜덤 매칭 신청 조건 조회", description = "랜덤 매칭 신청 조건 조회")
    @GetMapping("/random-matches/condition/mine")
    public RandomMatchDto readRandomMatchCondition(RandomMatchSearch randomMatchCancelRequest,
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user) {
        //contentCategory에 따라 필드 검증
        return randomMatchService.readRandomMatchCondition(user.getUsername(), randomMatchCancelRequest);
    }

    private void verifyRandomMatchDtoHasEmptyField(RandomMatchDto randomMatchDto) {
        ContentCategory contentCategory = randomMatchDto.getContentCategory();
        MatchConditionRandomMatchDto matchConditionRandomMatchDto = randomMatchDto.getMatchConditionRandomMatchDto();
        if (matchConditionRandomMatchDto.getPlaceList().isEmpty() ||
            (contentCategory == ContentCategory.MEAL && matchConditionRandomMatchDto.getWayOfEatingList().isEmpty()) ||
            (contentCategory == ContentCategory.STUDY && matchConditionRandomMatchDto.getTypeOfStudyList().isEmpty())) {
            throw new InvalidInputException(ErrorCode.MATCH_CONDITION_EMPTY);
        }
    }
}
