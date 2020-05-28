package no.nav.klage.controller

import no.nav.klage.service.AttachmentService
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException

@RestController
@RequestMapping("attachment")
class AttachmentController(private val attachmentService: AttachmentService) {

    @GetMapping("{id}")
    fun getAttachment(@PathVariable("id") id: String): ResponseEntity<Resource> {
        return try {
            val resource = attachmentService.getAttachmentAsResource(id)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        } catch (fnfe: FileNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun addAttachment(@RequestParam("file") file: MultipartFile): ResponseEntity<AttachmentCreatedResponse> {
        val id = attachmentService.saveAttachment(file)
        return ResponseEntity(AttachmentCreatedResponse(id), HttpStatus.CREATED)
    }

    @DeleteMapping("{id}")
    fun deleteAttachment(@PathVariable("id") id: String) {
        attachmentService.deleteAttachment(id)
    }

    data class AttachmentCreatedResponse(val id: String)
}