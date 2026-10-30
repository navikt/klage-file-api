package no.nav.klage.config.security

import no.nav.klage.auth.test.AzureSecurityContractTest
import no.nav.klage.controller.KlageController
import no.nav.klage.service.KlageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(KlageController::class)
@Import(SecurityConfiguration::class)
class SecurityConfigurationTest : AzureSecurityContractTest() {
    @MockitoBean
    private lateinit var klageService: KlageService

    @BeforeEach
    fun setUp() {
        `when`(klageService.deleteKlage("abc")).thenReturn(true)
    }

    override fun protectedRequest() = delete("/klage/abc")

    override fun assertAccepted(result: ResultActions) {
        result.andExpect(status().isOk)
        verify(klageService).deleteKlage("abc")
    }

    override fun unauthenticatedPaths() = listOf("/internal/health", "/internal/info", "/internal/prometheus")

    @Test
    fun `other internal endpoints require token`() {
        mockMvc.perform(get("/internal/metrics")).andExpect(status().isUnauthorized)
    }
}
