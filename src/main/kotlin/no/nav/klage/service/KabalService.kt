package no.nav.klage.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import no.nav.klage.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.util.*

@Service
class KabalService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${GCS_BUCKET_KABAL}")
    private lateinit var bucket: String

    fun getKabalFileAsResource(id: String): Resource {
        logger.debug("Getting Kabal file with id {}", id)

        val blob = getGcsStorage().get(bucket, id.toPath())
        if (blob == null || !blob.exists()) {
            logger.warn("Kabal file not found: {}", id)
            throw FileNotFoundException()
        }

        return ByteArrayResource(blob.getContent())
    }

    fun deleteKabalFile(id: String): Boolean {
        logger.debug("Deleting Kabal file with id {}", id)
        return getGcsStorage().delete(BlobId.of(bucket, id.toPath())).also {
            if (it) {
                logger.debug("Kabal file was deleted.")
            } else {
                logger.debug("Kabal file was not found and could not be deleted.")
            }
        }
    }

    fun saveKabalFile(file: MultipartFile): String {
        logger.debug("Saving Kabal file")

        val id = UUID.randomUUID().toString()

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, id.toPath())).build()
        getGcsStorage().create(blobInfo, file.bytes)

        logger.debug("Attachment saved, and id is {}", id)

        return id
    }

    private fun String.toPath() = "file/$this"

    private fun getGcsStorage() = StorageOptions.getDefaultInstance().service
}