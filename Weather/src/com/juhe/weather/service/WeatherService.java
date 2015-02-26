package com.juhe.weather.service;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.juhe.weather.bean.FutureWeatherBean;
import com.juhe.weather.bean.HoursWeatherBean;
import com.juhe.weather.bean.PMBean;
import com.juhe.weather.bean.WeatherBean;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.air.AirData;
import com.thinkland.juheapi.data.weather.WeatherData;

import android.R.integer;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WeatherService extends Service{
	
	private String city;
	private final String tag = "WeatherService";
	private WeatherService binder = new WeatherService();
	private boolean isRunning = false;
	private int count = 0;
	private FutureWeatherBean futureBean;
	private HoursWeatherBean hoursBean;
	private List<HoursWeatherBean> list;
	private PMBean pmBean;
	private WeatherBean weatherBean;
	private OnPaserCallBack callBack;
	
	private static final int REPEAT_MSG = 0x01;
	
	public interface OnPaserCallBack{
		public void OnPaserComplete(List<HoursWeatherBean> list, 
				PMBean pmBean, 
				WeatherBean weatherBean);
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		city = "±±¾©";
		mHandler.sendEmptyMessage(REPEAT_MSG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(tag, "OnStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == REPEAT_MSG) {
				
				getCityWeather();
				//send delay msg
				sendEmptyMessageDelayed(REPEAT_MSG, 30*60*1000);
			
				
			}
		}
			
	};
	
	public void setCallBack(OnPaserCallBack callBack) {
		this.callBack = callBack;
	}
	
	
	public void removeCallBack() {
		callBack = null;
	}
	
	public void getCityWeather(String city) {
		this.city = city;
		getCityWeather();

	}
	
	public void getCityWeather() {
		
		if (isRunning) {
			return;
		}
		
		isRunning = true;
		count = 0;
		
		WeatherData data = WeatherData.getInstance();
		
		data.getByCitys(city, 2, new JsonCallBack(){
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				synchronized (this) {
					count++;
				}

				weatherBean = parserWeather(arg0);
				System.out.print(arg0);
				
				if (weatherBean != null) {
//					setWeatherView(bean);
				}
				
				if (count == 3) {
//					mPullTORefreshScrollView.onRefreshComplete();
					if (callBack != null) {
						callBack.OnPaserComplete(list, pmBean, weatherBean);
					}
					
					isRunning = false;
				}
			}
		});
		
		data.getForecast3h(city, new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				synchronized (this) {
					count++;
				}
				
				list = paserForecast3h(arg0);
				if (list != null && list.size() >= 5) {
//					setHoursView(list);
				}
				
				if (count == 3) {
//					mPullTORefreshScrollView.onRefreshComplete();
					if (callBack != null) {
						callBack.OnPaserComplete(list, pmBean, weatherBean);
					}
					isRunning = false;
				}
				
			}
		} );
		
		AirData airData = AirData.getInstance();
		airData.cityAir(city, new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				synchronized (this) {
					count++;
				}
				pmBean = paserPM(arg0);
				if (pmBean != null) {
//					setPMView(bean);
				}
				
				if (count == 3) {
//					mPullTORefreshScrollView.onRefreshComplete();
					if (callBack != null) {
						callBack.OnPaserComplete(list, pmBean, weatherBean);
					}
					isRunning = false;
				}
			}
		});
		
	}
	
	//paser for PM
	
	private PMBean paserPM(JSONObject json) {
		PMBean bean = null;
		
		try {
			
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			
			if(code == 200 && error_code == 0){ 
			JSONObject pmJson = json.getJSONArray("result").getJSONObject(1).getJSONObject(
					"citynow");
			bean = new PMBean();
			bean.setAqi(pmJson.getString("AQI"));
			bean.setQuality(pmJson.getString("quality"));
			
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return bean;
	}
	
	//
	private List<HoursWeatherBean> paserForecast3h(JSONObject json) {
		
		List<HoursWeatherBean> list = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		Date date = new Date(System.currentTimeMillis());
		try {
			
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			
			if(code == 200 && error_code == 0){
				list = new ArrayList<HoursWeatherBean>();
				JSONArray resultArray = json.getJSONArray("result");
				
				for(int i = 0;i < resultArray.length(); i ++){
					JSONObject hourJson = resultArray.getJSONObject(i);
					java.util.Date hDate = sdf.parse(hourJson.getString("sfdate"));
					
					if (!hDate.after(date)) {
						continue;
					}
					
					HoursWeatherBean bean = new HoursWeatherBean();
					bean.setWeather_id(hourJson.getString("weatherid"));
					bean.setTmp(hourJson.getString("temp1"));
					
					Calendar c = Calendar.getInstance();
					c.setTime(hDate);
					bean.setTime(c.get(Calendar.HOUR_OF_DAY) + "");
					
					if(list.size() == 5){
						break;
					}
				}
			}else {
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return list;
	}
	
	//paser weather for city
	private WeatherBean parserWeather(JSONObject json) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		WeatherBean bean = null;
		
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			
			if(code == 200 && error_code == 0){
				JSONObject resultJson = json.getJSONObject("result");
				
				bean = new WeatherBean();
				
				//today
				JSONObject todayJson = resultJson.getJSONObject("today");
				bean.setCity(todayJson.getString("city"));
				bean.setUv_index(todayJson.getString("uv_index"));
				bean.setTemp(todayJson.getString("temperature"));
				bean.setWeather_str(todayJson.getString("weather"));
				bean.setWeather_id(todayJson.getJSONObject("weather_id").getString("fa"));
				bean.setDressing_index(todayJson.getString("dressing_index"));
				
				//SK
				JSONObject skJson = resultJson.getJSONObject("sk");
				bean.setWind(skJson.getString("wind_direction") + 
						skJson.getString("wind_strength"));
				bean.setNow_temp(skJson.getString("temp"));
				bean.setRelease(skJson.getString("time"));
				bean.setHumidity(skJson.getString("humidity"));
				
				//future
				JSONArray futureArray = resultJson.getJSONArray("future");
				
				Date date = new Date(System.currentTimeMillis());
				List<FutureWeatherBean> futureList = new ArrayList<FutureWeatherBean>();
				for(int i = 0; i < futureArray.length(); i ++){
					JSONObject futureJson = futureArray.getJSONObject(i);
					FutureWeatherBean futureBean = new FutureWeatherBean();
					
					java.util.Date datef = null;
					try {
						datef = sdf.parse(futureJson.getString("date"));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (!datef.after(date)) {
						continue;
					}
					
					futureBean.setTemp(futureJson.getString("temperature"));
					futureBean.setWeek(futureJson.getString("week"));
					futureBean.setWeather_id(futureJson.getJSONObject(
							"weather_id").getString("fa"));
					futureList.add(futureBean);
					
					if (futureList.size() == 3) {
						break;
					}
					
				}				
				bean.setFuturelist(futureList);				
			}else {
				Toast.makeText(getApplicationContext(), "WHERE_ERROR", Toast.LENGTH_SHORT).show();
			}
			
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return bean;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(tag, "onDestroy");
	}
	
	public class WeatherServiceBinder extends Binder{
		public WeatherService getService() {
			return WeatherService.this;
		}
	}

}
