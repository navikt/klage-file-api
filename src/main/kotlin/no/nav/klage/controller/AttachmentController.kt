package no.nav.klage.controller

import jakarta.servlet.http.HttpServletResponse
import no.nav.klage.getLogger
import no.nav.klage.service.AttachmentService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("attachment")
class AttachmentController(private val attachmentService: AttachmentService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("{id}")
    fun getAttachmentOutputStream(
        @PathVariable("id") id: String,
        response: HttpServletResponse
    ) {
        logger.debug("getAttachmentOutputStream requested with id {}", id)

        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=file.pdf")
        response.contentType = MediaType.APPLICATION_PDF_VALUE

        attachmentService.getAttachmentAsBlob(id).downloadTo(response.outputStream)
    }

    @GetMapping("{id}/signedurl")
    fun getDocumentAsSignedUrl(
        @PathVariable("id") id: String,
        response: HttpServletResponse
    ): String {
        logger.debug("getDocumentAsSignedUrl request with id {}", id)

        return attachmentService.getAttachmentAsSignedUrl(id = id)
    }

    @PostMapping
    fun addAttachment(@RequestParam("file") file: MultipartFile): ResponseEntity<AttachmentCreatedResponse> {
        logger.debug("Add attachment requested.")
        val id = attachmentService.saveAttachment(file)
        return ResponseEntity(AttachmentCreatedResponse(id), HttpStatus.CREATED)
    }

    @DeleteMapping("{id}")
    fun deleteAttachment(@PathVariable("id") id: String): Boolean {
        logger.debug("Delete attachment requested.")
        return attachmentService.deleteAttachment(id)
    }

    data class AttachmentCreatedResponse(val id: String)
}