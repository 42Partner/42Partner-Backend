package com.Seoul.OpenProject.Partner.domain.model;


import com.Seoul.OpenProject.Partner.domain.model.user.Role;
import com.Seoul.OpenProject.Partner.domain.model.user.User;
import com.Seoul.OpenProject.Partner.domain.model.user.UserRole;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "MEMBER", uniqueConstraints = {
    @UniqueConstraint(name = "NICK_NAME_UNIQUE", columnNames = {"nickname"}),
    @UniqueConstraint(name = "API_ID_UNIQUE", columnNames = {"apiId"}),
    @UniqueConstraint(name = "SLACK_EMAIL_UNIQUE", columnNames = {"slackEmail"})
})
@Entity
public class Member {
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
    @Column(name = "MEMBER_ID")
    private Long id;


    /********************************* PK가 아닌 필드 *********************************/

    /**
     * AUTH에 필요한 필드
     */

    @Column(nullable = false, updatable = false)
    private String apiId;

    @Column(unique = true, nullable = false, length = 30)
    private String nickname;

    @Column(length = 80)
    private String slackEmail;


    /********************************* 비영속 필드 *********************************/

    /********************************* 연관관계 매핑 *********************************/




    /********************************* 연관관계 편의 메서드 *********************************/

    /********************************* 생성 메서드 *********************************/


    /********************************* 비니지스 로직 *********************************/

}
