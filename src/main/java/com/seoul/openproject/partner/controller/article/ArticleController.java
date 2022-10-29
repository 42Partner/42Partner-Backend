package com.seoul.openproject.partner.controller.article;

import com.seoul.openproject.partner.domain.model.article.Article;
import com.seoul.openproject.partner.domain.model.user.User;
import com.seoul.openproject.partner.error.ErrorResult;
import com.seoul.openproject.partner.service.article.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @ExceptionHandler
    public ResponseEntity<ErrorResult> entityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResult.builder()
                .message(e.getMessage())
                .build());
    }
    @Operation(summary = "방 하나 상세조회", description = "방 상세페이지")
    @GetMapping("/articles/{articleId}")
    public Article.ArticleReadOneResponse readOneRoomMatching(@PathVariable String articleId) {
        return articleService.readOneArticle(articleId);
    }


//    @PreAuthorize("hasAuthority('article.create') OR "
//        + "(hasAuthority('article.create') AND @customAuthenticationManager.userIdMatches(authentication, #articleRequest))")
    @Operation(summary = "방 매칭 글쓰기", description = "방 매칭 글쓰기")
    @PostMapping("/articles")
    public Article.ArticleOnlyIdResponse writeRoomMatching(@Validated @Parameter @RequestBody Article.ArticleDto articleRequest) {
        return articleService.createArticle(articleRequest);
    }

    @Operation(summary = "방 매칭 글수정", description = "방 매칭 글쓰기")
    @PutMapping("/articles/{articleId}")
    public Article.ArticleOnlyIdResponse updateRoomMatching(@Validated @Parameter @RequestBody Article.ArticleDto articleRequest,
        @PathVariable String articleId) {
        return articleService.updateArticle(articleRequest, articleId);
    }

    @Operation(summary = "방 매칭 삭제", description = "방 매칭 삭제")
    @DeleteMapping("/articles/{articleId}")
    public Article.ArticleOnlyIdResponse deleteRoomMatching(@PathVariable String articleId) {
        return articleService.deleteArticle(articleId);
    }

}
