package com.cervons.chickenroad.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FirebaseAuthService {

    private static final Logger LOG = Logger.getLogger(FirebaseAuthService.class);

    public FirebaseToken verifyToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            LOG.error("Error verifying Firebase token: " + e.getMessage());
            return null;
        }
    }
}
