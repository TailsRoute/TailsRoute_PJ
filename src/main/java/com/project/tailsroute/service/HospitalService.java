package com.project.tailsroute.service;

import com.project.tailsroute.repository.HospitalRepository;
import com.project.tailsroute.vo.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    // 루트 디렉토리에 있는 .env 파일에서 API 키를 불러옴.
    @Value("${GOOGLE_MAP_API_KEY}")
    private String API_KEY;


    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public void doInsertHospitalInfo(String callNumber, String addressLocation, String addressStreet, String name) {
        hospitalRepository.doInsertHospitalInfo(callNumber, addressLocation, addressStreet, name);
    }

    public List<Hospital> get24HourHospitals() {
        return hospitalRepository.findHospitalsByType("24시간");
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAllHospitals();
    }

    public List<Hospital> getHospitalsWithoutCoordinates() {
        return hospitalRepository.getHospitalsWithoutCoordinates();
    }

    public void updateHospitalCoordinates(int id, String latitude, String longitude) {
        hospitalRepository.updateHospitalCoordinates(id, latitude, longitude);
    }

    public List<Hospital> getFilteredHospitals(String type, String region) {
        if (type != null && region != null) {
            return hospitalRepository.findByTypeAndRegion(type, region);
        } else if (type != null) {
            return hospitalRepository.findByType(type);
        } else if (region != null) {
            return hospitalRepository.findByRegion(region);
        } else {
            return hospitalRepository.findAll();
        }
    }

    public List<Hospital> getHospitalsByTypeAndRegion(String type, String region) {

//        System.err.println("Service) type: " + type + " region: " + region);

        return hospitalRepository.findHospitalsByTypeAndRegion(type, region);
    }
}
