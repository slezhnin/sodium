function init() {
    eventBus = new EventBus('/eventbus');
}

var eventBus;
var consumer;

function sendMessage(key) {
    if (event.type === 'keydown' && event.key !== 'Enter') {
        return
    }
    console.log('input key', key.value);
    if (consumer !== undefined && consumer != null) {
        eventBus.unregisterHandler('sodium.out/' + key.value, consumer);
    }
    eventBus.send('sodium.in.request', key.value, function (error, message) {
        if (error == null) {
            console.log('reply message', message);
            document.getElementById('current_value').innerHTML = message.body;
            document.getElementById('error_value').innerHTML = '';
        } else {
            console.log('reply error', error);
            document.getElementById('current_value').innerHTML = '';
            document.getElementById('error_value').innerHTML = error.message;
        }
    });
    consumer = function (message) {
        console.log("consume message", message);
        document.getElementById('current_value').innerHTML = message.body;
    };
    eventBus.registerHandler('sodium.out/' + key.value, consumer)
}

function request() {
    sendMessage(document.getElementById('key_input'));
}
