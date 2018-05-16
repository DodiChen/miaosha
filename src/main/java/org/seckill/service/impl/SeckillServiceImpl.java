package org.seckill.service.impl;

import java.util.Date;
import java.util.List;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import ch.qos.logback.classic.Logger;

//@Component(包含service，dao和controller) @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService {
	
	private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	//注入service以来
	@Autowired
	private SeckillDao seckillDao;
	
	@Autowired
	private SuccessKilledDao successKilledDao;
	
	@Autowired
	private RedisDao redisDao;
	
	// 加入一个混淆的概念
	// 不希望用户猜到我们的结果
	// md5混淆字符串，用于混淆md5
	private final String slat = "dsdjfhy89752480hsdiaf9oab@@@hfdsf7987804236";
	
	public List<Seckill> getSeckillList() {
		// TODO Auto-generated method stub
		return seckillDao.queryAll(0, 4);
	}

	public Seckill getById(long seckillId) {
		// TODO Auto-generated method stub
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {
		// 优化点: 缓存优化, 一致性维护在 超时的基础上
		/**
		 * get from cache
		 * if null
		 * 	get db
		 * else
		 * 	put cache
		 */
		// 1: 访问reids
		Seckill seckill = redisDao.getSeckill(seckillId);
		if (seckill == null){
			// 2:访问数据库
			seckill = seckillDao.queryById(seckillId);
			if(seckill == null){
				return new Exposer(false, seckillId);
			}else{
				// 3: 放入redis
				redisDao.putSeckill(seckill);
			}
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		Date nowTime = new Date();
		if(nowTime.getTime() < startTime.getTime() ||
				nowTime.getTime() > endTime.getTime()){
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		// 转化特定字符串的过程， md5 不可逆
		String md5 = getMD5(seckillId); 
		return new Exposer(true, md5, seckillId);
	}
	
	private String getMD5(long seckillId){
		String base = seckillId + "/" + slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}
	
	/**
	 * 事务在遇到runtime异常是才会rollback
	 * 
	 * 使用注解控制事务方法的优点
	 * 1： 开发团队达成一致约定，明确标注事务方法的编程风格
	 * 2：保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP或者剥离到事务方法外部（再做一个更上层的算法）
	 * 3：不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
	 */
	@Transactional
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		// TODO Auto-generated method stub
		if(md5 == null || !md5.equals(getMD5(seckillId))){
			throw new SeckillException("seckill data rewrite");
		}
		// 执行秒杀逻辑：减库存 + 记录购买行为
		Date nowTime = new Date();
		try{
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if(updateCount <= 0){
				// 没有更新到记录，秒杀成功
				throw new SeckillCloseException("seckill is closed");
			}else{
				// 减库存成功，记录购买行为
				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
				if(insertCount <= 0){
					// 重复秒杀
					throw new RepeatKillException("seckill repeated");
				}else{
					// 秒杀成功
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
				}
			}
		}catch(SeckillCloseException e){
			throw e;
		}catch(RepeatKillException e){
			throw e;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			// 所有编译器异常，转化为运行时异常
			throw new SeckillException("seckill inner error: " + e.getMessage());
		}
	}
}
