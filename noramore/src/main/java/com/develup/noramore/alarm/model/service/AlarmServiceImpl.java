package com.develup.noramore.alarm.model.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.develup.noramore.alarm.model.dao.AlarmDao;
import com.develup.noramore.alarm.model.vo.Alarm;

@Service
public class AlarmServiceImpl implements AlarmService{
	@Autowired
	private AlarmDao alarmDao;

	@Override
	public ArrayList<Alarm> selectAlarmList(Alarm alarm) {
		return alarmDao.selectAlarmList(alarm);
	}

	@Override
	public int insertAlarm(Alarm alarm) {
		return alarmDao.insertAlarm(alarm);
	}

	@Override
	public int updateAlarm(Alarm alarm) {
		return alarmDao.updateAlarm(alarm);
	}
}