import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.ExperimentalHoplite

data class Config(val application: Application)
data class Application(val workers: Map<String, Worker>)
data class Worker(val batchSize: Int)

@OptIn(ExperimentalHoplite::class)
fun main(args: Array<String>) {
    if (args.contains("--process-env")) {
        val config = ConfigLoaderBuilder.default()
            .withExplicitSealedTypes()
            .build()
            .loadConfigOrThrow<Config>(listOf("application.yaml"))
        println(config.application.workers)
        check(config.application.workers.keys == setOf("alphaWorker", "betaWorker")) {
            "Unexpected map keys: ${config.application.workers.keys}"
        }
        return
    }

    val cases = listOf(
        "YAML only" to emptyMap(),
        "Override alphaWorker only" to mapOf(
            "APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234",
        ),
        "Override betaWorker only" to mapOf(
            "APPLICATION_WORKERS_betaWorker_BATCHSIZE" to "5678",
        ),
        "Override both workers" to mapOf(
            "APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234",
            "APPLICATION_WORKERS_betaWorker_BATCHSIZE" to "5678",
        ),
    )
    var failures = 0
    for ((label, environment) in cases) {
        val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
            .withExplicitSealedTypes()
            .addSource(EnvironmentVariablesPropertySource(environmentVariableMap = { environment }))
            .build()
            .loadConfigOrThrow<Config>(listOf("application.yaml"))
        val actual = config.application.workers
        val expected = mapOf(
            "alphaWorker" to Worker(if (environment.containsKey("APPLICATION_WORKERS_alphaWorker_BATCHSIZE")) 1234 else 10),
            "betaWorker" to Worker(if (environment.containsKey("APPLICATION_WORKERS_betaWorker_BATCHSIZE")) 5678 else 20),
        )
        println("$label: $actual")
        if (actual != expected) {
            failures++
            println("  EXPECTED: $expected")
        }
    }
    check(failures == 0) { "$failures case(s) changed map keys or values unexpectedly" }
}
