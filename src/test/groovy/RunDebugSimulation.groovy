import io.gatling.app.Gatling
import io.gatling.core.config.GatlingConfiguration
import simulations.ComputerDatabaseSimulation
import java.nio.file.Paths

class RunDebugSimulation {

    //to run/debug a single simulation locally, uses vm option --add-opens java.base/java.lang=ALL-UNNAMED
    static void main(String[] args) {
        //load configuration
        GatlingConfiguration.load()

        //change logger level for io.gatling.http.engine.response in logback.xml, as needed
        //specify desired gatling simulation
        def simulationClassName = ComputerDatabaseSimulation.class.getName()
        Gatling.main(
                new String[] {
                        "-s", simulationClassName,
                        "-rf", Paths.get("target", "gatling").toString()
                }
        )
    }
}