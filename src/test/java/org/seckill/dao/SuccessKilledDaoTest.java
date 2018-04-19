package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	
	@Autowired
	private SuccessKilledDao successKilledDao;
	
	@Test
	public void testinsertSuccessKilled(){
		/**
		 * 第一次：insertCount=1
		 * 第二次：insertCount=0
		 */
		long id = 1000L;
		long userPhone = 18758261111L;
		int insertCount = successKilledDao.insertSuccessKilled(id, userPhone);
		System.out.println("insertCount="+insertCount);
	}
	
	@Test
	public void testqueryByIdWithSeckill(){
		long id = 1000L;
		long userPhone = 18758261111L;
		SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, userPhone);
		System.out.println(successKilled);
		System.out.println(successKilled.getSeckill());
	}
}
