# Hoplite Map-Key Reproducer

Generic standalone illustration of a Hoplite 3.0.0 YAML/environment map-merge defect. No application code or services are required.

Requirements: JDK 17 or newer, Maven, and access to Maven Central for the first build. Dependencies are pinned in `pom.xml`.

Run from this directory:

```sh
mvn -B -ntp compile exec:java
```

The program injects environment-variable maps into Hoplite's real `EnvironmentVariablesPropertySource`, rather than modifying the process environment. It checks both keys and values in four scenarios. With 3.0.0, YAML-only and both-entry override cases pass; each single-entry override changes the untouched sibling's key to its dotted configuration path. The program prints each result and intentionally exits with an assertion failure when the defect is present.

To use the default loader and a real environment variable:

```sh
env APPLICATION_WORKERS_alphaWorker_BATCHSIZE=1234 \
  mvn -B -ntp compile exec:java -Dexec.args=--process-env
```

Actual map:

```text
{alphaWorker=Worker(batchSize=1234), application.workers.betaWorker=Worker(batchSize=20)}
```

Expected map:

```text
{alphaWorker=Worker(batchSize=1234), betaWorker=Worker(batchSize=20)}
```

The `--process-env` mode checks map keys; the default deterministic mode checks both keys and values. Avoid unrelated `APPLICATION_*` overrides in process-environment mode.

Both modes were compiled and executed on Hoplite 3.0.0 with Kotlin 2.2.21, Maven 3.9.16, and Amazon Corretto 17.0.20.1 on Linux aarch64. This is not a cross-version test; the environment-source API differs in 2.9.0.
