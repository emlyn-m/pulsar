package xyz.emlyn.pulsar

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.distinctUntilChanged


class HomeViewModel() : ViewModel() {


    // ok to use !! here as Home.kt ensures an existing instance of AlertDB to be acquired

    var alertLiveData = AlertDB.getInstance()!!.alertDao().getAllObservable()

}

