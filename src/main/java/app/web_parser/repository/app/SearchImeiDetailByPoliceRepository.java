package app.web_parser.repository.app;

import app.web_parser.model.app.SearchImeiDetailByPolice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface SearchImeiDetailByPoliceRepository extends JpaRepository<SearchImeiDetailByPolice, Long> {
/*    @Modifying
    @Query(value = "INSERT INTO search_imei_detail_by_police (imei,lost_date_time,created_by,transaction_id,request_id,device_owner_name,device_owner_address,contact_number,device_owner_national_id,device_lost_police_station,request_mode) SELECT :imeiNumber,device_lost_date_time,created_by,lost_id,request_id,device_owner_name,device_owner_address,contact_number,device_owner_national_id,police_station,request_mode FROM lost_device_mgmt WHERE request_id = :requestId", nativeQuery = true)
    int copyLostDeviceMgmtToSearchIMEIDetailByPolice(String imeiNumber,String requestId);*/
}