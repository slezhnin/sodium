BASEDIR=$(dirname "$0")
RUN_PATH=$BASEDIR/../../../build/install/sodium-store-shadow/bin/sodium-store

sh $RUN_PATH -conf $BASEDIR/sodium-store.json
