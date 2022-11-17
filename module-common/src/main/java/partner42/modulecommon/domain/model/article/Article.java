package partner42.modulecommon.domain.model.article;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import partner42.modulecommon.domain.model.BaseEntity;
import partner42.modulecommon.domain.model.match.ContentCategory;
import partner42.modulecommon.domain.model.matchcondition.ArticleMatchCondition;
import partner42.modulecommon.domain.model.member.Member;
import partner42.modulecommon.domain.model.opinion.Opinion;
import partner42.modulecommon.exception.ErrorCode;
import partner42.modulecommon.exception.InvalidInputException;
import partner42.modulecommon.exception.UnmodifiableArticleException;


@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "ARTICLE", uniqueConstraints = {
    @UniqueConstraint(name = "API_ID_UNIQUE", columnNames = {"apiId"}),
})
@Entity
public class Article extends BaseEntity {

//    @Autowired
//    private
    //********************************* static final 상수 필드 *********************************/

    /**
     * email 뒤에 붙는 문자열
     */

    /********************************* PK 필드 *********************************/

    /**
     * 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ARTICLE_ID")
    private Long id;

    /********************************* PK가 아닌 필드 *********************************/

    /**
     * AUTH에 필요한 필드
     */

    @Builder.Default
    @Column(nullable = false, updatable = false, length = 50)
    private final String apiId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String title;

    //longtext형
    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean anonymity;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isComplete = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer participantNum = 1;

    @Column(nullable = false)
    private Integer participantNumMax;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ContentCategory contentCategory;

    /********************************* 비영속 필드 *********************************/

    /********************************* 연관관계 매핑 *********************************/


    /*********************************  *********************************/

    @Builder.Default
    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,
        CascadeType.PERSIST})
    @Column(nullable = false, updatable = false)
    private List<ArticleMatchCondition> articleMatchConditions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,
        CascadeType.PERSIST})
    @Column(nullable = false, updatable = false)
    private List<ArticleMember> articleMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Column(nullable = false, updatable = false)
    private List<Opinion> opinions = new ArrayList<>();

    /********************************* 연관관계 편의 메서드 *********************************/

    /********************************* 생성 메서드 *********************************/

    public static Article of(LocalDate date, String title, String content, Boolean anonymity,
        Integer participantNumMax, ContentCategory contentCategory
//        ,
//        ArticleMember articleMember,
//        List<ArticleMatchCondition> articleMatchConditions
    ) {
        return Article.builder()
            .date(date)
            .title(title)
            .content(content)
            .anonymity(anonymity)
            .participantNumMax(participantNumMax)
            .contentCategory(contentCategory)
            .build();
//        articleMember.setArticle(article);
//        for (ArticleMatchCondition articleMatchCondition : articleMatchConditions) {
//            articleMatchCondition.setArticle(article);
//        }
    }

    /********************************* 비니지스 로직 *********************************/


    public void update(LocalDate date, String title, String content, Integer participantNumMax,
        List<ArticleMatchCondition> articleMatchConditions) {
        verifyDeleted();
        verifyCompleted();
        verifyChangeableParticipantNumMax(participantNumMax);
        this.date = date;
        this.title = title;
        this.content = content;
        this.participantNumMax = participantNumMax;
        for (ArticleMatchCondition articleMatchCondition : articleMatchConditions) {
            articleMatchCondition.setArticle(this);
        }
    }

    public Member getAuthorMember() {
        return this.getArticleMembers().stream()
            .filter((articleMember) ->
                articleMember.getIsAuthor())
            .map((articleMember) ->
                articleMember.getMember())
            .findFirst().orElseThrow(() -> (
                new IllegalStateException(ErrorCode.NO_AUTHOR.getMessage())
            ));
    }

    public List<Member> getParticipatedMembers() {
        return this.getArticleMembers().stream()
            .filter((articleMember) ->
                !articleMember.getIsAuthor())
            .map((articleMember) ->
                articleMember.getMember())
            .collect(Collectors.toList());
    }


    public boolean isDateToday() {
        return this.date.isEqual(LocalDate.now());

    }


    private void verifyChangeableParticipantNumMax(Integer participantNumMax) {
        if (this.participantNum > participantNumMax) {
            throw new InvalidInputException(ErrorCode.NOT_CHANGEABLE_PARTICIPANT_NUM_MAX);
        }
    }

    private void verifyDeleted() {
        if (this.isDeleted) {
            throw new UnmodifiableArticleException(ErrorCode.DELETED_ARTICLE);
        }
    }

    private void verifyFull() {
        if (this.participantNum >= this.participantNumMax) {
            throw new UnmodifiableArticleException(ErrorCode.FULL_ARTICLE);
        }
    }

    private void verifyEmpty() {
        if (this.participantNum <= 1) {
            throw new UnmodifiableArticleException(ErrorCode.EMPTY_ARTICLE);
        }
    }

    private void verifyCompleted() {
        if (this.isComplete) {
            throw new UnmodifiableArticleException(ErrorCode.COMPLETED_ARTICLE);
        }
    }


    private void verifyParticipatedMember(Member member) {

        if (this.getArticleMembers().stream()
            .anyMatch((articleMember) ->
                articleMember.getMember().equals(member))) {
            throw new InvalidInputException(ErrorCode.ALREADY_PARTICIPATED);
        }
    }

    private void verifyUnparticipatedMember(Member member) {

        if (this.getArticleMembers().stream()
            .noneMatch((articleMember) ->
                articleMember.getMember().equals(member))) {
            throw new InvalidInputException(ErrorCode.NOT_PARTICIPATED_MEMBER);
        }
    }


    public void complete() {
        verifyDeleted();
        verifyCompleted();
        this.isComplete = true;
    }

    public ArticleMember participateMember(Member member) {
        verifyDeleted();
        verifyCompleted();
        verifyFull();
        verifyParticipatedMember(member);
        ArticleMember participateMember = ArticleMember.of(member, false, this);
        this.participantNum++;
        return participateMember;
    }

    public ArticleMember participateCancelMember(Member member) {
        verifyDeleted();
        verifyCompleted();
        verifyEmpty();
        verifyUnparticipatedMember(member);
        ArticleMember participateMember = this.getArticleMembers().stream()
            .filter((articleMember1) ->
                articleMember1.getMember().equals(member))
            .findFirst().orElseThrow(() -> (
                new InvalidInputException(ErrorCode.NOT_PARTICIPATED_MEMBER)
            ));
        this.participantNum--;
        return participateMember;
    }

    public void recoverableDelete(){
        verifyDeleted();
        this.isDeleted = true;
    }


    /********************************* Dto *********************************/











}
