package com.centinela.app.nutricion

import android.content.Context
import org.json.JSONObject

data class AlimentoInfo(
    val nombre: String,
    val categoria: String,
    val estado: String,
    val kcal: Double,
    val proteinas: Double,
    val carbohidratos: Double,
    val grasas: Double
)

class AlimentosRepository(private val context: Context) {

    private var cache: List<AlimentoInfo>? = null

    fun cargar(): List<AlimentoInfo> {
        cache?.let { return it }
        val texto = context.assets.open("alimentos.json").bufferedReader().use { it.readText() }
        val root = JSONObject(texto)
        val categorias = root.getJSONArray("categorias")
        val resultado = mutableListOf<AlimentoInfo>()

        for (i in 0 until categorias.length()) {
            val cat = categorias.getJSONObject(i)
            val nombreCategoria = cat.optString("nombre", "")
            val alimentos = cat.optJSONArray("alimentos") ?: continue
            for (j in 0 until alimentos.length()) {
                val al = alimentos.getJSONObject(j)
                val nombre = al.optString("nombre", "")

                al.optJSONObject("crudo_seco")?.let { crudo ->
                    resultado += AlimentoInfo(
                        nombre = nombre, categoria = nombreCategoria, estado = "Crudo/Seco",
                        kcal = crudo.optDouble("kcal", 0.0),
                        proteinas = crudo.optDouble("proteinas", 0.0),
                        carbohidratos = crudo.optDouble("carbohidratos", 0.0),
                        grasas = crudo.optDouble("grasas", 0.0)
                    )
                }
                al.optJSONArray("cocido")?.let { cocidos ->
                    for (k in 0 until cocidos.length()) {
                        val c = cocidos.getJSONObject(k)
                        val prep = c.optString("preparacion", if (cocidos.length() > 1) "cocido ${k + 1}" else "Cocido")
                        resultado += AlimentoInfo(
                            nombre = nombre, categoria = nombreCategoria, estado = prep,
                            kcal = c.optDouble("kcal", 0.0),
                            proteinas = c.optDouble("proteinas", 0.0),
                            carbohidratos = c.optDouble("carbohidratos", 0.0),
                            grasas = c.optDouble("grasas", 0.0)
                        )
                    }
                }
            }
        }
        cache = resultado
        return resultado
    }

    fun buscar(query: String): List<AlimentoInfo> =
        cargar().filter { it.nombre.contains(query, ignoreCase = true) }
}
