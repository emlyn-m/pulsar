package xyz.emlyn.pulsar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class HomeViewModel() : ViewModel() {

    // ok to use !! here as Home.kt ensures an existing instance of AlertDB to be acquired
    private val _alertData = AlertDB.getInstance()!!.alertDao().getAllObservable()

    val alertLiveData: LiveData<List<Alert>> = _alertData

}

