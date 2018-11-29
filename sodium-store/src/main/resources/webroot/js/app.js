function init() {
    registerHandler();
}

let eventBus;

function registerHandler() {
    eventBus = new EventBus('/eventbus');
    eventBus.onopen = function () {
        eventBus.registerHandler('out', function (error, message) {
            document.getElementById('current_value').innerHTML = message.body;
        });
    }
}

function increment() {
    eventBus.send('in')
}
