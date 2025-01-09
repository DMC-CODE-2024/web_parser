package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.dto.EirsResponseParamDto;
import com.glocks.web_parser.model.app.EirsResponseParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EirsResponseParamRepository extends JpaRepository<EirsResponseParam, Integer> {

/*
    @Query("select u.value from EirsResponseParam u where u.featureName= :featureName and u.language= :language and u.tag= :tag")
    String findValue(@Param("featureName") String featureName,
                                                 @Param("language") String language, @Param("tag") String tag);
*/
@Query("select NEW com.glocks.web_parser.dto.EirsResponseParamDto(u.subject,u.value) FROM EirsResponseParam u where u.language= :language and u.tag= :tag")
EirsResponseParamDto findValue(@Param("language") String language, @Param("tag") String tag);

    List<EirsResponseParam> findByFeatureNameIn(List<String> module);

    public EirsResponseParam getByTagAndLanguage(String tag, String language);
}
