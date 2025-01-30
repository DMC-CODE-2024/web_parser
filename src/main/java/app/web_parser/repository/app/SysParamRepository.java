package app.web_parser.repository.app;


import app.web_parser.model.app.SysParam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysParamRepository extends JpaRepository<SysParam, Integer>, JpaSpecificationExecutor<SysParam> {

    @Query("select u.value from SysParam u where u.tag= :tag")
    String getValueFromTag(@Param("tag") String tag);

}
