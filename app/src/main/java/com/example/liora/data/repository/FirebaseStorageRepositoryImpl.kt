package com.example.liora.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.liora.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Implementação concreta do StorageRepository que usa o Google Firebase Storage.
 * Requer um Context para ler e processar as Uri's das imagens.
 */
class FirebaseStorageRepositoryImpl(
    private val storage: FirebaseStorage,
    private val context: Context // Contexto necessário para ContentResolver
) : StorageRepository {

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) { // Garante que operações de IO sejam feitas em thread apropriada
            try {
                // 1. Criar uma referência única para o arquivo no Storage.
                val fileName = "profile_image_${UUID.randomUUID()}.jpg"
                val imageRef = storage.reference.child("profile_images/$userId/$fileName")

                // 2. Processar a imagem: redimensionar e comprimir
                val processedImageBytes = processImage(imageUri)
                    ?: return@withContext Result.failure(Exception("Falha ao processar a imagem."))

                // 3. Fazer o upload dos bytes da imagem processada.
                imageRef.putBytes(processedImageBytes).await()

                // 4. Obter a URL de download da imagem.
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // 5. Retornar a URL.
                Result.success(downloadUrl)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Processa a imagem: decodifica a Uri, redimensiona e comprime para JPEG.
     * Retorna um ByteArray com os dados da imagem processada.
     */
    private fun processImage(imageUri: Uri): ByteArray? {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        inputStream?.close()

        if (originalBitmap == null) {
            return null
        }

        // Definir um tamanho máximo para a imagem (ex: 1080px de largura)
        val maxWidth = 1080
        val bitmap: Bitmap = if (originalBitmap.width > maxWidth) {
            val aspectRatio = originalBitmap.height.toDouble() / originalBitmap.width.toDouble()
            val newHeight = (maxWidth * aspectRatio).toInt()
            Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        // Comprimir para JPEG com qualidade de 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Reciclar o bitmap original se ele foi escalado para liberar memória
        if (bitmap != originalBitmap) {
            originalBitmap.recycle()
        }

        return outputStream.toByteArray()
    }
}