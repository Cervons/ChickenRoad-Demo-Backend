# Chicken Road Demo Backend

Backend for the Chicken Road game built with Quarkus.

## Deployment to Firebase App Hosting

This backend is configured to be deployed on Firebase App Hosting (which uses Cloud Run).

### Configuration

- Environment variables are managed in `apphosting.yaml`.
- The application connects to Redis using the `QUARKUS_REDIS_HOSTS` variable.
- CORS is opened to allow traffic from the frontend.

### Deployment Process

1. Push the changes to your GitHub repository (master branch).
2. Firebase App Hosting will automatically trigger a build based on the `Dockerfile` and `apphosting.yaml`.
3. Check the logs in the Firebase Console if you see any "500" errors.

## Firebase Integration

If you plan to use Firebase Auth or Firestore, make sure to add your `firebase-service-account.json` to `src/main/resources/` (but do not commit it to public git repos!).
In production, it's better to use Secret Manager for these credentials.