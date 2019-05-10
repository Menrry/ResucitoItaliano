package it.cammino.risuscito

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.viewmodels.InsertSearchViewModel
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.ricerca_tab_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import java.lang.ref.WeakReference

class InsertVeloceFragment : Fragment() {

    internal val cantoAdapter: FastItemAdapter<InsertItem> = FastItemAdapter()

    private var searchTask: SearchTask? = null
    private var rootView: View? = null
    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private var mViewModel: InsertSearchViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.ricerca_tab_layout, container, false)

        mViewModel = ViewModelProviders.of(this).get(InsertSearchViewModel::class.java)

        val bundle = arguments
        fromAdd = bundle!!.getInt("fromAdd")
        idLista = bundle.getInt("idLista")
        listPosition = bundle.getInt("position")

        activity!!.tempTextField
                .addTextChangedListener(
                        object : TextWatcher {

                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                val tempText = textfieldRicerca.text.toString()
                                if (tempText != s.toString()) textfieldRicerca.setText(s)
                            }

                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                            override fun afterTextChanged(s: Editable) {}
                        })

        mLUtils = LUtils.getInstance(activity!!)

        populateDb()
        subscribeUiFavorites()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ricerca_subtitle.text = getString(R.string.fast_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<InsertItem>, item: InsertItem, _: Int ->
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()

                if (fromAdd == 1) {
                    ListeUtils.addToListaDupAndFinish(activity!!, idLista, listPosition, item.id)
                } else {
                    ListeUtils.updateListaPersonalizzataAndFinish(activity!!, idLista, item.id, listPosition)
                }
            }
            true
        }

        cantoAdapter.addEventHook(object : ClickEventHook<InsertItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? InsertItem.ViewHolder)?.mPreview
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<InsertItem>, item: InsertItem) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity!!.applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf("pagina" to item.source!!.getText(context), "idCanto" to item.id))
                mLUtils!!.startActivityWithTransition(intent)
            }
        })

        cantoAdapter.setHasStableIds(true)

        matchedList.adapter = cantoAdapter
        val mMainActivity = activity as GeneralInsertSearch?
        val llm = if (mMainActivity!!.isGridLayout)
            GridLayoutManager(context, if (mMainActivity.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        matchedList.layoutManager = llm
        matchedList.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        matchedList.addItemDecoration(insetDivider)

        pulisci_ripple.setOnClickListener {
            textfieldRicerca.setText("")
            search_no_results.visibility = View.GONE
        }

        consegnati_only_check.setOnCheckedChangeListener { _, isChecked ->
            if (textfieldRicerca.text.toString().isNotEmpty())
                ricercaStringa(textfieldRicerca.text.toString(), isChecked)
        }

        textfieldRicerca.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                (ContextCompat.getSystemService(context as Context, InputMethodManager::class.java) as InputMethodManager)
                        .hideSoftInputFromWindow(textfieldRicerca.windowToken, 0)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        textfieldRicerca.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (consegnati_only_check != null)
                            ricercaStringa(s.toString(), consegnati_only_check.isChecked)
                    }
                }
        )

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isResumed && isVisibleToUser) {
            Log.d(TAG, "VISIBLE: ")
            // to hide soft keyboard
            (ContextCompat.getSystemService(context as Context, InputMethodManager::class.java) as InputMethodManager)
                    .hideSoftInputFromWindow(textfieldRicerca?.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
            searchTask!!.cancel(true)
    }

    private fun ricercaStringa(s: String, onlyConsegnati: Boolean) {
        val tempText = activity?.tempTextField?.text?.toString() ?: ""
        if (tempText != s) activity!!.tempTextField.setText(s)

        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
                searchTask!!.cancel(true)
            searchTask = SearchTask(this@InsertVeloceFragment)
            searchTask!!.execute(textfieldRicerca.text.toString(), onlyConsegnati.toString())
        } else {
            if (s.isEmpty()) {
                if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
                    searchTask!!.cancel(true)
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: InsertVeloceFragment) : AsyncTask<String, Void, ArrayList<InsertItem>>() {

        private val fragmentReference: WeakReference<InsertVeloceFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sParam: String): ArrayList<InsertItem> {

            val titoliResult = ArrayList<InsertItem>()

            Log.d(javaClass.name, "STRINGA: " + sParam[0])

            val stringa = Utility.removeAccents(sParam[0]).toLowerCase()
            Log.d(javaClass.name, "onTextChanged: stringa $stringa")

            val onlyConsegnati = java.lang.Boolean.parseBoolean(sParam[1])

            fragmentReference.get()!!.mViewModel!!.titoli.sortedBy { it.title!!.getText(fragmentReference.get()!!.context) }
                    .filter { Utility.removeAccents(it.title!!.getText(fragmentReference.get()!!.context)).toLowerCase().contains(stringa) && (!onlyConsegnati || it.consegnato == 1) }
                    .forEach {
                        if (isCancelled) return titoliResult
                        titoliResult.add(it.withFilter(stringa))
                    }

            return titoliResult
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()?.search_no_results?.visibility = View.GONE
            fragmentReference.get()?.search_progress?.visibility = View.VISIBLE
        }

        override fun onPostExecute(titoliResult: ArrayList<InsertItem>) {
            super.onPostExecute(titoliResult)
            if (isCancelled) return
            fragmentReference.get()?.cantoAdapter?.set(titoliResult)
            fragmentReference.get()?.search_progress?.visibility = View.INVISIBLE
            fragmentReference.get()?.search_no_results?.visibility = if (fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    private fun populateDb() {
        mViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mViewModel!!
                .itemsResult!!
                .observe(
                        this,
                        Observer<List<InsertItem>> { canti ->
                            if (canti != null) {
                                mViewModel!!.titoli = canti.sortedBy { it.title!!.getText(context) }
                            }
                        })
    }

    companion object {
        private val TAG = InsertVeloceFragment::class.java.canonicalName
    }

}
