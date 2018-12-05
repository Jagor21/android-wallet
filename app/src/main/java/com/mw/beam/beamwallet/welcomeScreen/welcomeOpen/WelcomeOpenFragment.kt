package com.mw.beam.beamwallet.welcomeScreen.welcomeOpen

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_open.*

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeOpenFragment : BaseFragment<WelcomeOpenPresenter>(), WelcomeOpenContract.View {
    private lateinit var presenter: WelcomeOpenPresenter
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter.onPassChanged()
        }
    }

    companion object {
        fun newInstance() = WelcomeOpenFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeOpenFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_open

    override fun addListeners() {
        btnOpen.setOnClickListener {
            presenter.onOpenWallet()
        }

        btnChange.setOnClickListener {
            presenter.onChangeWallet()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun hasValidPass(): Boolean {
        var hasErrors = false
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true

        if (pass.text.isNullOrBlank()) {
            passError.text = getString(R.string.welcome_pass_empty_error)
            passError.visibility = View.VISIBLE
            pass.isStateError = true
            hasErrors = true
        }

        return !hasErrors
    }

    override fun clearError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun clearListeners() {
        btnOpen.setOnClickListener(null)
        btnChange.setOnClickListener(null)

        pass.removeTextChangedListener(passWatcher)
    }

    override fun getPass(): String = pass.text?.trim().toString()
    override fun openWallet() = (activity as WelcomeMainHandler).openWallet()

    override fun showOpenWalletError() {
        pass.isStateError = true
        passError.text = getString(R.string.welcome_pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showChangeAlert() {
        showAlert(getString(R.string.welcome_change_alert), R.string.welcome_change, R.drawable.ic_btn_change_dark) { presenter.onChangeConfirm() }
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomeOpenPresenter(this, WelcomeOpenRepository())
        return presenter
    }

    interface WelcomeMainHandler {
        fun openWallet()
    }
}