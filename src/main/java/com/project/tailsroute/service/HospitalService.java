package com.project.tailsroute.service;

import com.project.tailsroute.repository.HospitalRepository;
import com.project.tailsroute.vo.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public void doInsertHospitalInfo(String callNumber, String addressLocation, String addressStreet, String name, String latitude, String longitude) {
        hospitalRepository.doInsertHospitalInfo(callNumber, addressLocation, addressStreet, name, latitude, longitude);
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

    public int getHospitalsCount() {
        return hospitalRepository.getHospitalsCount();
    }

    public String getX(int i) {
        return hospitalRepository.getX(i);
    }

    public String getY(int i) {
        return hospitalRepository.getY(i);
    }

    public void setLatLon(int i, double lat, double lon) {
        hospitalRepository.setLatLon(i, lat, lon);
        System.err.println(i + "번째 행 Update...");
    }
}
