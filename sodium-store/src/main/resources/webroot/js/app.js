function init() {
    registerHandler();
}

let eventBus;

function registerHandler() {
    eventBus = new EventBus('/eventbus');
    eventBus.onopen = function () {
        eventBus.registerHandler('sodium.out/TEST1', function (error, message) {
            document.getElementById('current_value').innerHTML = message.body;
        });
    }
}

function request() {
    eventBus.send('sodium.in.request', 'TEST1', function (ar, ar_err) {
        if (ar_err == null) {
            document.getElementById('current_value').innerHTML = ar.body();
        } else {
            console.log(ar_err)
        }
    })
}
