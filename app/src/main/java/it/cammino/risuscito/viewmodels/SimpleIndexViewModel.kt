package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.SimpleItem

class SimpleIndexViewModel(application: Application) : GenericIndexViewModel(application) {

    var itemsResult: LiveData<List<SimpleItem>>? = null
        private set
    //-1 come valore per indicare che non è mai stato settato ancora (fragment appena creato)
    var tipoLista: Int = -1

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        when (tipoLista) {
            0, 1 ->
                itemsResult = Transformations.map(mDb!!.cantoDao().liveAll) { canti ->
                    val newList = ArrayList<SimpleItem>()
                    canti.forEach {
                        newList.add(
                                SimpleItem()
                                        .withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                                        .withPage(LUtils.getResId(it.pagina, R.string::class.java))
                                        .withSource(LUtils.getResId(it.source, R.string::class.java))
                                        .withColor(it.color!!)
                                        .withId(it.id)
                                        .withUndecodedSource(it.source ?: "")
                        )
                    }
                    newList
                }
            2 ->
                itemsResult = Transformations.map(mDb!!.salmiDao().liveAll) { canti ->
                    val newList = ArrayList<SimpleItem>()
                    canti.forEach {
                        newList.add(
                                SimpleItem()
                                        .withTitle(LUtils.getResId(it.titoloSalmo, R.string::class.java))
                                        .withPage(LUtils.getResId(it.pagina, R.string::class.java))
                                        .withSource(LUtils.getResId(it.source, R.string::class.java))
                                        .withColor(it.color!!)
                                        .withId(it.id)
                                        .withNumSalmo(it.numSalmo!!)
                        )
                    }
                    newList
                }
        }
    }
}
