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
import java.util.*

@Service
class AttachmentService(private val gcsStorage: Storage) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${GCS_BUCKET}")
    private lateinit var bucket: String

    fun getAttachmentAsResource(id: String): Resource {
        logger.debug("Getting attachment with id {}", id)
        return ByteArrayResource(gcsStorage.get(bucket, id).getContent())
    }

    fun deleteAttachment(id: String) {
        logger.debug("Deleting attachment with id {}", id)
        gcsStorage.delete(BlobId.of(bucket, id))
    }

    fun saveAttachment(file: MultipartFile): String {
        logger.debug("Saving attachment")

        val id = UUID.randomUUID().toString()

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, id)).build()
        gcsStorage.create(blobInfo, file.bytes)

        logger.debug("Attachment saved, and id is {}", id)

        return id
    }

}