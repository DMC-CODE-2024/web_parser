package app.web_parser.repository.rep;

import app.web_parser.model.rep.IosTotalDownloadsByDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IosTotalDownloadsByDeviceRepository extends JpaRepository<IosTotalDownloadsByDevice, Integer> {
}
