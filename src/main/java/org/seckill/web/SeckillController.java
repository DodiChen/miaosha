package org.seckill.web;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller //@Service, @Component
@RequestMapping("/seckill")
public class SeckillController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	
	@RequestMapping(name="/list", method=RequestMethod.GET)
	public String list(Model model) {
		// list.jsp + model = ModelAndView
		List<Seckill> list = seckillService.getSeckillList();
		model.addAttribute("list", list);
		return "list"; // /WEB-INF/jsp/list.jsp
	}
	
	@RequestMapping(name="/{seckillId}/detail", method=RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
		
		if(seckillId == null) {
			return "redirect: /seckill/list"; //如果seckillId不存在，这重定向到上面的list接口，返回列表页
		}
		Seckill seckill = seckillService.getById(seckillId);
		if(seckill == null) {
			return "forward: /seckill/list";
		}
		model.addAttribute("seckill", seckill);
		return "detail";
	}
	
	//ajax, 返回json
	@RequestMapping(value="/{seckillId}/exposer", 
			method=RequestMethod.POST,
			produces = "{application/json;charset=UTF-8}")
	@ResponseBody //  返回类型是json
	public SeckillResult<Exposer> exposer(Long seckillId) {
		SeckillResult<Exposer> result;
		try {
			Exposer exposer = seckillService.exportSeckillUrl(seckillId);
			result = new SeckillResult<Exposer>(true, exposer);
		}catch(Exception e) {
			result = new SeckillResult<Exposer>(false, e.getMessage());
		}
		return result;
	}
	
	
}
