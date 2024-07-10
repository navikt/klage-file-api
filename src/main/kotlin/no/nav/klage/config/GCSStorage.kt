package no.nav.klage.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GCSStorage(
    @Value("\${GCS_CREDENTIALS}")
    val gcsCredentials: String
) {

    @Bean
    fun gcsStorage(): Storage {
        return StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(gcsCredentials.byteInputStream()))
            .build()
            .service
    }
}