package com.iflytek.cordova.speech;

import android.os.Bundle;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class echoes a string called from JavaScript.
 */
public class Speech extends CordovaPlugin implements RecognizerListener, SynthesizerListener {
    public static final String TAG = "Speech";

    public static final String STR_EVENT = "event";
    public static final String STR_CODE = "code";
    public static final String STR_MESSAGE = "message";
    public static final String STR_VOLUME = "volume";
    public static final String STR_RESULTS = "results";
    public static final String STR_PROGRESS = "progress";

    public static final String EVENT_SPEECH_RESULTS = "SpeechResults";

    public static final String EVENT_SPEECH_ERROR = "SpeechError";
    public static final String EVENT_VOLUME_CHANGED = "VolumeChanged";
    public static final String EVENT_SPEECH_BEGIN = "SpeechBegin";
    public static final String EVENT_SPEECH_END = "SpeechEnd";
    public static final String EVENT_SPEECH_CANCEL = "SpeechCancel";

    public static final String EVENT_SPEAK_COMPLETED = "SpeakCompleted";
    public static final String EVENT_SPEAK_BEGIN = "SpeakBegin";
    public static final String EVENT_SPEAK_PAUSED = "SpeakPaused";
    public static final String EVENT_SPEAK_RESUMED = "SpeakResumed";
    public static final String EVENT_SPEAK_CANCEL = "SpeakCancel";
    public static final String EVENT_SPEAK_PROGRESS = "SpeakProgress";
    public static final String EVENT_BUFFER_PROGRESS = "BufferProgress";
	
    public static final String ASR_FILE = "./sdcard/iflytek.asr.wav";
    public static final String TTS_FILE = "./sdcard/iflytek.tts.wav";

