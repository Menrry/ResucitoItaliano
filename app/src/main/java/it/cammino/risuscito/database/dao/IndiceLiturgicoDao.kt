package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import it.cammino.risuscito.database.CantoLiturgico
import it.cammino.risuscito.database.entities.IndiceLiturgico
import it.cammino.risuscito.database.entities.NomeLiturgico

@Suppress("unused")
@Dao
interface IndiceLiturgicoDao {

    @get:Query("SELECT C.titolo, C.pagina, C.source, C.color, C.id, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
    val liveAll: LiveData<List<CantoLiturgico>>

    @get:Query("SELECT C.titolo, C.pagina, C.source, C.color, C.id, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
    val all: List<CantoLiturgico>

    @Query("DELETE FROM indiceliturgico")
    fun truncateIndiceLiturgico()

    @Query("DELETE FROM nomeliturgico")
    fun truncateNomeIndiceLiturgico()

    @Insert
    fun insertIndice(indice: IndiceLiturgico)

    @Insert
    fun insertIndice(indiceList: List<IndiceLiturgico>)

    @Insert
    fun insertNomeIndice(nomeIndice: NomeLiturgico)

    @Insert
    fun insertNomeIndice(nomeIndiceList: List<NomeLiturgico>)
}
