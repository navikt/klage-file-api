package no.nav.klage.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import no.nav.klage.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException

@Service
class KlageService(
    private val gcsStorage: Storage,
    @Value("\${GCS_BUCKET}")
    private var bucket: String
)  {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getKlageAsResource(id: String): Resource {
        logger.debug("Getting klage with id {}", id)

        val blob = gcsStorage.get(bucket, id.toPath())
        if (blob == null || !blob.exists()) {
            logger.warn("Klage not found: {}", id)
            throw FileNotFoundException()
        }

        return ByteArrayResource(blob.getContent())
    }

    fun deleteKlage(id: String): Boolean {
        logger.debug("Deleting klage with id {}", id)
        return gcsStorage.delete(BlobId.of(bucket, id.toPath())).also {
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
        val result = gcsStorage.create(blobInfo, file.bytes).exists()

        logger.debug("Klage saved, and id is {}", id)

        return result
    }

    private fun String.toPath() = "klage/$this"
}
