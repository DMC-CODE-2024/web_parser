package app.web_parser.repository.rep;

import app.web_parser.model.rep.IosTotalInstallationsByAppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IosTotalInstallationsByAppVersionRepository extends JpaRepository<IosTotalInstallationsByAppVersion, Integer> {
}

