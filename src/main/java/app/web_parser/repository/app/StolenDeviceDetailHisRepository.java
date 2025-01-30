package app.web_parser.repository.app;

import app.web_parser.model.app.StolenDeviceDetailHis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface StolenDeviceDetailHisRepository extends JpaRepository<StolenDeviceDetailHis, Long> {

}
