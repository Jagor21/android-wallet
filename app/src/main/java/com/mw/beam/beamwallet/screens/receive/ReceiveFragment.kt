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

package com.mw.beam.beamwallet.screens.receive

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import com.mw.beam.beamwallet.screens.address_edit.CategoryAdapter
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.fragment_receive.categorySpinner
import kotlinx.android.synthetic.main.fragment_receive.comment
import kotlinx.android.synthetic.main.fragment_receive.emptyCategoryListMessage

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {

    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter?.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })
        }
    }

    private val commentTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onCommentChanged()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val changeAddressCallback = object : ChangeAddressCallback {
        override fun onChangeAddress(walletAddress: WalletAddress) {
            presenter?.onAddressChanged(walletAddress)
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_receive
    override fun getToolbarTitle(): String? = getString(R.string.receive_title)

    override fun getAmountFromArguments(): Long {
        return ReceiveFragmentArgs.fromBundle(arguments!!).amount
    }

    override fun getWalletAddressFromArguments(): WalletAddress? {
        return ReceiveFragmentArgs.fromBundle(arguments!!).walletAddress
    }

    override fun getAmount(): Double? = amount.text?.toString()?.toDoubleOrNull()
    override fun setAmount(newAmount: Double) = amount.setText(newAmount.convertToBeamString())

    override fun init() {
        ArrayAdapter.createFromResource(
                context!!,
                R.array.receive_expires_periods,
                R.layout.receive_expire_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            expiresOnSpinner.adapter = adapter
            expiresOnSpinner.setSelection(0)
        }

        amount.filters = arrayOf(AmountFilter())
    }

    override fun initAddress(isGenerateAddress: Boolean, walletAddress: WalletAddress) {
        tokenTitle.setText(if (isGenerateAddress) R.string.receive_token_title_generated else R.string.receive_token_title)

        expiresOnSpinner.setSelection(if (walletAddress.duration == 0L) ExpirePeriod.NEVER.ordinal else ExpirePeriod.DAY.ordinal)

        comment.setText(walletAddress.label)

        token.text = walletAddress.walletID
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        animateDropDownIcon(btnExpandAdvanced, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        advancedGroup.visibility = if (expand) View.VISIBLE else View.GONE
    }

    override fun handleExpandEditAddress(expand: Boolean) {
        animateDropDownIcon(btnExpandEditAddress, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        editAddressGroup.visibility = if (expand) View.VISIBLE else View.GONE

        emptyCategoryListMessage.visibility = if (categorySpinner.adapter?.isEmpty != false && expand) View.VISIBLE else View.GONE

    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 360f else 180f
        val angleTo = if (shouldExpand) 180f else 360f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }


    override fun addListeners() {
        btnShareToken.setOnClickListener { presenter?.onShareTokenPressed() }
        btnShowQR.setOnClickListener { presenter?.onShowQrPressed() }
        expiresOnSpinner.onItemSelectedListener = expireListener

        val advancedClickListener = View.OnClickListener {
            presenter?.onAdvancedPressed()
        }
        advancedTitle.setOnClickListener(advancedClickListener)
        btnExpandAdvanced.setOnClickListener(advancedClickListener)

        val editAddressClickListener = View.OnClickListener {
            presenter?.onEditAddressPressed()
        }
        editAddressTitle.setOnClickListener(editAddressClickListener)
        btnExpandEditAddress.setOnClickListener(editAddressClickListener)

        btnChangeAddress.setOnClickListener {
            presenter?.onChangeAddressPressed()
        }

        comment.addTextChangedListener(commentTextWatcher)
    }

    override fun shareToken(receiveToken: String) {
        shareText(getString(R.string.common_share_title), receiveToken)
    }

    override fun showChangeAddressFragment() {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToChangeAddressFragment(callback = changeAddressCallback))
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    @SuppressLint("InflateParams")
    override fun showQR(walletAddress: WalletAddress, amount: Long?) {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToQrDialogFragment(walletAddress, amount ?: 0))
    }

    override fun getComment(): String? = comment.text?.toString()

    override fun configCategory(currentCategory: Category?, categories: List<Category>) {
        categorySpinner.isEnabled = categories.isNotEmpty()

        emptyCategoryListMessage.visibility = if (categories.isEmpty() && editAddressGroup.visibility == View.VISIBLE) View.VISIBLE else View.GONE

        if (categories.isNotEmpty()) {
            categorySpinner.adapter = CategoryAdapter(context!!, arrayListOf(CategoryHelper.noneCategory).apply { addAll(categories) })

            if (currentCategory == null) {
                categorySpinner.setSelection(0)
            } else {
                categorySpinner.setSelection(categories.indexOfFirst { currentCategory.id == it.id } + 1)
            }
        }


        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    presenter?.onSelectedCategory(categories[position - 1])
                } else {
                    presenter?.onSelectedCategory(null)
                }
            }
        }
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnShareToken.setOnClickListener(null)
        btnShowQR.setOnClickListener(null)
        btnChangeAddress.setOnClickListener(null)
        advancedTitle.setOnClickListener(null)
        btnExpandAdvanced.setOnClickListener(null)
        editAddressTitle.setOnClickListener(null)
        btnExpandEditAddress.setOnClickListener(null)

        comment.removeTextChangedListener(commentTextWatcher)

        expiresOnSpinner.onItemSelectedListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }
}
