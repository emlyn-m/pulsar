package xyz.emlyn.pulsar

import java.util.concurrent.atomic.AtomicInteger

// todo: note that if app restarts, this will be reset to 0, potentially overwriting
//      existing notifications - maybe store value in prefs datastore??
class NotificationIDManager {
    companion object {
        private var cID = AtomicInteger(0);

        fun getNewID() : Int {
            return cID.incrementAndGet()
        }


    }
}