package partner42.moduleapi.controller.article;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import partner42.moduleapi.dto.article.ArticleDto;
import partner42.moduleapi.dto.article.ArticleOnlyIdResponse;
import partner42.moduleapi.dto.article.ArticleReadOneResponse;
import partner42.moduleapi.dto.article.ArticleReadResponse;
import partner42.moduleapi.service.article.ArticleService;
import partner42.modulecommon.domain.model.user.User;
import partner42.modulecommon.repository.article.ArticleSearch;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "방 하나 상세조회", description = "방 상세페이지")
    @GetMapping("/articles/{articleId}")
    public ArticleReadOneResponse readOneArticle(
        @PathVariable String articleId) {
        return articleService.readOneArticle(articleId);
    }

    @Operation(summary = "방 목록조회", description = "방 목록 페이지, ")
    @GetMapping("/articles")
    public SliceImpl<ArticleReadResponse> readAllArticle(Pageable pageable, ArticleSearch condition) {
        return articleService.readAllArticle(pageable, condition);
    }


//    @PreAuthorize("hasAuthority('article.create') OR "
//        + "(hasAuthority('article.create') AND @customAuthenticationManager.userIdMatches(authentication, #articleRequest))")
    @Operation(summary = "방 매칭 글쓰기", description = "방 매칭 글쓰기")
    @PostMapping("/articles")
    public ArticleOnlyIdResponse writeArticle(
        @ApiParam(hidden = true) @AuthenticationPrincipal String userApiId,
        @Validated @Parameter @RequestBody ArticleDto articleRequest) {
        return articleService.createArticle(userApiId, articleRequest);
    }

    @Operation(summary = "방 매칭 글수정", description = "방 매칭 글 수정")
    @PutMapping("/articles/{articleId}")
    public ArticleOnlyIdResponse updateArticle(@Validated @Parameter @RequestBody ArticleDto articleRequest,
        @PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId) {
        return articleService.updateArticle(articleRequest, userApiId, articleId);
    }

    @Operation(summary = "방 매칭 삭제", description = "방 매칭 삭제")
    @DeleteMapping("/articles/{articleId}")
    public ArticleOnlyIdResponse deleteArticle(@PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId) {
        return articleService.deleteArticle(userApiId, articleId);
    }

    @Operation(summary = "방 매칭 글 임시 삭제", description = "방 매칭 글 임시 삭제")
    @PostMapping("/articles/{articleId}/recoverable-delete")
    public ArticleOnlyIdResponse recoverableDeleteArticle(@PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId
    ) {
        return articleService.changeIsDelete(userApiId, articleId);
    }

    @Operation(summary = "방 매칭 참여", description = "방 매칭 참여")
    @PostMapping("/articles/{articleId}/participate")
    public ArticleOnlyIdResponse participateArticle(@PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId
    ) {
        return articleService.participateArticle(userApiId, articleId);
    }

    @Operation(summary = "방 매칭 참여 최소", description = "방 매칭 참여 최소")
    @PostMapping("/articles/{articleId}/participate-cancel")
    public ArticleOnlyIdResponse participateCancelArticle(@PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId
    ) {
        return articleService.participateCancelArticle(userApiId, articleId);
    }

    //작성자인지 확인하는 권한 처리.
    @Operation(summary = "방 매칭 글 확정", description = "방 매칭 글 확정")
    @PostMapping("/articles/{articleId}/complete")
    public ArticleOnlyIdResponse completeArticle(@PathVariable String articleId,
        @Parameter(hidden = true) @AuthenticationPrincipal String userApiId
    ) {
        return articleService.completeArticle(userApiId, articleId);
    }

}
