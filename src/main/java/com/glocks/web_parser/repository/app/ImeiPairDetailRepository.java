package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.ImeiPairDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImeiPairDetailRepository extends JpaRepository<ImeiPairDetail, Integer> {

    @Query(value = "SELECT * from imei_pair_detail  WHERE created_on >=:createdOn and imei=:imei",nativeQuery = true)
    Optional<List<ImeiPairDetail>> findByCreatedOnGreaterThanEqual(String createdOn,String imei);
}
