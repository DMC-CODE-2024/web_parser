package app.web_parser.repository.app;

import app.web_parser.model.app.OperatorSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatorSeriesRepository extends JpaRepository<OperatorSeries, Integer> {
}
