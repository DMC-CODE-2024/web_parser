package app.web_parser.repository.app;

import app.web_parser.model.app.ImeiPairDetailHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImeiPairDetailHisRepository extends JpaRepository<ImeiPairDetailHis, Integer> {
}
