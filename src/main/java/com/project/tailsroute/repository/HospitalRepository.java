package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Hospital;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HospitalRepository {

    @Insert("""
                INSERT INTO `hospital` (name, callNumber, jibunAddress, roadAddress, latitude, longitude)
                VALUES (#{name}, #{callNumber}, #{addressLocation}, #{addressStreet}, #{latitude}, #{longitude})
            """)
    void doInsertHospitalInfo(String callNumber, String addressLocation, String addressStreet, String name, String latitude, String longitude);

    @Select("SELECT * FROM hospital")
    List<Hospital> findAllHospitals();

    @Select("SELECT id, IF(roadAddress = '', NULL, roadAddress) AS roadAddress, " +
            "  IF(jibunAddress = '', NULL, jibunAddress) AS jibunAddress FROM hospital WHERE latitude IS NULL OR longitude IS NULL")
    List<Hospital> getHospitalsWithoutCoordinates();

    @Update("UPDATE hospital SET latitude = #{latitude}, longitude = #{longitude} WHERE id = #{id}")
    void updateHospitalCoordinates(@Param("id") int id, @Param("latitude") String latitude, @Param("longitude") String longitude);

    @Select("""
            SELECT COUNT(*)
            FROM `hospital`
            """)
    int getHospitalsCount();

    @Select("""
            SELECT latitude
            FROM `hospital`
            WHERE id = #{id}
            """)
    String getX(int id);

    @Select("""
			SELECT latitude
			FROM `hospital`
			WHERE id = #{id}
			""")
    String getY(int id);

    @Update("""
			UPDATE `hospital`
			SET latitude = #{lat},
			longitude = #{lon}
			WHERE id = #{id}
			""")
    void setLatLon(int i, double lat, double lon);
}
