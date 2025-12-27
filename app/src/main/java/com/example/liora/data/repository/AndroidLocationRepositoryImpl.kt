package com.example.liora.data.repository

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.example.liora.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Implementação concreta do LocationRepository que usa as APIs do Android.
 * É a única classe no nosso sistema que sabe sobre FusedLocationProvider e Geocoder.
 *
 * @param application O contexto da aplicação é necessário para acessar serviços do sistema.
 */
class AndroidLocationRepositoryImpl(
    private val application: Application
) : LocationRepository {

    // Cliente de localização do Google.
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    // Ferramenta do Android para converter coordenadas em endereços.
    private val geocoder by lazy {
        Geocoder(application, Locale("pt", "BR"))
    }

    override suspend fun getCurrentCityState(): Result<String> {
        // --- ETAPA 1: VERIFICAR A PERMISSÃO ---
        // Uma verificação de segurança crucial. Se não tivermos permissão, falhamos imediatamente.
        val hasPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return Result.failure(Exception("Permissão de localização não concedida."))
        }

        // --- ETAPA 2: OBTER AS COORDENADAS ---
        return try {
            // Usamos a API moderna para pedir uma localização atual. É mais confiável que a "última localização".
            // A prioridade de 'BALANCED_POWER_ACCURACY' é ideal para obter cidade/bairro sem gastar muita bateria.
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await() // .await() transforma a tarefa assíncrona em uma suspend function

            if (location == null) {
                // Caso raro em que o serviço não consegue retornar uma localização.
                return Result.failure(Exception("Não foi possível obter a localização."))
            }

            // --- ETAPA 3: CONVERTER COORDENADAS EM NOME DE CIDADE/ESTADO ---
            // O Geocoder pode falhar se não houver conexão com a internet.
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val address = addresses?.firstOrNull()
            if (address == null) {
                return Result.failure(Exception("Não foi possível traduzir as coordenadas em um endereço."))
            }

            // Formatamos a string para "Cidade, UF"
            val city = address.subAdminArea ?: address.locality ?: "Cidade não encontrada"
            val state = address.adminArea ?: "Estado não encontrado"
            val resultString = "$city, $state"

            Result.success(resultString)

        } catch (e: Exception) {
            // Captura qualquer erro no processo (ex: serviços do Google desativados, etc.)
            Result.failure(e)
        }
    }
}