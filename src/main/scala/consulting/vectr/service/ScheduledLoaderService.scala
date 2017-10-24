package consulting.vectr.service

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.twitter.inject.Logging
import com.twitter.conversions.time._
import com.twitter.util.{ScheduledThreadPoolTimer, Time}

class ScheduledLoaderService @Inject()(dataService: DataService) extends Logging {
  private var running = false
  def start(): Unit = {
    if (running) return

    info("starting recurring task: \"getDataFromWebAndInsertInNeo4j\"")
    val scheduledThreadPoolTimer = new ScheduledThreadPoolTimer()
    scheduledThreadPoolTimer.schedule(when = Time.now, period = 24.hours) {
      info("running recurring task \"getDataFromWebAndInsertInNeo4j\"")
      dataService.getDataFromWebAndInsertInNeo4j()
    }
  }

}
