var exec = require('cordova/exec');

    var proximity = function() {};

    proximity.prototype.getProximity = function(successCallback, failureCallback) {
        exec(successCallback, failureCallback, 'Proximity', 'getProximityValue', []);
    };
    
    proximity.prototype.enableSensor = function(successCallback, failureCallback) {
        exec(null, null, 'Proximity', 'start', []);
    };

    proximity.prototype.disableSensor = function(successCallback, failureCallback) {
        exec(null, null, 'Proximity', 'stop', []);
    };

    var proximity = new proximity();
    module.exports = proximity;