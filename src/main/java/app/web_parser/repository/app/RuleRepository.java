package app.web_parser.repository.app;

import app.web_parser.dto.RuleDto;
import app.web_parser.model.app.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {


    @Query("SELECT NEW app.web_parser.dto.RuleDto(a.id, a.name, b.output, b.graceAction, " +
            "b.postGraceAction, b.failedRuleActionGrace, b.failedRuleActionPostGrace, b.ruleMessage) " +
            "FROM Rule a, FeatureRule b " +
            "WHERE a.name = b.name AND a.state = :state AND b.feature = :feature " +
            "ORDER BY b.ruleOrder ASC")
    List<RuleDto> getRuleDetails(@Param("feature") String feature, @Param("state") String state);
}
