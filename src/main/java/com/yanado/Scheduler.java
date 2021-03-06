package com.yanado;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yanado.dao.AlarmDAO;
import com.yanado.dao.ProductDAO;
import com.yanado.dto.Alarm;
import com.yanado.dto.Auc;
import com.yanado.dto.Common;
import com.yanado.dto.CommonJoin;
import com.yanado.dto.Product;
import com.yanado.service.AucService;
import com.yanado.service.CommonService;

@Component
public class Scheduler {

	@Autowired
	private CommonService service;
	
	@Autowired
	private ProductDAO productDao;

	@Autowired
	private AlarmDAO alarmDao;
	
	@Autowired
	private AucService aucService;

	@Scheduled(cron = "0 1 0 * * *")
	public void changeCommonStatus() {
		List<Common> list = service.findAll();
		Date today = new Date(System.currentTimeMillis());
		Date yesterday = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * -1));

		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");

		String to = transFormat.format(today);
		String yes = transFormat.format(yesterday);

		for (Common c : list) {

			Product p = productDao.getProductByProductId(c.getProductId());

			String start = transFormat.format(c.getStartDate());
			String end = transFormat.format(c.getEndDate());
			String dead = transFormat.format(c.getDeadline());

			if (start.equals(to)) {
				service.changeStatus(c.getCommonId(), 2);
			}

			if (end.equals(yes)) {
				
				if (c.getParticipants() >= c.getMin()) {
					service.changeStatus(c.getCommonId(), 5);

					// alarm
					Alarm alarm = new Alarm(null, c.getCommonId(), null, 1, p.getPrice(),
							"최소 인원이 넘어 공동구매가 성사되었습니다.\n 해당 날짜 이전까지 결제해주시길 바랍니다.", today, c.getDeadline());
					
					alarmDao.insertAlarm(alarm);
					
					List<CommonJoin> commonJoin = service.findAllCommonJoinByCommonId(c.getCommonId());
					for (CommonJoin j : commonJoin) {
						alarm = new Alarm(j.getUserId(), c.getCommonId(), null, 1, p.getPrice(),
								"최소 인원이 넘어 공동구매가 성사되었습니다.\n 해당 날짜 이전까지 결제해주시길 바랍니다.", today, c.getDeadline());
						alarmDao.insertAlarm(alarm);
					}

				} else {

					service.changeStatus(c.getCommonId(), 4);
					
					Alarm alarm = new Alarm(null, c.getCommonId(), null, 1, p.getPrice(),
							"최소 인원을 넘지 못하여 공동구매가 성사되지 못하였습니다.", today, c.getDeadline());
					
					alarmDao.insertAlarm(alarm);

					// alarm
					List<CommonJoin> commonJoin = service.findAllCommonJoinByCommonId(c.getCommonId());
					for (CommonJoin j : commonJoin) {
						alarm = new Alarm(j.getUserId(), c.getCommonId(), null, 1, p.getPrice(),
								"최소 인원을 넘지 못하여 공동구매가 성사되지 못하였습니다.", today, c.getDeadline());
						alarmDao.insertAlarm(alarm);
					}
				}
				
			}

			if (dead.equals(yes) && c.getStatus() == 5) {
				service.changeStatus(c.getCommonId(), 6);

				List<CommonJoin> notpayList = service.notPayment(c.getCommonId());
				
				Alarm alarm = new Alarm(null, c.getCommonId(), null, 1, p.getPrice(),
						"결제를 진행하지 않아 공동구매에서 제외되셨습니다.", today, c.getDeadline());
				alarmDao.insertAlarm(alarm);
				
				for (CommonJoin j : notpayList) {
					alarm = new Alarm(j.getUserId(), c.getCommonId(), null, 1, p.getPrice(),
							"결제를 진행하지 않아 공동구매에서 제외되셨습니다.", today, c.getDeadline());
					alarmDao.insertAlarm(alarm);
				}
			}
		}
	}
	
	@Scheduled(cron = "0 1 0 * * *")
	public void changeAucStatus() {
		List<Auc> list = aucService.getAllAucList();
		
		Date today = new Date(System.currentTimeMillis());
		Date yesterday = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * -1));

		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");

		String to = transFormat.format(today);
		String yes = transFormat.format(yesterday);

		for (Auc a : list) {

			String start = transFormat.format(a.getStartDate());
			String end = transFormat.format(a.getEndDate());
			
			if (start.equals(to)) {
				Auc auc = aucService.getAuc(a.getAucId());
				auc.setStatus(2);
				aucService.updateAuc(auc);
			}

			if (end.equals(yes)) {
				Auc auc = aucService.getAuc(a.getAucId());
				auc.setStatus(3);
				aucService.updateAuc(auc);
				
				Alarm alarm = new Alarm(null, a.getAucId(), null, 1, a.getHighestPrice(),
						"경매가 종료되었습니다.", today, null);
				alarmDao.insertAlarm(alarm);
				
				alarm = new Alarm(a.getHighestUserId(), a.getAucId(), null, 1, a.getHighestPrice(),
						"경매에 낙찰되었습니다.", today, null);
				alarmDao.insertAlarm(alarm);
			}
		}
	}
}
