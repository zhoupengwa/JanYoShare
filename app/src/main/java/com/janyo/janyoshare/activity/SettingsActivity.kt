@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.SwitchPreference
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.janyo.janyoshare.R
import com.janyo.janyoshare.handler.PayHandler

import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.Logs.Logs
import java.util.*
import kotlin.concurrent.timerTask

class SettingsActivity : PreferenceActivity()
{
	private val TAG = "SettingsActivity"
	private lateinit var settings: Settings
	private lateinit var toolbar: Toolbar
	private lateinit var payHandler: PayHandler
	private lateinit var auto_clean: SwitchPreference
	private lateinit var developerMode: PreferenceCategory
	private lateinit var developerModeEnable: SwitchPreference
	private lateinit var autoUploadLog: SwitchPreference
	private lateinit var about: Preference
	private lateinit var howToUse: Preference
	private lateinit var openSourceAddress: Preference
	private lateinit var license: Preference
	private lateinit var checkUpdate: Preference
	private lateinit var versionCode: Preference
	private lateinit var support: Preference
	private var clickTime = 0
	private var isGooglePlayPay = false

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		settings = Settings(this@SettingsActivity)
		payHandler = PayHandler(this, Volley.newRequestQueue(this))
		addPreferencesFromResource(R.xml.preferences)
		initialization()
		monitor()
		toolbar.title = title
	}

	private fun initialization()
	{
		auto_clean = findPreference(getString(R.string.key_auto_clean)) as SwitchPreference
		developerMode = findPreference(getString(R.string.key_developer_mode)) as PreferenceCategory
		developerModeEnable = findPreference(getString(R.string.key_developer_mode_enable)) as SwitchPreference
		autoUploadLog = findPreference(getString(R.string.key_auto_upload_log)) as SwitchPreference
		about = findPreference(getString(R.string.key_about))
		howToUse = findPreference(getString(R.string.key_how_to_use))
		openSourceAddress = findPreference(getString(R.string.key_open_source_address))
		license = findPreference(getString(R.string.key_license))
		checkUpdate = findPreference(getString(R.string.key_check_update))
		versionCode = findPreference(getString(R.string.key_version_code))
		support = findPreference(getString(R.string.key_support))

		auto_clean.isChecked = settings.isAutoClean
		developerModeEnable.isChecked = settings.isDeveloperModeEnable
		autoUploadLog.isChecked = settings.isAutoUploadLog

		if (settings.isAutoClean)
		{
			auto_clean.setSummary(R.string.summary_auto_clean_on)
		}
		else
		{
			auto_clean.setSummary(R.string.summary_auto_clean_off)
		}

		if (!settings.isDeveloperModeEnable)
		{
			preferenceScreen.removePreference(developerMode)
		}
		else
		{
			clickTime = 7
		}
	}

	private fun monitor()
	{
		auto_clean.setOnPreferenceChangeListener { _, _ ->
			val isAutoClean = !auto_clean.isChecked
			if (isAutoClean)
			{
				AlertDialog.Builder(this@SettingsActivity)
						.setTitle(" ")
						.setMessage(R.string.autoCleanWarn)
						.setPositiveButton(R.string.action_open) { _, _ -> settings.isAutoClean = true }
						.setNegativeButton(R.string.action_cancel) { _, _ ->
							auto_clean.isChecked = false
							settings.isAutoClean = false
						}
						.setOnDismissListener {
							auto_clean.isChecked = settings.isAutoClean
							if (settings.isAutoClean)
							{
								auto_clean.setSummary(R.string.summary_auto_clean_on)
							}
							else
							{
								auto_clean.setSummary(R.string.summary_auto_clean_off)
							}
						}
						.show()
			}
			else
			{
				settings.isAutoClean = false
				auto_clean.setSummary(R.string.summary_auto_clean_off)
			}
			true
		}
		developerModeEnable.setOnPreferenceChangeListener { _, _ ->
			val isDeveloperModeEnable = !developerModeEnable.isChecked
			settings.isDeveloperModeEnable = isDeveloperModeEnable
			true
		}
		autoUploadLog.setOnPreferenceChangeListener { _, _ ->
			val isAutoUploadLog = !autoUploadLog.isChecked
			settings.isAutoUploadLog = isAutoUploadLog
			true
		}
		about.setOnPreferenceClickListener {
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_about, LinearLayout(this@SettingsActivity), false))
					.setPositiveButton(R.string.action_close, null)
					.show()
			false
		}
		howToUse.setOnPreferenceClickListener {
			val view = LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_help, LinearLayout(this@SettingsActivity), false)
			val textView = view.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings.isAutoClean)
			{
				textView.visibility = View.VISIBLE
			}
			AlertDialog.Builder(this@SettingsActivity)
					.setTitle(" ")
					.setView(view)
					.setPositiveButton(R.string.action_done, null)
					.show()
			false
		}
		openSourceAddress.setOnPreferenceClickListener {
			val intent = Intent()
			intent.action = "android.intent.action.VIEW"
			val content_url = Uri.parse(getString(R.string.address_open_source))
			intent.data = content_url
			startActivity(intent)
			false
		}
		license.setOnPreferenceClickListener {
			val view_license = LayoutInflater.from(this@SettingsActivity).inflate(R.layout.dialog_license, NestedScrollView(this@SettingsActivity), false)
			val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
			val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
			val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
			val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
			point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
			text_license_point1.setCompoundDrawables(point, null, null, null)
			text_license_point2.setCompoundDrawables(point, null, null, null)
			text_license_point3.setCompoundDrawables(point, null, null, null)
			AlertDialog.Builder(this)
					.setTitle(" ")
					.setView(view_license)
					.setPositiveButton(R.string.action_done, { _, _ ->
						settings.isFirst = false
					})
					.show()
			false
		}
		checkUpdate.setOnPreferenceClickListener {
			val intent = Intent()
			intent.action = "android.intent.action.VIEW"
			val content_url = Uri.parse(getString(R.string.address_check_update))
			intent.data = content_url
			startActivity(intent)
			false
		}
		versionCode.setOnPreferenceClickListener {
			when
			{
				clickTime < 3 ->
					clickTime++
				clickTime in 3..6 ->
				{
					val hintToast = Toast.makeText(this, String.format(getString(R.string.hint_developer_mode), 7 - clickTime), Toast.LENGTH_SHORT)
					hintToast.show()
					Timer().schedule(timerTask {
						hintToast.cancel()
					}, 100)
					clickTime++
				}
				clickTime >= 7 ->
				{
					if (!settings.isDeveloperModeEnable)
					{
						preferenceScreen.addPreference(developerMode)
						settings.isDeveloperModeEnable = true
						developerModeEnable.isChecked = true
					}
					val hintToast = Toast.makeText(this, R.string.hint_developer_mode_enable, Toast.LENGTH_SHORT)
					hintToast.show()
					Timer().schedule(timerTask {
						hintToast.cancel()
					}, 1000)
				}
			}
			false
		}
		support.setOnPreferenceClickListener {
			AlertDialog.Builder(this)
					.setTitle(R.string.pay_method_title)
					.setItems(R.array.pay_method, { _, choose ->
						val message = Message()
						when (choose)
						{
							0 ->
							{
								message.what = PayHandler.PAY_PLAY
								isGooglePlayPay = true
							}
							1 ->
							{
								message.what = PayHandler.PAY_ALIPAY
								isGooglePlayPay = false
							}
							2 ->
							{
								message.what = PayHandler.PAY_WEIXIN
								isGooglePlayPay = false
							}
						}
						payHandler.sendMessage(message)
					})
					.show()
			false
		}
	}

	override fun setContentView(layoutResID: Int)
	{
		val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
		toolbar = contentView.findViewById<Toolbar>(R.id.toolbar)
		toolbar.setNavigationOnClickListener { finish() }

		val contentWrapper = contentView.findViewById<ViewGroup>(R.id.content_wrapper)
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)

		window.setContentView(contentView)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (!isGooglePlayPay && payHandler.onPayResult(requestCode, resultCode, data))
			super.onActivityResult(requestCode, resultCode, data)
		else
			Logs.i(TAG, "onActivityResult handled by IABUtil.")
	}

	override fun onDestroy()
	{
		payHandler.playDestroy()
		super.onDestroy()
	}
}