    private CallbackContext callback;
    private SpeechRecognizer recognizer;
    private SpeechSynthesizer synthesizer;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize")) {
            try{
                ApplicationInfo appInfo = cordova.getActivity().getPackageManager()  
                    .getApplicationInfo(cordova.getActivity().getPackageName(),  
                            PackageManager.GET_META_DATA);  
                String appId = appInfo.metaData.getString("iflytek.speech.AppId");
                this.initialize(appId, callbackContext);
            }catch(NameNotFoundException e)
            {
                e.printStackTrace();
            }
        } else if (action.equals("startListening")) {
            JSONObject options = args.optJSONObject(0);
            this.startListening(options, callbackContext);

        } else if (action.equals("stopListening")) {
            this.stopListening(callbackContext);

        } else if (action.equals("cancelListening")) {
            this.cancelListening(callbackContext);

        } else if (action.equals("startSpeaking")) {
            String text = args.getString(0);
            JSONObject options = args.optJSONObject(1);
            this.startSpeaking(text, options, callbackContext);

        } else if (action.equals("pauseSpeaking")) {
            this.pauseSpeaking(callbackContext);

        } else if (action.equals("resumeSpeaking")) {
            this.resumeSpeaking(callbackContext);

        } else if (action.equals("stopSpeaking")) {
            this.stopSpeaking(callbackContext);

        } else { // Unrecognized action.
            Log.v(TAG,"Unrecognized action.");
            return false;
        }

        return true;
    }

    private SpeechRecognizer getRecognizer() {
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createRecognizer(this.cordova.getActivity(), null);
        }
        return recognizer;
    }

    private SpeechSynthesizer getSynthesizer() {
        if (synthesizer == null) {
            synthesizer = SpeechSynthesizer.createSynthesizer(this.cordova.getActivity(), null);
        }
        return synthesizer;
    }

    private void initialize(String appId, CallbackContext callbackContext) {
        this.callback = callbackContext;
        SpeechUtility.createUtility(cordova.getActivity(), SpeechConstant.APPID + "=" + appId + "," + SpeechConstant.FORCE_LOGIN + "=true");
    }

    private void startListening(JSONObject options, CallbackContext callbackContext) {
        Log.v(TAG,"startListening " + String.valueOf(options));
        SpeechRecognizer rec = getRecognizer();

        rec.setParameter(SpeechConstant.DOMAIN, "iat");
        rec.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        rec.setParameter(SpeechConstant.ACCENT, "mandarin");
        rec.setParameter(SpeechConstant.ASR_AUDIO_PATH, ASR_FILE);

        if (options != null) {
            Iterator it = options.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                String value = options.optString(key);
                rec.setParameter(key, value);
            }
        }

        rec.startListening(this);
    }

    private void stopListening(CallbackContext callbackContext) {
        getRecognizer().stopListening();
    }

    private void cancelListening(CallbackContext callbackContext) {
        getRecognizer().cancel();
    }

    private void startSpeaking(String text, JSONObject options, CallbackContext callbackContext) {
        Log.v(TAG,"startSpeaking " + text + " " + String.valueOf(options));
        SpeechSynthesizer sp = getSynthesizer();

        sp.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        sp.setParameter(SpeechConstant.SPEED, "50");
        sp.setParameter(SpeechConstant.VOLUME, "80");
        sp.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        sp.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        sp.setParameter(SpeechConstant.TTS_AUDIO_PATH, TTS_FILE);

        if (options != null) {
            Iterator it = options.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                String value = options.optString(key);
                sp.setParameter(key, value);
            }
        }

        sp.startSpeaking(text, this);
    }

    private void pauseSpeaking(CallbackContext callbackContext) {
        getSynthesizer().pauseSpeaking();
    }

    private void resumeSpeaking(CallbackContext callbackContext) {
        getSynthesizer().resumeSpeaking();
    }

    private void stopSpeaking(CallbackContext callbackContext) {
        getSynthesizer().stopSpeaking();
    }

    private void sendUpdate(JSONObject obj, boolean keepCallback, PluginResult.Status status) {
        if (callback != null) {
            PluginResult result = new PluginResult(status, obj);
            result.setKeepCallback(keepCallback);
            callback.sendPluginResult(result);
            if (!keepCallback) {
                callback = null;
            }
        }
    }

    private void sendUpdate(JSONObject obj, boolean keepCallback) {
        sendUpdate(obj, keepCallback, PluginResult.Status.OK);
    }

    private void fireEvent(String event) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, event);
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBufferProgress(int progress, int beginPos, int endPos, String info) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_BUFFER_PROGRESS);
            obj.put(STR_PROGRESS, progress);
            obj.put(STR_MESSAGE, info);
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted(SpeechError error) {
        Log.v(TAG,"onCompleted " + String.valueOf(error));
	
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_SPEAK_COMPLETED);
            if (error != null) {
                obj.put(STR_CODE, error.getErrorCode());
                obj.put(STR_MESSAGE, error.getErrorDescription());
            }
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSpeakBegin() {
        fireEvent(EVENT_SPEAK_BEGIN);
    }

    @Override
    public void onSpeakPaused() {
        fireEvent(EVENT_SPEAK_PAUSED);
    }

    @Override
    public void onSpeakProgress(int progress, int beginPos, int endPos) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_SPEAK_PROGRESS);
            obj.put(STR_PROGRESS, progress);
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSpeakResumed() {
        fireEvent(EVENT_SPEAK_RESUMED);
    }

    @Override
    public void onBeginOfSpeech() {
        fireEvent(EVENT_SPEECH_BEGIN);
    }

    @Override
    public void onEndOfSpeech() {
        fireEvent(EVENT_SPEECH_END);
    }

    @Override
    public void onError(SpeechError error) {
        Log.v(TAG,"onError " + error.toString());
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_SPEECH_ERROR);
            if (error != null) {
                obj.put(STR_CODE, error.getErrorCode());
                obj.put(STR_MESSAGE, error.getErrorDescription());
            }
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(RecognizerResult result, boolean isLast) {
        Log.v(TAG,"onResult RecognizerResult " + String.valueOf(result) + " isLast " + String.valueOf(isLast));
	
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_SPEECH_RESULTS);
            String text = result.getResultString();
            obj.put(STR_RESULTS, text);
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVolumeChanged(int volume,byte[] data) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(STR_EVENT, EVENT_VOLUME_CHANGED);
            obj.put(STR_VOLUME, volume * 100 / 30);
            sendUpdate(obj, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        Log.v(TAG,String.format("onEvent %d %d %d %s",eventType,arg1,arg2,String.valueOf(obj)));
    }
}
