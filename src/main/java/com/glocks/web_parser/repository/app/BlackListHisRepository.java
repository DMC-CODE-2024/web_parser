package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.BlackListHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListHisRepository extends JpaRepository<BlackListHis, Integer> {
}
