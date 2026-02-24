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
import java.nio.channels.Channels
import java.util.*

@Service
class AttachmentService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${GCS_BUCKET}")
    private lateinit var bucket: String

    fun getAttachmentAsResource(id: String): Resource {
        logger.debug("Getting attachment with id {}", id)

        val blob = getGcsStorage().get(bucket, id.toPath())
        if (blob == null || !blob.exists()) {
            logger.warn("Attachment not found: {}", id)
            throw FileNotFoundException()
        }

        return InputStreamResource(Channels.newInputStream(blob.reader()))
    }

    fun deleteAttachment(id: String): Boolean {
        logger.debug("Deleting attachment with id {}", id)
        return getGcsStorage().delete(BlobId.of(bucket, id.toPath())).also {
            if (it) {
                logger.debug("Attachment was deleted.")
            } else {
                logger.debug("Attachment was not found and could not be deleted.")
            }
        }
    }

    fun saveAttachment(file: MultipartFile): String {
        logger.debug("Saving attachment")

        val id = UUID.randomUUID().toString()

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, id.toPath())).build()
        getGcsStorage().create(blobInfo, file.bytes)

        logger.debug("Attachment saved, and id is {}", id)

        return id
    }

    private fun String.toPath() = "attachment/$this"

    private fun getGcsStorage() = StorageOptions.getDefaultInstance().service

}