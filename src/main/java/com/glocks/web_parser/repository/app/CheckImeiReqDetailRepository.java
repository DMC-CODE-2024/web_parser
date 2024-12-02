package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.CheckImeiReqDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckImeiReqDetailRepository extends JpaRepository<CheckImeiReqDetail, Integer> {
}
