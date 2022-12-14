package partner42.moduleapi.service.article;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import partner42.moduleapi.dto.article.ArticleDto;
import partner42.moduleapi.dto.article.ArticleOnlyIdResponse;
import partner42.moduleapi.dto.matchcondition.MatchConditionDto;
import partner42.moduleapi.mapper.MatchConditionMapper;
import partner42.moduleapi.mapper.MemberMapper;
import partner42.moduleapi.utils.CreateTestDataUtils;
import partner42.moduleapi.utils.WorkerWithCountDownLatch;
import partner42.modulecommon.domain.model.match.ContentCategory;
import partner42.modulecommon.domain.model.member.Member;
import partner42.modulecommon.repository.activity.ActivityRepository;
import partner42.modulecommon.repository.article.ArticleRepository;
import partner42.modulecommon.repository.articlemember.ArticleMemberRepository;
import partner42.modulecommon.repository.match.MatchMemberRepository;
import partner42.modulecommon.repository.match.MatchRepository;
import partner42.modulecommon.repository.matchcondition.ArticleMatchConditionRepository;
import partner42.modulecommon.repository.matchcondition.MatchConditionMatchRepository;
import partner42.modulecommon.repository.matchcondition.MatchConditionRepository;
import partner42.modulecommon.repository.member.MemberRepository;
import partner42.modulecommon.repository.user.UserRepository;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CreateTestDataUtils createTestDataUtils;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * ????????? ??? AOP?????? ????????? ??????????????? ??????
     */
    @Test
    void participateArticle() throws Exception{
        //given
        createTestDataUtils.signUpUsers();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ArticleDto articleDto = ArticleDto.builder()
            .anonymity(false)
            .content("content")
            .title("title")
            .date(tomorrow)
            .contentCategory(ContentCategory.MEAL)
            .participantNumMax(3)
            .matchConditionDto(MatchConditionDto.builder()
                .placeList(new ArrayList<>())
                .timeOfEatingList(new ArrayList<>())
                .wayOfEatingList(new ArrayList<>())
                .build())
            .build();

        ArticleOnlyIdResponse articleOnlyIdResponse = articleService.createArticle("takim@student.42Seoul.kr",
            articleDto);
        //when
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //????????? ??? ????????? ???????????? ??? retry??? ??????????????? ??? ???????????? ??????.
        WorkerWithCountDownLatch sorkimParticipate = new WorkerWithCountDownLatch(
            "sorkim participate", countDownLatch, () ->
        {

            articleService.participateArticle("sorkim@student.42Seoul.kr",
                articleOnlyIdResponse.getArticleId());
        });

        WorkerWithCountDownLatch hyenamParticipate = new WorkerWithCountDownLatch(
            "hyenam participate", countDownLatch, () ->
        {
            articleService.participateArticle("hyenam@student.42Seoul.kr",
                articleOnlyIdResponse.getArticleId());
        });
        sorkimParticipate.start();
        hyenamParticipate.start();

        Thread.sleep(10);
        log.info("-----------------------------------------------");
        log.info(" Now release the latch:");
        log.info("-----------------------------------------------");
        countDownLatch.countDown();
        Thread.sleep(2000);
        //then
        Assertions.assertThat(
            articleRepository.findByApiId(articleOnlyIdResponse.getArticleId()).get()
                .getParticipantNum()).isEqualTo(3);

    }

}