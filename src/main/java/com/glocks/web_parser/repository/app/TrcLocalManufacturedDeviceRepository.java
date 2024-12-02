package com.glocks.web_parser.repository.app;


import com.glocks.web_parser.model.app.TrcLocalManufacturedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrcLocalManufacturedDeviceRepository extends JpaRepository<TrcLocalManufacturedDevice, Long> {
}
