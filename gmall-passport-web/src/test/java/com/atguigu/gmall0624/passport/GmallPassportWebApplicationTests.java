package com.atguigu.gmall0624.passport;

import com.atguigu.gmall0624.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void testJWT(){
		//生成token
		String key = "zzx";
		HashMap<String, Object> map = new HashMap<>();
		map.put("userId",111);
		map.put("nickName","啊啊");
		String salt = "192.168.5.1";
		String token = JwtUtil.encode(key, map, salt);

		System.out.println(token);
		System.out.println("_____________");

		//解密
		Map<String, Object> ml = JwtUtil.decode(token, key, salt);
		System.out.println("m1:"+ml);
		Map<String, Object> m2 = JwtUtil.decode(token,"bbb", salt);
		System.out.println("m2"+m2);
		Map<String, Object> m3 = JwtUtil.decode(token, key, "192.168.1.5");
		System.out.println("m3"+m3);
	}

}
