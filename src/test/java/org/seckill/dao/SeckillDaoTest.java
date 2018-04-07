package org.seckill.dao;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * ����Spring��junit�����ϣ�junit����ʱ����springIOC����
 * spring-test, junit
*/
@RunWith(SpringJUnit4ClassRunner.class)
/*
 * ����junit spring �����ļ�
 */
@ContextConfiguration({"classpath:spring/spring-mybatis.xml"})
public class SeckillDaoTest {
	
	@Autowired
	private SeckillDao seckillDao;
	
	@Test
	public void testReduceNumber() throws Exception {
		Date killTime = new Date();
		int result = seckillDao.reduceNumber(1000L, killTime);
		System.out.println(result);
	}
	
	@Test
	public void testQueryById() throws Exception {
		long id = 1000;
		Seckill seckill = seckillDao.queryById(id);
		System.out.println(seckill.getName());
		System.out.println(seckill);
	}
	
	@Test
	public void testQueryAll() throws Exception {
		//org.mybatis.spring.MyBatisSystemException: nested exception is org.apache.ibatis.binding.BindingException: Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
		//List<Seckill> queryAll(int offset, int limie);
		// java 没有保存形参的记录：  queryAll(int offset, int limie) -> queryAll(arg0, arg1)
		// 所以需要使用@Param去设定形参
		List<Seckill> seckills = seckillDao.queryAll(0, 100);
		for(Seckill sec : seckills){
			System.out.println(sec);
		}
		
	}

}
