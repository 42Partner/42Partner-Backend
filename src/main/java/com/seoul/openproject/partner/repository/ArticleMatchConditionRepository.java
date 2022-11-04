package com.seoul.openproject.partner.repository;

import com.seoul.openproject.partner.domain.model.matchcondition.ArticleMatchCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleMatchConditionRepository extends
    JpaRepository<ArticleMatchCondition, Long> {

}