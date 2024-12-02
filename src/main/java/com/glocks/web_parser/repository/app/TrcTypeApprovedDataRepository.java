package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.TrcTypeApprovedData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrcTypeApprovedDataRepository extends JpaRepository<TrcTypeApprovedData, Long> {


    @Transactional
    @Modifying
    @Query("delete from TrcTypeApprovedData u where u.model=:model")
    void deleteByModel(@Param("model") String model);

}
