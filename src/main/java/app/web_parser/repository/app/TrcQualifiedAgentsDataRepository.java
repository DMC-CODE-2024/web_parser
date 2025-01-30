package app.web_parser.repository.app;


import app.web_parser.model.app.TrcQualifiedAgentsData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrcQualifiedAgentsDataRepository extends JpaRepository<TrcQualifiedAgentsData, Long> {

    @Transactional
    @Modifying
    @Query("delete from TrcQualifiedAgentsData u where u.email=:email")
    void deleteByEmail(@Param("email") String email);
}
