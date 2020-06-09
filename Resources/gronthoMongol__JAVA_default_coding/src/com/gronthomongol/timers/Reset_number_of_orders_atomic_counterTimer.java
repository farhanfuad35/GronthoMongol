package com.gronthomongol.timers;

import android.util.Log;

import com.backendless.Counters;
import com.backendless.servercode.annotation.BackendlessTimer;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

/**
* Reset_number_of_orders_atomic_counterTimer is a timer.
* It is executed according to the schedule defined in Backendless Console. The
* class becomes a timer by extending the TimerExtender class. The information
* about the timer, its name, schedule, expiration date/time is configured in
* the special annotation - BackendlessTimer. The annotation contains a JSON
* object which describes all properties of the timer.
*/
@BackendlessTimer("{'startDate':1591722080033,'language':'JAVA','mode':'DRAFT','model':'default','frequency':{'schedule':'daily','repeat':{'every':1}},'timername':'reset_number_of_orders_atomic_counter'}")
public class Reset_number_of_orders_atomic_counterTimer extends com.backendless.servercode.extension.TimerExtender
{
    
  	@Override
  	public void execute() {
		// add your code here

		System.out.println("Running Timer");
		String counterName = "current_number_of_orders";
		Backendless.Counters.reset(counterName);
		System.out.println("Done");
	}
}
        