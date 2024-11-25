package com.project.tailsroute.repository;


import com.project.tailsroute.vo.WebPush;
import nl.martijndwars.webpush.Subscription;
import org.apache.ibatis.annotations.*;


@Mapper
public interface WebPushRepository {

	@Insert("""
            INSERT INTO WebPush
            SET 
            memberId = #{memberId},
            endpoint = #{subscription.endpoint},
            p256dh = #{subscription.keys.p256dh},
            auth = #{subscription.keys.auth}
            """)
	void save(int memberId, Subscription subscription);

	@Select("""
			SELECT *
			FROM webpush
			WHERE memberId = #{memberId}
			""")
    WebPush getMemberId(int memberId);

	@Delete("""
			DELETE FROM webpush
			WHERE memberId = #{memberId}
			""")
	void delete(int memberId);
}
