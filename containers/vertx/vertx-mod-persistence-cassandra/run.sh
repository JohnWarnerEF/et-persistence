
# Use HK2 Verticle Factory
VERTICLE_FACTORY="-Dvertx.langs.java=com.englishtown~vertx-mod-hk2~1.5.0-final:com.englishtown.vertx.hk2.HK2VerticleFactory"

# Tell vert.x to use slf4j
LOG_DELEGATE_FACTORY="-Dorg.vertx.logger-delegate-factory-class-name=org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory"

# Export JAVA_OPTS for vertx
export JAVA_OPTS="${VERTICLE_FACTORY} ${LOG_DELEGATE_FACTORY}"

# Export VERTX_MODS to use target/mods
export VERTX_MODS=target/mods

vertx runmod com.englishtown~vertx-mod-persistence-cassandra~1.0.0-SNAPSHOT