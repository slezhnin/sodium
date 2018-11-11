BASEDIR=$(dirname "$0")
BUILD_PATH=$BASEDIR/../../../build
JAR_NAME=sodium-store-1.0-SNAPSHOT-all.jar
JVM_ARGS=-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory

java $JVM_ARGS -jar $BUILD_PATH/libs/$JAR_NAME -conf $BASEDIR/sodium-store.json
