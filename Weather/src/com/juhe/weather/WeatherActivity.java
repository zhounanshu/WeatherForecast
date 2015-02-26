package com.juhe.weather;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.juhe.weather.bean.FutureWeatherBean;
import com.juhe.weather.bean.HoursWeatherBean;
import com.juhe.weather.bean.PMBean;
import com.juhe.weather.bean.WeatherBean;
import com.juhe.weather.service.WeatherService;
import com.juhe.weather.service.WeatherService.OnPaserCallBack;
import com.juhe.weather.service.WeatherService.WeatherServiceBinder;
import com.juhe.weather.swiperefresh.PullToRefreshBase;
import com.juhe.weather.swiperefresh.PullToRefreshBase.OnRefreshListener;
import com.juhe.weather.swiperefresh.PullToRefreshScrollView;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.common.c;
import com.thinkland.juheapi.data.air.AirData;
import com.thinkland.juheapi.data.weather.WeatherData;

import android.R.integer;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract.Contacts.Data;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity {
	
	private Context mContext;
	private PullToRefreshScrollView mPullTORefreshScrollView;
	private ScrollView mScrollView;
	private WeatherService mService; 
	private TextView tv_city,tv_realse,tv_now_weather,tv_today_temp,
					 tv_now_temp,tv_aqi,tv_quality,tv_next_three,
					 tv_next_six,tv_next_nine,tv_next_twelve,tv_next_fifteen,
					 tv_next_three_temp,tv_next_six_temp,tv_next_nine_tmp,
					 tv_next_twelve_tmp,tv_next_fifteen_tmp,tv_today_temp_a,
					 tv_today_temp_b,tv_tommrow,tv_tommrow_temp_a,tv_tommrow_temp_b,
					 tv_third_day,tv_third_temp_a,tv_third_temp_b,tv_fourth_day,
					 tv_fourth_temp_a,tv_fourth_temp_b,tv_humidity,
					 tv_wind,tv_uv_index,tv_dressing_index;
	
	private ImageView iv_now_weather,iv_next_three,iv_next_six,iv_next_nine,iv_next_twelve,
					  iv_next_fifteen,iv_today_weather,iv_tommrow_weather,iv_third_weather,
					  iv_fourth_weather;
	
	private RelativeLayout rl_city;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		
		mContext = this;
		init();
		initService();
	}
	
	private void initService() {
		Intent intent = new Intent(mContext, WeatherService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
	}
	
	ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService.removeCallBack();
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			mService = ((WeatherServiceBinder)arg1).getService();
			mService.setCallBack(new OnPaserCallBack() {
				
				@Override
				public void OnPaserComplete(List<HoursWeatherBean> list, PMBean pmBean,
						WeatherBean weatherBean) {
					// TODO Auto-generated method stub
					mPullTORefreshScrollView.onRefreshComplete();
					if (list != null && list.size() >= 5) {
						setHoursView(list);
					}
					
					if (pmBean != null) {
						setPMView(pmBean);
					}
					
					if (weatherBean != null) {
						setWeatherView(weatherBean);
					}
				}
			});
			
			mService.getCityWeather();
		}
	};
	
	private void setPMView(PMBean bean) {
		
		tv_aqi.setText(bean.getAqi());
		tv_quality.setText(bean.getQuality());
		
	}
	
	private void setWeatherView(WeatherBean bean) {
		
		tv_city.setText(bean.getCity());
		tv_realse.setText(bean.getRelease());
		tv_now_weather.setText(bean.getWeather_str());
		String[] tempArr = bean.getTemp().split("~");
		String temp_str_h = tempArr[1].substring(0, tempArr[1].indexOf("¡æ"));
		String temp_str_l = tempArr[0].substring(0, tempArr[0].indexOf("¡æ"));
		//today temperature ¡ü¡ý ¡æ ¡ã
		tv_today_temp.setText("¡ü " + temp_str_h + "¡ã ¡ý" + temp_str_l + "¡ã");
				
		tv_today_temp_a.setText(temp_str_h + "¡ã");
		tv_today_temp_b.setText(temp_str_l + "¡ã");
		List<FutureWeatherBean> futureList = bean.getFuturelist();
		tv_tommrow.setText(futureList.get(0).getWeek());
		
		if (futureList .size() == 3) {
			setFutureData(tv_tommrow, iv_tommrow_weather, tv_tommrow_temp_a, 
					tv_tommrow_temp_b, futureList.get(0));
			setFutureData(tv_third_day, iv_third_weather, 
					tv_third_temp_a, tv_third_temp_b, futureList.get(1));
			setFutureData(tv_fourth_day, iv_fourth_weather, 
					tv_fourth_temp_a, tv_fourth_temp_b, futureList.get(2));
		}
		
		Calendar c = Calendar.getInstance();
		int time = c.get(Calendar.HOUR_OF_DAY);
		String prefixStr = null;
		
		if (time >= 6 && time < 18) {
			prefixStr = "d";
		}else {
			prefixStr = "n";
		}
		
		iv_now_weather.setImageResource(getResources().getIdentifier(
				"d" + bean.getWeather_id(), "drawable", "com.juhe.weather"));
		
		tv_humidity.setText(bean.getHumidity());
		tv_dressing_index.setText(bean.getDressing_index());
		tv_uv_index.setText(bean.getUv_index());
		tv_wind.setText(bean.getWind());

	}
	
	private void setHoursView(List<HoursWeatherBean> list) {
		
		setHoursData(tv_next_three, iv_next_three, tv_next_three_temp, list.get(0));
		setHoursData(tv_next_six, iv_next_six, tv_next_six_temp, list.get(1));
		setHoursData(tv_next_nine, iv_next_nine, tv_next_nine_tmp, list.get(2));
		setHoursData(tv_next_twelve, iv_next_twelve, tv_next_twelve_tmp, list.get(3));
		setHoursData(tv_next_fifteen, iv_next_fifteen, tv_next_fifteen, list.get(4));
		
	}
	
	private void setHoursData(TextView tv_hour, 
			ImageView iv_weather, 
			TextView tv_temp, 
			HoursWeatherBean bean) {
		
		String prefixStr = null;
		int time = Integer.valueOf(bean.getTime());
		if (time >= 6 && time < 18) {
			prefixStr = "d";
		}else {
			prefixStr = "n";
		}
		

		tv_hour.setText(bean.getTime());
		iv_weather.setImageResource(getResources().getIdentifier(
				prefixStr + bean.getWeather_id(), "drawable", "com.juhe.weather"));
		tv_temp.setText(bean.getTmp());
	}
	
	private void setFutureData(
			TextView tv_week, 
			ImageView iv_weather, 
			TextView tv_temp_a,
			TextView tv_temp_b,
			FutureWeatherBean bean) {
		
		tv_week.setText(bean.getWeek());
		iv_weather.setImageResource(getResources().getIdentifier(
				"d" + bean.getWeather_id(), "drawable", "com.juhe.weather"));
		
		String[] tempArr = bean.getTemp().split("~");
		String temp_str_a = tempArr[1].substring(0, tempArr[1].indexOf("¡æ"));
		String temp_str_b = tempArr[0].substring(0, tempArr[0].indexOf("¡æ"));
		
		tv_temp_a.setText(temp_str_a);
		tv_temp_b.setText(temp_str_b);
		
	}
	
	private void init() {
		mPullTORefreshScrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_scrollview);
		mPullTORefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				mService.getCityWeather();
			}
		});
		mScrollView = mPullTORefreshScrollView.getRefreshableView();
		
		rl_city = (RelativeLayout)findViewById(R.id.rl_city);
		rl_city.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				startActivityForResult(new Intent(mContext, CityActivity.class), 1);
			}
		});
		
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_realse = (TextView)findViewById(R.id.tv_release);
		tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
		tv_now_temp = (TextView) findViewById(R.id.tv_now_temp);
		tv_today_temp = (TextView) findViewById(R.id.tv_today_temp);
		tv_aqi = (TextView) findViewById(R.id.tv_aqi);
		tv_quality = (TextView) findViewById(R.id.tv_quality);
		tv_next_three = (TextView) findViewById(R.id.tv_next_three);
		tv_next_six = (TextView) findViewById(R.id.tv_next_six);
		tv_next_nine = (TextView) findViewById(R.id.tv_next_nine);
		tv_next_twelve = (TextView) findViewById(R.id.tv_next_twelve);
		tv_next_fifteen = (TextView) findViewById(R.id.tv_next_fifteen);
		tv_next_three_temp = (TextView) findViewById(R.id.tv_next_three_temp);
		tv_next_six_temp = (TextView) findViewById(R.id.tv_next_six_temp);
		tv_next_nine_tmp = (TextView) findViewById(R.id.tv_next_nine_tmp);
		tv_next_twelve_tmp = (TextView) findViewById(R.id.tv_next_twelve_tmp);
		tv_next_fifteen_tmp = (TextView) findViewById(R.id.tv_next_fifteen_tmp);
		tv_today_temp_a = (TextView) findViewById(R.id.tv_today_temp_a);
		tv_today_temp_b = (TextView) findViewById(R.id.tv_tody_temp_b);
		tv_tommrow = (TextView) findViewById(R.id.tv_tommrow);
		tv_tommrow_temp_a = (TextView) findViewById(R.id.tv_tommrow_temp_a);
		tv_tommrow_temp_b = (TextView) findViewById(R.id.tv_tommrow_temp_b);
		tv_third_day = (TextView) findViewById(R.id.tv_third_day);
		tv_third_temp_a = (TextView) findViewById(R.id.tv_third_temp_a);
		tv_third_temp_b = (TextView) findViewById(R.id.tv_third_temp_b);
		tv_fourth_day = (TextView) findViewById(R.id.tv_fourth_day);
		tv_fourth_temp_a = (TextView) findViewById(R.id.tv_fourth_temp_a);
		tv_fourth_temp_b = (TextView) findViewById(R.id.tv_fourth_temp_b);
		tv_humidity = (TextView) findViewById(R.id.tv_humidity);
		tv_uv_index = (TextView) findViewById(R.id.tv_uv_index);
		tv_wind = (TextView) findViewById(R.id.tv_wind);
		tv_dressing_index = (TextView) findViewById(R.id.tv_dressing_index);
		
		iv_now_weather = (ImageView)findViewById(R.id.iv_now_weather);
		iv_next_three = (ImageView)findViewById(R.id.iv_next_three);
		iv_next_six = (ImageView)findViewById(R.id.iv_next_six);
		iv_next_nine = (ImageView)findViewById(R.id.iv_next_nine);
		iv_next_twelve = (ImageView)findViewById(R.id.iv_next_twelve);
		iv_next_fifteen = (ImageView)findViewById(R.id.iv_next_fifteen);
		iv_today_weather = (ImageView)findViewById(R.id.iv_today_weather);
		iv_tommrow_weather = (ImageView)findViewById(R.id.iv_tommrow_weather);
		iv_third_weather = (ImageView)findViewById(R.id.iv_third_weather);
		iv_fourth_weather = (ImageView)findViewById(R.id.iv_fourth_weather);
		
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 1 && requestCode == 1) {
			String city = data.getStringExtra("city");
			mService.getCityWeather();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		unbindService(conn);
		super.onDestroy();
	}
	
	
}
