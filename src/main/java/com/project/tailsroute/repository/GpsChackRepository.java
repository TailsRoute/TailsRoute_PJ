package com.project.tailsroute.repository;

import com.project.tailsroute.vo.GpsChack;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GpsChackRepository {

	@Select("""
		   SELECT *
           FROM gpsChack
           WHERE memberId = #{memberId}		
             """)
	GpsChack chack(int memberId);

	@Insert("""
            INSERT INTO gpsChack
            SET
			memberId = #{memberId}, 
            regDate = NOW(),
            updateDate = NOW(),            
            latitude = #{latitude},
            longitude = #{longitude},
         	location = #{location}
              			""")
	void on(double latitude, double longitude, String location, int memberId);

	@Insert("""
            INSERT INTO gpsChack
            SET
			memberId = #{memberId}, 
            regDate = NOW(),
            updateDate = NOW()
              			""")
	void off(int memberId);

	@Update("""
            UPDATE gpsChack
            SET
            updateDate = NOW(),            
            latitude = #{latitude},
            longitude = #{longitude},
         	location = #{location}
            WHERE memberId = #{memberId}
            """)
	void update(double latitude, double longitude, String location, int memberId);
}
