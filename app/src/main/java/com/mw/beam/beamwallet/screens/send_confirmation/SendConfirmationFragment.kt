/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.send_confirmation

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToCurrencyString
import com.mw.beam.beamwallet.core.helpers.createSpannableString
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.app_activity.PendingSendInfo
import com.mw.beam.beamwallet.screens.confirm.PasswordConfirmDialog

import kotlinx.android.synthetic.main.fragment_send_confirmation.*

class SendConfirmationFragment : BaseFragment<SendConfirmationPresenter>(), SendConfirmationContract.View {
    private val args: SendConfirmationFragmentArgs by lazy {
        SendConfirmationFragmentArgs.fromBundle(arguments!!)
    }

    private val foregroundStartColorSpan by lazy { ForegroundColorSpan(resources.getColor(R.color.sent_color, context?.theme)) }
    private val foregroundEndColorSpan by lazy { ForegroundColorSpan(resources.getColor(R.color.sent_color, context?.theme)) }

    override fun getToolbarTitle(): String? = getString(R.string.confirmation)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_send_confirmation

    override fun getAddress(): String = args.sendAddress
    override fun getOutgoingAddress(): String = args.outgoingAddress
    override fun getAmount(): Long = args.sendAmount
    override fun getFee(): Long = args.fee
    override fun getComment(): String? = args.comment

    @SuppressLint("SetTextI18n")
    override fun init(address: String, outgoingAddress: String, amount: Double, fee: Long) {
        sendTo.text = address

        val length = address.length
        val spannable = SpannableStringBuilder.valueOf(address)


        spannable.setSpan(foregroundStartColorSpan, 0, if (length < 7) length else 6, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable.setSpan(foregroundEndColorSpan, if (length - 6 < 0) 0 else length - 6, length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        sendTo.text = spannable

        this.outgoingAddress.text = outgoingAddress
        amountToSend.text = "${amount.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondAvailableSum.text = amount.convertToCurrencyString()
        this.fee.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.sent_color)
    }

    override fun configureContact(walletAddress: WalletAddress, tags: List<Tag>) {
        if (!walletAddress.label.isBlank()) {
            contactName.visibility = View.VISIBLE
            contactName.text = walletAddress.label
        }

        if (tags.isNotEmpty()) {
            contactCategory.visibility = View.VISIBLE
            contactCategory.text = tags.createSpannableString(context!!)
        }
    }

    override fun showConfirmDialog() {
       this.passwordDialog =  PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.SendBeam, {
            presenter?.onConfirmed()
        }, {

        })
        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter?.onSendPressed()
        }
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
    }

    @SuppressLint("SetTextI18n")
    override fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double) {
        totalUtxoTitle.visibility = View.VISIBLE
        totalUtxo.visibility = View.VISIBLE
        changeUtxoTitle.visibility = View.VISIBLE
        changeUtxo.visibility = View.VISIBLE

        totalUtxo.text = "${usedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        changeUtxo.text = "${changedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
    }

    override fun delaySend(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        (activity as? AppActivity)?.pendingSend(PendingSendInfo(token, comment, amount, fee, outgoingAddress))
    }

    override fun showSaveAddressFragment(address: String) {
        findNavController().navigate(SendConfirmationFragmentDirections.actionSendConfirmationFragmentToSaveAddressFragment(address))
    }

    override fun showWallet() {
        findNavController().popBackStack(R.id.walletFragment, false)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendConfirmationPresenter(this, SendConfirmationRepository(), SendConfirmationState())
    }
}