/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2021-2022 The Evolution X Project
* Copyright (C) 2018-2021 crDroid Android Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.evolution.oneplus.DeviceExtras;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import java.util.Arrays;

import org.evolution.oneplus.DeviceExtras.doze.DozeSettingsActivity;
import org.evolution.oneplus.DeviceExtras.FileUtils;
import org.evolution.oneplus.DeviceExtras.modeswitch.*;
import org.evolution.oneplus.DeviceExtras.panelsettings.PanelSettingsActivity;
import org.evolution.oneplus.DeviceExtras.preferences.*;
import org.evolution.oneplus.DeviceExtras.R;
import org.evolution.oneplus.DeviceExtras.services.*;
import org.evolution.oneplus.DeviceExtras.slider.SliderConstants;
import org.evolution.oneplus.DeviceExtras.touch.TouchscreenGestureSettings;

public class DeviceExtras extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = DeviceExtras.class.getSimpleName();

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    public static final String KEY_CATEGORY_AUDIO = "audio";
    public static final String KEY_CATEGORY_SLIDER = "slider";
    public static final String KEY_CATEGORY_DISPLAY = "display";
    public static final String KEY_CATEGORY_FPS = "fps";
    public static final String KEY_CATEGORY_TOUCHSCREEN = "touchscreen";
    public static final String KEY_CATEGORY_SPEAKER_MIC = "speaker";
    public static final String KEY_CATEGORY_USB = "usb";
    public static final String KEY_CATEGORY_VIBRATOR = "vibrator";

    public static final String KEY_DOZE = "advanced_doze_settings";
    public static final String KEY_PANEL_MODES = "panel_modes";
    public static final String KEY_KCAL = "kcal";
    public static final String KEY_P3_SWITCH = "p3";
    public static final String KEY_CUSTOMER_P3_SWITCH = "customer_p3";
    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_CUSTOMER_SRGB_SWITCH = "customer_srgb";
    public static final String KEY_WIDE_SWITCH = "wide";
    public static final String KEY_LOADING_EFFECT_SWITCH = "loading_effect";
    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_HBM_INFO = "hbm_info";

    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_FPS_INFO_POSITION = "fps_info_position";
    public static final String KEY_FPS_INFO_COLOR = "fps_info_color";
    public static final String KEY_FPS_INFO_TEXT_SIZE = "fps_info_text_size";

    public static final String KEY_TOUCHSCREEN_GESTURES = "touchscreen_gestures";
    public static final String KEY_GAME_SWITCH = "game_mode";
    public static final String KEY_GAME_SWITCH_WARNING = "game_mode_warning";
    public static final String KEY_TP_EDGE_LIMIT_SWITCH = "tp_edge_limit";

    public static final String KEY_EAR_GAIN = "earpiece_gain";
    public static final String KEY_MIC_GAIN = "microphone_gain";

    public static final String KEY_USB2_SWITCH = "usb2_fast_charge";
    public static final String KEY_OTG_SWITCH = "otg";

    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_CALL_VIBSTRENGTH = "vib_call_strength";
    public static final String KEY_NOTIF_VIBSTRENGTH = "vib_notif_strength";

    private static ListPreference mFpsInfoPosition;
    private static ListPreference mFpsInfoColor;
    private static SwitchPreference mFpsInfo;
    private static TwoStatePreference mAutoHBMSwitch;
    private static TwoStatePreference mDCModeSwitch;
    private static TwoStatePreference mGameModeSwitch;
    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mTPEdgeLimitModeSwitch;
    private static TwoStatePreference mUSB2FastChargeModeSwitch;
    private static TwoStatePreference mOTGModeSwitch;

    private CustomSeekBarPreference mFpsInfoTextSizePreference;
    private EarGainPreference mEarGain;
    private Preference mDozeSettings;
    private Preference mTouchScreenGestureSettings;
    private ListPreference mBottomKeyPref;
    private ListPreference mMiddleKeyPref;
    private ListPreference mTopKeyPref;
    private MicGainPreference mMicGain;
    private VibratorStrengthPreference mVibratorStrength;
    private VibratorCallStrengthPreference mVibratorCallStrength;
    private VibratorNotifStrengthPreference mVibratorNotifStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        addPreferencesFromResource(R.xml.main);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        Context context = this.getContext();

        // Audio - Dolby
        if (isFeatureSupported(context, R.bool.config_deviceSupportsDolby)) {
        }
        else {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_AUDIO));
        }

        // Slider Preferences
        if (isFeatureSupported(context, R.bool.config_deviceSupportsAlertSlider)) {
            initNotificationSliderPreference();
        }
        else {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_SLIDER));
        }

        boolean displayCategory = false;

        // DozeSettings Activity
        mDozeSettings = (Preference)findPreference(KEY_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        // Panel Modes
        displayCategory = displayCategory | isFeatureSupported(context, R.bool.config_deviceSupportsPanelModes);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsPanelModes)) {
        }
        else {
            findPreference(KEY_PANEL_MODES).setVisible(false);
        }

        // Kcal
        displayCategory = displayCategory | isFeatureSupported(context, R.bool.config_deviceSupportsKcal);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsKcal)) {
        }
        else {
            findPreference(KEY_KCAL).setVisible(false);
        }

        // DC-Dimming
        displayCategory = displayCategory | isFeatureSupported(context, R.bool.config_deviceSupportsDCdimming);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsDCdimming)) {
            mDCModeSwitch = (TwoStatePreference) findPreference(KEY_DC_SWITCH);
            mDCModeSwitch.setEnabled(DCModeSwitch.isSupported(this.getContext()));
            mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled(this.getContext()));
            mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());
        }
        else {
            findPreference(KEY_DC_SWITCH).setVisible(false);
        }

        // HBM
        displayCategory = displayCategory | isFeatureSupported(context, R.bool.config_deviceSupportsHBM);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsHBM)) {
            mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
            mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported(getContext()));
            mHBMModeSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceExtras.KEY_HBM_SWITCH, false));
            mHBMModeSwitch.setOnPreferenceChangeListener(this);
        }
        else {
            findPreference(KEY_HBM_SWITCH).setVisible(false);
        }

        // AutoHBM
        displayCategory = displayCategory | isFeatureSupported(context, R.bool.config_deviceSupportsAutoHBM);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsAutoHBM)) {
            mAutoHBMSwitch = (TwoStatePreference) findPreference(KEY_AUTO_HBM_SWITCH);
            mAutoHBMSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceExtras.KEY_AUTO_HBM_SWITCH, false));
            mAutoHBMSwitch.setOnPreferenceChangeListener(this);
        }
        else {
            findPreference(KEY_AUTO_HBM_SWITCH).setVisible(false);
            findPreference(KEY_AUTO_HBM_THRESHOLD).setVisible(false);
            findPreference(KEY_HBM_INFO).setVisible(false);
        }

        if (!displayCategory) {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_DISPLAY));
        }

        // FPS
        if (isFeatureSupported(context, R.bool.config_deviceSupportsFPS)) {
            mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
            mFpsInfo.setChecked(prefs.getBoolean(KEY_FPS_INFO, false));
            mFpsInfo.setOnPreferenceChangeListener(this);

            mFpsInfoPosition = (ListPreference) findPreference(KEY_FPS_INFO_POSITION);
            mFpsInfoPosition.setOnPreferenceChangeListener(this);

            mFpsInfoColor = (ListPreference) findPreference(KEY_FPS_INFO_COLOR);
            mFpsInfoColor.setOnPreferenceChangeListener(this);

            mFpsInfoTextSizePreference = (CustomSeekBarPreference) findPreference(KEY_FPS_INFO_TEXT_SIZE);
            mFpsInfoTextSizePreference.setOnPreferenceChangeListener(this);
        }
        else {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_FPS));
        }

        boolean touchscreenCategory = false;

        // TouchScreen Gestures
        touchscreenCategory = touchscreenCategory | isFeatureSupported(context, R.bool.config_deviceSupportsTouchScreenGestures);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsTouchScreenGestures)) {
        }
        else {
            findPreference(KEY_TOUCHSCREEN_GESTURES ).setVisible(false);
        }

        // Game Mode
        touchscreenCategory = touchscreenCategory | isFeatureSupported(context, R.bool.config_deviceSupportsGameMode);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsGameMode)) {
            mGameModeSwitch = (TwoStatePreference) findPreference(KEY_GAME_SWITCH);
            mGameModeSwitch.setEnabled(GameModeSwitch.isSupported(this.getContext()));
            mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled(this.getContext()));
            mGameModeSwitch.setOnPreferenceChangeListener(new GameModeSwitch());
        }
        else {
           findPreference(KEY_GAME_SWITCH).setVisible(false);
           findPreference(KEY_GAME_SWITCH_WARNING).setVisible(false);
        }

        // TP Edge Limit
        touchscreenCategory = touchscreenCategory | isFeatureSupported(context, R.bool.config_deviceSupportsTPEdgeLimit);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsTPEdgeLimit)) {
            mTPEdgeLimitModeSwitch = (TwoStatePreference) findPreference(KEY_TP_EDGE_LIMIT_SWITCH);
            mTPEdgeLimitModeSwitch.setEnabled(TPEdgeLimitModeSwitch.isSupported(this.getContext()));
            mTPEdgeLimitModeSwitch.setChecked(TPEdgeLimitModeSwitch.isCurrentlyEnabled(this.getContext()));
            mTPEdgeLimitModeSwitch.setOnPreferenceChangeListener(new TPEdgeLimitModeSwitch());
        }
        else {
           findPreference(KEY_TP_EDGE_LIMIT_SWITCH).setVisible(false);
        }

        if (!touchscreenCategory) {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_TOUCHSCREEN));
        }

        boolean speakerCategory = false;

        // Earpiece gain
        speakerCategory = speakerCategory | isFeatureSupported(context, R.bool.config_deviceSupportsEarGain);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsEarGain)) {
            mEarGain = (EarGainPreference) findPreference(KEY_EAR_GAIN);
            if (mEarGain != null) {
                mEarGain.setEnabled(EarGainPreference.isSupported(getContext()));
            }
        }
        else {
            findPreference(KEY_EAR_GAIN).setVisible(false);
        }

        // Microphone gain
        speakerCategory = speakerCategory | isFeatureSupported(context, R.bool.config_deviceSupportsMicGain);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsMicGain)) {
            mMicGain = (MicGainPreference) findPreference(KEY_MIC_GAIN);
            if (mMicGain != null) {
                mMicGain.setEnabled(MicGainPreference.isSupported(getContext()));
            }
        }
        else {
            findPreference(KEY_MIC_GAIN).setVisible(false);
        }

        if (!speakerCategory) {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_SPEAKER_MIC));
        }

        boolean usbCategory = false;

        // USB2 Force FastCharge
        usbCategory = usbCategory | isFeatureSupported(context, R.bool.config_deviceSupportsUSB2FC);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsUSB2FC)) {
            mUSB2FastChargeModeSwitch = (TwoStatePreference) findPreference(KEY_USB2_SWITCH);
            mUSB2FastChargeModeSwitch.setEnabled(USB2FastChargeModeSwitch.isSupported(this.getContext()));
            mUSB2FastChargeModeSwitch.setChecked(USB2FastChargeModeSwitch.isCurrentlyEnabled(this.getContext()));
            mUSB2FastChargeModeSwitch.setOnPreferenceChangeListener(new USB2FastChargeModeSwitch());
        }
        else {
           findPreference(KEY_USB2_SWITCH).setVisible(false);
        }

        // OTG Switch
        usbCategory = usbCategory | isFeatureSupported(context, R.bool.config_deviceSupportsOTG);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsOTG)) {
            mOTGModeSwitch = (TwoStatePreference) findPreference(KEY_OTG_SWITCH);
            mOTGModeSwitch.setEnabled(OTGModeSwitch.isSupported(this.getContext()));
            mOTGModeSwitch.setChecked(OTGModeSwitch.isCurrentlyEnabled(this.getContext()));
            mOTGModeSwitch.setOnPreferenceChangeListener(new OTGModeSwitch());
        }
        else {
           findPreference(KEY_OTG_SWITCH).setVisible(false);
        }

        if (!usbCategory) {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_USB));
        }

        boolean vibratorCategory = false;

        // Vibrator
        vibratorCategory = vibratorCategory | isFeatureSupported(context, R.bool.config_deviceSupportsSysVib);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsSysVib)) {
            mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
            if (mVibratorStrength != null) {
                mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported(this.getContext()));
            }
        }
        else {
            findPreference(KEY_VIBSTRENGTH).setVisible(false);
        }

        // Vibrator - Call
        vibratorCategory = vibratorCategory | isFeatureSupported(context, R.bool.config_deviceSupportsCallVib);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsCallVib)) {
            mVibratorCallStrength = (VibratorCallStrengthPreference) findPreference(KEY_CALL_VIBSTRENGTH );
            if (mVibratorCallStrength != null) {
                mVibratorCallStrength.setEnabled(VibratorCallStrengthPreference.isSupported(this.getContext()));
            }
        }
        else {
            findPreference(KEY_CALL_VIBSTRENGTH).setVisible(false);
        }

        // Vibrator - Notification
        vibratorCategory = vibratorCategory | isFeatureSupported(context, R.bool.config_deviceSupportsNotifVib);
        if (isFeatureSupported(context, R.bool.config_deviceSupportsNotifVib)) {
            mVibratorNotifStrength = (VibratorNotifStrengthPreference) findPreference(KEY_NOTIF_VIBSTRENGTH);
            if (mVibratorNotifStrength != null) {
                mVibratorNotifStrength.setEnabled(VibratorNotifStrengthPreference.isSupported(this.getContext()));
            }
        }
        else {
            findPreference(KEY_NOTIF_VIBSTRENGTH).setVisible(false);
        }

        if (!vibratorCategory) {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_CATEGORY_VIBRATOR));
        }
    }

    private static boolean isFeatureSupported(Context ctx, int feature) {
        try {
            return ctx.getResources().getBoolean(feature);
        }
        // TODO: Replace with proper exception type class
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isHBMModeService(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceExtras.KEY_HBM_SWITCH, false);
    }

    public static boolean isAUTOHBMEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceExtras.KEY_AUTO_HBM_SWITCH, false);
    }

    private void initNotificationSliderPreference() {
        registerPreferenceListener(SliderConstants.NOTIF_SLIDER_USAGE_KEY);
        registerPreferenceListener(SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY);
        registerPreferenceListener(SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY);
        registerPreferenceListener(SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY);

        ListPreference usagePref = (ListPreference) findPreference(
                SliderConstants.NOTIF_SLIDER_USAGE_KEY);
        handleSliderUsageChange(usagePref.getValue());
    }

    private void registerPreferenceListener(String key) {
        Preference p = findPreference(key);
        p.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        if (isFeatureSupported(this.getContext(), R.bool.config_deviceSupportsHBM)) {
            mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()));
        }
        if (isFeatureSupported(this.getContext(), R.bool.config_deviceSupportsFPS)) {
            mFpsInfo.setChecked(isFPSOverlayRunning());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_AUTO_HBM_SWITCH, enabled).commit();
            FileUtils.enableService(getContext());
            return true;
        } else if (preference == mHBMModeSwitch) {
            Boolean enabled = (Boolean) newValue;
            FileUtils.writeValue(HBMModeSwitch.getFile(getContext()), enabled ? "5" : "0");
            Intent hbmIntent = new Intent(this.getContext(),
                    org.evolution.oneplus.DeviceExtras.services.HBMModeService.class);
            if (enabled) {
                this.getContext().startService(hbmIntent);
            } else {
                this.getContext().stopService(hbmIntent);
            }
            return true;
          } else if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            Intent fpsinfo = new Intent(this.getContext(),
                    org.evolution.oneplus.DeviceExtras.services.FPSInfoService.class);
            if (enabled) {
                this.getContext().startService(fpsinfo);
            } else {
                this.getContext().stopService(fpsinfo);
            }
            return true;
        } else if (preference == mFpsInfoPosition) {
            int position = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isPositionChanged(mContext, position)) {
                FPSInfoService.setPosition(mContext, position);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;
        } else if (preference == mFpsInfoColor) {
            int color = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isColorChanged(mContext, color)) {
                FPSInfoService.setColorIndex(mContext, color);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;
        } else if (preference == mFpsInfoTextSizePreference) {
            int size = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isSizeChanged(mContext, size - 1)) {
                FPSInfoService.setSizeIndex(mContext, size - 1);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;

            }

        String key = preference.getKey();
        switch (key) {
            case SliderConstants.NOTIF_SLIDER_USAGE_KEY:
                return handleSliderUsageChange((String) newValue) &&
                        handleSliderUsageDefaultsChange((String) newValue) &&
                        notifySliderUsageChange((String) newValue);
            case SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY:
                return notifySliderActionChange(0, (String) newValue);
            case SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY:
                return notifySliderActionChange(1, (String) newValue);
            case SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY:
                return notifySliderActionChange(2, (String) newValue);
            default:
                break;
        }

        String node = SliderConstants.sBooleanNodePreferenceMap.get(key);
        if (!TextUtils.isEmpty(node) && FileUtils.fileWritable(node)) {
            Boolean value = (Boolean) newValue;
            FileUtils.writeValue(node, value ? "1" : "0");
            return true;
        }
        node = SliderConstants.sStringNodePreferenceMap.get(key);
        if (!TextUtils.isEmpty(node) && FileUtils.fileWritable(node)) {
            FileUtils.writeValue(node, (String) newValue);
            return true;
        }

        return false;
    }

    @Override
    public void addPreferencesFromResource(int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        // Initialize node preferences
        for (String pref : SliderConstants.sBooleanNodePreferenceMap.keySet()) {
            SwitchPreference b = (SwitchPreference) findPreference(pref);
            if (b == null) continue;
            String node = SliderConstants.sBooleanNodePreferenceMap.get(pref);
            if (FileUtils.isFileReadable(node)) {
                String curNodeValue = FileUtils.readOneLine(node);
                b.setChecked(curNodeValue.equals("1"));
                b.setOnPreferenceChangeListener(this);
            } else {
                removePref(b);
            }
        }
        for (String pref : SliderConstants.sStringNodePreferenceMap.keySet()) {
            ListPreference l = (ListPreference) findPreference(pref);
            if (l == null) continue;
            String node = SliderConstants.sStringNodePreferenceMap.get(pref);
            if (FileUtils.isFileReadable(node)) {
                l.setValue(FileUtils.readOneLine(node));
                l.setOnPreferenceChangeListener(this);
            } else {
                removePref(l);
            }
        }
    }

    private void removePref(Preference pref) {
        PreferenceGroup parent = pref.getParent();
        if (parent == null) {
            return;
        }
        parent.removePreference(pref);
        if (parent.getPreferenceCount() == 0) {
            removePref(parent);
        }
    }

    private boolean handleSliderUsageChange(String newValue) {
        switch (newValue) {
            case SliderConstants.NOTIF_SLIDER_FOR_NOTIFICATION:
                return updateSliderActions(
                        R.array.notification_slider_mode_entries,
                        R.array.notification_slider_mode_entry_values);
            case SliderConstants.NOTIF_SLIDER_FOR_FLASHLIGHT:
                return updateSliderActions(
                        R.array.notification_slider_flashlight_entries,
                        R.array.notification_slider_flashlight_entry_values);
            case SliderConstants.NOTIF_SLIDER_FOR_BRIGHTNESS:
                return updateSliderActions(
                        R.array.notification_slider_brightness_entries,
                        R.array.notification_slider_brightness_entry_values);
            case SliderConstants.NOTIF_SLIDER_FOR_ROTATION:
                return updateSliderActions(
                        R.array.notification_slider_rotation_entries,
                        R.array.notification_slider_rotation_entry_values);
            case SliderConstants.NOTIF_SLIDER_FOR_RINGER:
                return updateSliderActions(
                        R.array.notification_slider_ringer_entries,
                        R.array.notification_slider_ringer_entry_values);
            case SliderConstants.NOTIF_SLIDER_FOR_NOTIFICATION_RINGER:
                return updateSliderActions(
                        R.array.notification_ringer_slider_mode_entries,
                        R.array.notification_ringer_slider_mode_entry_values);
            default:
                return false;
        }
    }

    private boolean handleSliderUsageDefaultsChange(String newValue) {
        int defaultsResId = getDefaultResIdForUsage(newValue);
        if (defaultsResId == 0) {
            return false;
        }
        return updateSliderActionDefaults(defaultsResId);
    }

    private boolean updateSliderActions(int entriesResId, int entryValuesResId) {
        String[] entries = getResources().getStringArray(entriesResId);
        String[] entryValues = getResources().getStringArray(entryValuesResId);
        return updateSliderPreference(SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY,
                entries, entryValues) &&
            updateSliderPreference(SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY,
                    entries, entryValues) &&
            updateSliderPreference(SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY,
                    entries, entryValues);
    }

    private boolean updateSliderActionDefaults(int defaultsResId) {
        String[] defaults = getResources().getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return false;
        }

        return updateSliderPreferenceValue(SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY,
                defaults[0]) &&
            updateSliderPreferenceValue(SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY,
                    defaults[1]) &&
            updateSliderPreferenceValue(SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY,
                    defaults[2]);
    }

    private boolean updateSliderPreference(CharSequence key,
            String[] entries, String[] entryValues) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setEntries(entries);
        pref.setEntryValues(entryValues);
        return true;
    }

    private boolean updateSliderPreferenceValue(CharSequence key,
            String value) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setValue(value);
        return true;
    }

    private int[] getCurrentSliderActions() {
        int[] actions = new int[3];
        ListPreference p;

        p = (ListPreference) findPreference(
                SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY);
        actions[0] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY);
        actions[1] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY);
        actions[2] = Integer.parseInt(p.getValue());

        return actions;
    }

    private boolean notifySliderUsageChange(String usage) {
        sendUpdateBroadcast(getActivity().getApplicationContext(), Integer.parseInt(usage),
                getCurrentSliderActions());
        return true;
    }

    private boolean notifySliderActionChange(int index, String value) {
        ListPreference p = (ListPreference) findPreference(
                SliderConstants.NOTIF_SLIDER_USAGE_KEY);
        int usage = Integer.parseInt(p.getValue());

        int[] actions = getCurrentSliderActions();
        actions[index] = Integer.parseInt(value);

        sendUpdateBroadcast(getActivity().getApplicationContext(), usage, actions);
        return true;
    }

    public static void sendUpdateBroadcast(Context context,
            int usage, int[] actions) {
        Intent intent = new Intent(SliderConstants.ACTION_UPDATE_SLIDER_SETTINGS);
        intent.putExtra(SliderConstants.EXTRA_SLIDER_USAGE, usage);
        intent.putExtra(SliderConstants.EXTRA_SLIDER_ACTIONS, actions);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        Log.d(TAG, "update slider usage " + usage + " with actions: " +
                Arrays.toString(actions));
    }

    public static void restoreSliderStates(Context context) {
        Resources res = context.getResources();
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        String usage = prefs.getString(SliderConstants.NOTIF_SLIDER_USAGE_KEY,
                res.getString(R.string.config_defaultNotificationSliderUsage));

        int defaultsResId = getDefaultResIdForUsage(usage);
        if (defaultsResId == 0) {
            return;
        }

        String[] defaults = res.getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return;
        }

        String actionTop = prefs.getString(
                SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY, defaults[0]);

        String actionMiddle = prefs.getString(
                SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY, defaults[1]);

        String actionBottom = prefs.getString(
                SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY, defaults[2]);

        prefs.edit()
            .putString(SliderConstants.NOTIF_SLIDER_USAGE_KEY, usage)
            .putString(SliderConstants.NOTIF_SLIDER_ACTION_TOP_KEY, actionTop)
            .putString(SliderConstants.NOTIF_SLIDER_ACTION_MIDDLE_KEY, actionMiddle)
            .putString(SliderConstants.NOTIF_SLIDER_ACTION_BOTTOM_KEY, actionBottom)
            .commit();

        sendUpdateBroadcast(context, Integer.parseInt(usage), new int[] {
            Integer.parseInt(actionTop),
            Integer.parseInt(actionMiddle),
            Integer.parseInt(actionBottom)
        });
    }

    private static int getDefaultResIdForUsage(String usage) {
        switch (usage) {
            case SliderConstants.NOTIF_SLIDER_FOR_NOTIFICATION:
                return R.array.config_defaultSliderActionsForNotification;
            case SliderConstants.NOTIF_SLIDER_FOR_FLASHLIGHT:
                return R.array.config_defaultSliderActionsForFlashlight;
            case SliderConstants.NOTIF_SLIDER_FOR_BRIGHTNESS:
                return R.array.config_defaultSliderActionsForBrightness;
            case SliderConstants.NOTIF_SLIDER_FOR_ROTATION:
                return R.array.config_defaultSliderActionsForRotation;
            case SliderConstants.NOTIF_SLIDER_FOR_RINGER:
                return R.array.config_defaultSliderActionsForRinger;
            case SliderConstants.NOTIF_SLIDER_FOR_NOTIFICATION_RINGER:
                return R.array.config_defaultSliderActionsForNotificationRinger;
            default:
                return 0;
        }
    }

    private boolean isFPSOverlayRunning() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                am.getRunningServices(Integer.MAX_VALUE))
            if (FPSInfoService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
   }

    private void restartFpsInfo(Context context) {
        Intent fpsinfo = new Intent(context, FPSInfoService.class);
        context.stopService(fpsinfo);
        context.startService(fpsinfo);
    }
}
