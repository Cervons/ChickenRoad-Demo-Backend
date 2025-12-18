package com.cervons.chickenroad;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.io.InputStream;

@ApplicationScoped
public class FirebaseInitializer {

    private static final Logger LOG = Logger.getLogger(FirebaseInitializer.class);

    void onStart(@Observes StartupEvent ev) {
        try {
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                LOG.error(
                        "Firebase configuration file 'firebase-service-account.json' not found in src/main/resources/!");
                LOG.error("Please download it from Firebase Console -> Project Settings -> Service Accounts.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                LOG.info("Firebase has been initialized successfully!");
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}
