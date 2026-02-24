package no.nav.klage.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import no.nav.klage.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.channels.Channels

@Service
class KlageService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${GCS_BUCKET}")
    private lateinit var bucket: String

    fun getKlageAsResource(id: String): Resource {
        logger.debug("Getting klage with id {}", id)

        val blob = getGcsStorage().get(bucket, id.toPath())
        if (blob == null || !blob.exists()) {
            logger.warn("Klage not found: {}", id)
            throw FileNotFoundException()
        }

        return InputStreamResource(Channels.newInputStream(blob.reader()))
    }

    fun deleteKlage(id: String): Boolean {
        logger.debug("Deleting klage with id {}", id)
        return getGcsStorage().delete(BlobId.of(bucket, id.toPath())).also {
            if (it) {
                logger.debug("Klage was deleted.")
            } else {
                logger.debug("Klage was not found and could not be deleted.")
            }
        }
    }

    fun saveKlage(file: MultipartFile, id: String): Boolean {
        logger.debug("Saving klage")

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, id.toPath())).build()
        file.inputStream.use { inputStream ->
            getGcsStorage().writer(blobInfo).use { writer ->
                val buffer = ByteArray(1024 * 1024) // 1 MB buffer
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    writer.write(ByteBuffer.wrap(buffer, 0, bytesRead))
                }
            }
        }
        val result = getGcsStorage().get(bucket, id.toPath())?.exists() ?: false

        logger.debug("Klage saved, and id is {}", id)

        return result
    }

    private fun String.toPath() = "klage/$this"

    private fun getGcsStorage() = StorageOptions.getDefaultInstance().service

}
