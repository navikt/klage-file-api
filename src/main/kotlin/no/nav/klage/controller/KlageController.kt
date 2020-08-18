package no.nav.klage.controller

import no.nav.klage.getLogger
import no.nav.klage.service.KlageService
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException

@RestController
@RequestMapping("klage")
class KlageController(private val klageService: KlageService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("{id}")
    fun getKlage(@PathVariable("id") id: String): ResponseEntity<Resource> {
        logger.debug("Get klage requested with id {}", id)
        return try {
            val resource = klageService.getKlageAsResource(id)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        } catch (fnfe: FileNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun addKlage(@RequestParam("file") file: MultipartFile): ResponseEntity<KlageCreatedResponse> {
        logger.debug("Add klage requested.")
        val id = klageService.saveKlage(file)
        return ResponseEntity(KlageCreatedResponse(id), HttpStatus.CREATED)
    }

    @DeleteMapping("{id}")
    fun deleteKlage(@PathVariable("id") id: String): Boolean {
        logger.debug("Delete klage requested.")
        return klageService.deleteKlage(id)
    }

    data class KlageCreatedResponse(val id: String)
}