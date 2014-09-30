// var exec = require('cordova/exec');

//     var proximity = function() {};

//     proximity.prototype.getProximity = function(successCallback, failureCallback) {
//         exec(successCallback, failureCallback, 'Proximity', 'getProximityValue', []);
//     };
    
//     proximity.prototype.enableSensor = function(successCallback, failureCallback) {
//         exec(null, null, 'Proximity', 'start', []);
//     };

//     proximity.prototype.disableSensor = function(successCallback, failureCallback) {
//         exec(null, null, 'Proximity', 'stop', []);
//     };

//     var proximity = new proximity();
//     module.exports = proximity;


var argscheck = require('cordova/argscheck'),
    exec = require("cordova/exec");

var proximity = {
    /**
     *  Get the current proximity sensor state.
     *  @param successCallback  callback function which delivers the boolean sensor state
     */
    getProximityValue: function(successCallback,failureCallback) {
        argscheck.checkArgs('F', 'proximity.getProximityValue', arguments);
        exec(successCallback, failureCallback, "Proximity", "getProximityValue", []);
    },

    /**
     *  Enable the proximity sensor. Needs to be called before getting the proximity state.
     */
    enableSensor: function() {
        exec(null, null, "Proximity", "start", []);
    },

    /**
     *  Disable the proximity sensor.
     */
    disableSensor: function() {
        exec(null, null, "Proximity", "stop", []);
    }
};
module.exports = proximity;