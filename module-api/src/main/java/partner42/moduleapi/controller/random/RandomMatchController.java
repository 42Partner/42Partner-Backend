package partner42.moduleapi.controller.random;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import partner42.moduleapi.dto.random.RandomMatchCancelRequest;
import partner42.moduleapi.dto.random.RandomMatchDto;
import partner42.moduleapi.service.random.RandomMatchService;
import partner42.modulecommon.domain.model.user.User;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RandomMatchController {

    private final RandomMatchService randomMatchService;

    @PreAuthorize("isAuthenticated() and hasAuthority('random.create')")
    @Operation(summary = "랜덤 매칭 신청", description = "랜덤 매칭 신청")
    @PostMapping("/random-matches")
    public ResponseEntity<Void> applyRandomMatch(
        @ApiParam(hidden = true) @AuthenticationPrincipal String username,
        @Validated @Parameter @RequestBody RandomMatchDto randomMatchDto) {
        //contentCategory에 따라 필드 검증
        return randomMatchService.createRandomMatch(username, randomMatchDto);
    }

    @PreAuthorize("isAuthenticated() and hasAuthority('random.delete')")
    @Operation(summary = "랜덤 매칭 취소", description = "랜덤 매칭 취소")
    @PostMapping("/random-matches/mine")
    public ResponseEntity<Void> cancelRandomMatch(@Validated @Parameter @RequestBody RandomMatchCancelRequest randomMatchCancelRequest,
        @ApiParam(hidden = true) @AuthenticationPrincipal String username) {
        //contentCategory에 따라 필드 검증
        return randomMatchService.deleteRandomMatch(username, randomMatchCancelRequest);
    }
}
