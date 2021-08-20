package no.nav.klage.controller

import no.nav.klage.getLogger
import no.nav.klage.service.KabalService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("kabal")
class KabalController(private val kabalService: KabalService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("{id}")
    fun getKabalFile(@PathVariable("id") id: String): ResponseEntity<Resource> {
        logger.debug("Get Kabal file requested with id {}", id)
        return try {
            val resource = kabalService.getKabalFileAsResource(id)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        } catch (fnfe: FileNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun addKabalFile(@RequestParam("file") file: MultipartFile): ResponseEntity<AttachmentCreatedResponse> {
        logger.debug("Add Kabal file requested.")
        val id = kabalService.saveKabalFile(file)
        return ResponseEntity(AttachmentCreatedResponse(id), HttpStatus.CREATED)
    }

    @DeleteMapping("{id}")
    fun deleteKabalFile(@PathVariable("id") id: String): Boolean {
        logger.debug("Delete Kabal file requested.")
        return kabalService.deleteKabalFile(id)
    }

    data class AttachmentCreatedResponse(val id: String)
}