package org.seckill.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
	"classpath:spring/spring-dao.xml",
	"classpath:spring/spring-service.xml"
})
public class SeckillServiceTest{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	@Test
	public void testGetSeckillList() throws Exception{
		
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list);
	}
	
	@Test
	public void testGetById() throws Exception{
		Seckill seckill = seckillService.getById(1000l);
		logger.info("seckill={}", seckill);
	}
	
	// 集成测试代码完整逻辑，注意可重复执行
	@Test
	public void testExportSeckillUrl() throws Exception{
		long id = 1000;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if(exposer.isExposed()){
			logger.info("exposer={}", exposer);
			long phone = 13502171118L;
			String md5 = "d313e424b3d8b2afa2921355a999b76a";
			try {
				SeckillExecution execute = seckillService.executeSeckill(id, phone, md5);
				logger.info("result={}", execute);
			} catch (RepeatKillException e) {
				// TODO: handle exception
				logger.error(e.getMessage());
			} catch (SeckillCloseException e) {
				// TODO: handle exception
				logger.error(e.getMessage());
			}
		}else{
			// 秒杀未开启
			logger.info("exposer={}", exposer);
		}
		
		logger.info("==================");
		logger.info(exposer.toString());
		/**
		 * exposed=true, md5=d313e424b3d8b2afa2921355a999b76a, seckillId=1000, now=0, start=0, end=0
		 */
	}
	
	//@Test
	public void testExecuteSeckill() throws Exception{
		long id = 1000;
		long phone = 13502171118L;
		String md5 = "d313e424b3d8b2afa2921355a999b76a";
		try {
			SeckillExecution execute = seckillService.executeSeckill(id, phone, md5);
			logger.info("result={}", execute);
		} catch (RepeatKillException e) {
			// TODO: handle exception
			logger.error(e.getMessage());
		} catch (SeckillCloseException e) {
			// TODO: handle exception
			logger.error(e.getMessage());
		}	
	}
	
	@Test
	public void executeSeckillProcedure(){
		long seckillId = 1000;
		long phone = 15784515479L;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if(exposer.isExposed()){
			String md5 = exposer.getMd5();
			SeckillExecution execution = seckillService.executeSeckillByProcedure(seckillId, phone, md5);
			logger.info(execution.getStateInfo());
		}
		
		
	}
}
