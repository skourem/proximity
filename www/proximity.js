var exec = require('cordova/exec');

    var proximity = function() {};

    proximity.prototype.getProximity = function(successCallback, failureCallback) {
        exec(successCallback, failureCallback, 'Proximity', 'getProximityFlag', []);
    };
    
    var proximity = new proximity();
    module.exports = proximity;