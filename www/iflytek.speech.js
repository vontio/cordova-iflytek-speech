var cordova = require('cordova');
var channel = require('cordova/channel');
var exec = require('cordova/exec');

var iflytekSpeech = function () {
    this.channels = {
        'SpeechResults': channel.create('SpeechResults'),
        'SpeechError': channel.create('SpeechError'),
        'VolumeChanged': channel.create('VolumeChanged'),
        'SpeechBegin': channel.create('SpeechBegin'),
        'SpeechEnd': channel.create('SpeechEnd'),
        'SpeechCancel': channel.create('SpeechCancel'),
        'SpeakCompleted': channel.create('SpeakCompleted'),
        'SpeakBegin': channel.create('SpeakBegin'),
        'SpeakProgress': channel.create('SpeakProgress'),
        'SpeakPaused': channel.create('SpeakPaused'),
        'SpeakResumed': channel.create('SpeakResumed'),
        'SpeakCancel': channel.create('SpeakCancel'),
        'BufferProgress': channel.create('BufferProgress')
    };
    this.text = '';
};

iflytekSpeech.prototype = {
    _eventHandler: function (info) {
        if (info.event && this.channels[info.event]) {
            this.channels[info.event].fire(info);
        }
    },

    addEventListener: function (event, f, c) {
        if (event && this.channels[event]) {
            this.channels[event].subscribe(f, c || this);
        }
    },

    removeEventListener: function (event, f) {
        if (event && this.channels[event]) {
            this.channels[event].unsubscribe(f);
        }
    },

    initialize: function (appId) {
        // closure variable for local function to use
        var speech = this;

        // the callback will be saved in the session for later use
        var callback = function (info) {
            speech._eventHandler(info);
        };
        exec(callback, callback, 'Speech', 'initialize', [appId]);

        speech.addEventListener('SpeakBegin',function(){
            console.log('SpeakBegin');
        });

        speech.addEventListener('SpeakCompleted',function(obj){
            if (typeof speech.onSpeakCallback === 'function') {
                if (obj.code)
                    speech.onSpeakCallback(obj);
                else
                    speech.onSpeakCallback(null);
            }
        });

        speech.addEventListener('SpeechBegin',function(){
            console.log("SpeechBegin");
        });

        speech.addEventListener('SpeechError',function(obj){
            if (typeof speech.onListenCallback === 'function') {
                speech.onListenCallback(obj);
            }
        });

        speech.addEventListener('SpeechResults', function(obj){
			var data = JSON.parse(obj.results);
            if (data.sn === 1)
                speech.text = '';

            var ws = data.ws;
            for (var i = 0; i < ws.length; i++) {
                var word = ws[i].cw[0].w;
                speech.text += word;
            }

            if (data.ls === true) {
                if (typeof speech.onListenCallback === 'function') {
                    speech.onListenCallback(null,speech.text);
                }
            }
		});
    },

    startListening: function (options, callback) {
        this.onListenCallback = callback;
        exec(null, null, 'Speech', 'startListening', [options]);
    },

    stopListening: function () {
        exec(null, null, 'Speech', 'stopListening', []);
    },

    cancelListening: function () {
        exec(null, null, 'Speech', 'cancelListening', []);
    },

    startSpeaking: function (text, options, callback) {
        this.onSpeakCallback = callback;
        exec(null, null, 'Speech', 'startSpeaking', [text, options]);
    },

    pauseSpeaking: function () {
        exec(null, null, 'Speech', 'pauseSpeaking', []);
    },

    resumeSpeaking: function () {
        exec(null, null, 'Speech', 'resumeSpeaking', []);
    },

    stopSpeaking: function () {
        exec(null, null, 'Speech', 'stopSpeaking', []);
    },
};

module.exports = new iflytekSpeech();