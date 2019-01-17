package com.mw.beam.beamwallet.wallet

import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import io.reactivex.disposables.Disposable


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletPresenter(currentView: WalletContract.View, currentRepository: WalletContract.Repository, private val state: WalletState)
    : BasePresenter<WalletContract.View, WalletContract.Repository>(currentView, currentRepository),
        WalletContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onReceivePressed() {
        view?.showReceiveScreen()
    }

    override fun onSendPressed() {
        view?.showSendScreen()
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription)
    }

    override fun onExpandAvailablePressed() {
        state.shouldExpandAvailable = !state.shouldExpandAvailable
        view?.handleExpandAvailable(state.shouldExpandAvailable)
    }

    override fun onExpandInProgressPressed() {
        state.shouldExpandInProgress = !state.shouldExpandInProgress
        view?.handleExpandInProgress(state.shouldExpandInProgress)

        if (!state.shouldExpandInProgress) {
            view?.configInProgress(state.walletStatus?.receiving ?: 0, state.walletStatus?.sending ?:0, state.walletStatus?.maturing ?: 0)
        }
    }

    override fun onTransactionsMenuButtonPressed(menu: View) {
        view?.showTransactionsMenu(menu)
    }

    override fun onTransactionsMenuPressed(item: MenuItem): Boolean {
        return view?.handleTransactionsMenu(item) ?: false
    }

    override fun onSearchPressed() = toDo()
    override fun onFilterPressed() = toDo()
    override fun onExportPressed() = toDo()
    override fun onDeletePressed() = toDo()

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
            view?.configWalletStatus(it)
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            view?.configTransactions(state.updateTransactions(data.tx))
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, txStatusSubscription)

    private fun toDo() {
        view?.showSnackBar("Coming soon...")
    }

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
