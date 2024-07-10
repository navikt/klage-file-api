package no.nav.klage.service

import com.google.cloud.storage.Blob
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
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class AttachmentService(
    private val gcsStorage: Storage,
    @Value("\${GCS_BUCKET}")
    private var bucket: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getAttachmentAsResource(id: String): ByteArrayResource {
        logger.debug("Getting attachment with id {}", id)

        val blob = gcsStorage.get(bucket, id.toPath())

        if (blob == null || !blob.exists()) {
            logger.warn("Attachment not found: {}", id)
            throw FileNotFoundException()
        }

        return ByteArrayResource(blob.getContent())
    }

    fun getAttachmentAsBlob(id: String): Blob {
        logger.debug("Getting attachment with id {}", id)

        val blob = gcsStorage.get(bucket, id.toPath())

        if (blob == null || !blob.exists()) {
            logger.warn("Attachment not found: {}", id)
            throw FileNotFoundException()
        }

        return blob
    }

    fun getAttachmentAsSignedUrl(id: String): String {
        logger.debug("Getting attachment as signed URL with id {}", id)

        val blob = gcsStorage.get(bucket, id.toPath())

        if (blob == null || !blob.exists()) {
            logger.warn("Document not found: {}", id)
            throw FileNotFoundException()
        }

        return blob.signUrl(1, TimeUnit.MINUTES).toExternalForm()
    }

    fun deleteAttachment(id: String): Boolean {
        logger.debug("Deleting attachment with id {}", id)
        return gcsStorage.delete(BlobId.of(bucket, id.toPath())).also {
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
        gcsStorage.create(blobInfo, file.bytes)

        logger.debug("Attachment saved, and id is {}", id)

        return id
    }

    private fun String.toPath() = "attachment/$this"
}